<?php
declare(strict_types=1);
header('Content-Type: application/json; charset=utf-8'); header('Cache-Control: no-store');
$username=trim((string)($_GET['username']??'')); if($username===''){require_login();$username=(string)(Auth::user()['username']??'');}
$pdo=Database::connection();
$st=$pdo->prepare('SELECT u.id,u.username,u.email,u.bio,u.avatar_path,u.banner_path,u.created_at,u.last_seen_at,u.presence_status,r.name role_name,r.slug role_slug,r.color role_color,f.image_path equipped_frame_path FROM users u JOIN roles r ON r.id=u.role_id LEFT JOIN user_frames uf ON uf.user_id=u.id AND uf.equipped=1 LEFT JOIN frames f ON f.id=uf.frame_id WHERE LOWER(u.username)=LOWER(?) LIMIT 1'); $st->execute([$username]); $u=$st->fetch();
if(!$u){http_response_code(404);echo json_encode(['ok'=>false,'message'=>'Kullanıcı bulunamadı.'],JSON_UNESCAPED_UNICODE);exit;}
$pc=$pdo->prepare('SELECT COUNT(*) FROM topics WHERE user_id=? AND is_deleted=0');$pc->execute([$u['id']]); $topics=(int)$pc->fetchColumn();
$mc=$pdo->prepare('SELECT COUNT(*) FROM posts WHERE user_id=? AND is_deleted=0');$mc->execute([$u['id']]); $posts=(int)$mc->fetchColumn();
$likes=$pdo->prepare('SELECT COUNT(*) FROM likes l JOIN posts p ON p.id=l.post_id WHERE p.user_id=?');$likes->execute([$u['id']]); $likeCount=(int)$likes->fetchColumn();
echo json_encode(['ok'=>true,'profile'=>['id'=>(int)$u['id'],'username'=>$u['username'],'email'=>$u['email']??null,'bio'=>$u['bio']??'','avatar_path'=>$u['avatar_path']??null,'banner_path'=>$u['banner_path']??null,'created_at'=>$u['created_at']??null,'last_seen_at'=>$u['last_seen_at']??null,'presence_status'=>$u['presence_status']??null,'role_name'=>$u['role_name'],'role_slug'=>$u['role_slug'],'role_color'=>$u['role_color'],'equipped_frame_path'=>$u['equipped_frame_path']??null,'topic_count'=>$topics,'post_count'=>$posts,'like_count'=>$likeCount]],JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES);
