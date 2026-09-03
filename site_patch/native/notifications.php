<?php
declare(strict_types=1);
header('Content-Type: application/json; charset=utf-8'); header('Cache-Control: no-store'); require_login();
$pdo=Database::connection();$u=Auth::user();
$st=$pdo->prepare('SELECT n.id,n.type,n.data,n.read_at,n.created_at,u.username actor_username,u.avatar_path actor_avatar FROM notifications n LEFT JOIN users u ON u.id=n.actor_id WHERE n.user_id=? ORDER BY n.created_at DESC LIMIT 50');$st->execute([$u['id']]);$rows=$st->fetchAll();$items=[];foreach($rows as $n){$items[]=['id'=>(int)$n['id'],'type'=>$n['type'],'data'=>json_decode((string)($n['data']?:'{}'),true)?:[],'read_at'=>$n['read_at']??null,'created_at'=>$n['created_at'],'actor_username'=>$n['actor_username']??null,'actor_avatar'=>$n['actor_avatar']??null];}
$count=$pdo->prepare('SELECT COUNT(*) FROM notifications WHERE user_id=? AND read_at IS NULL');$count->execute([$u['id']]);
echo json_encode(['ok'=>true,'unread'=>(int)$count->fetchColumn(),'notifications'=>$items],JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES);
