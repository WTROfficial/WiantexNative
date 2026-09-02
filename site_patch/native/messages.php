<?php
declare(strict_types=1);
header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');
require_login();
$pdo=Database::connection(); $me=Auth::user();
if ($_SERVER['REQUEST_METHOD']==='POST') {
  $input=json_decode((string)file_get_contents('php://input'),true); if(!is_array($input))$input=$_POST;
  $username=trim((string)($input['username'] ?? '')); $content=trim((string)($input['content'] ?? '')); $csrf=(string)($input['csrf_token'] ?? '');
  if (!hash_equals((string)($_SESSION['csrf_token'] ?? ''),$csrf)) { http_response_code(419); echo json_encode(['ok'=>false,'message'=>'Güvenlik doğrulaması başarısız.'],JSON_UNESCAPED_UNICODE); exit; }
  if ($username===''||$content==='') { http_response_code(400); echo json_encode(['ok'=>false,'message'=>'Kullanıcı ve mesaj gerekli.'],JSON_UNESCAPED_UNICODE); exit; }
  if (mb_strlen($content)>2000) { http_response_code(400); echo json_encode(['ok'=>false,'message'=>'Mesaj çok uzun.'],JSON_UNESCAPED_UNICODE); exit; }
  $st=$pdo->prepare('SELECT id,username,avatar_path FROM users WHERE LOWER(username)=LOWER(?) LIMIT 1'); $st->execute([$username]); $target=$st->fetch();
  if(!$target || (int)$target['id']===(int)$me['id']) { http_response_code(404); echo json_encode(['ok'=>false,'message'=>'Kullanıcı bulunamadı.'],JSON_UNESCAPED_UNICODE); exit; }
  try {
    $one=min((int)$me['id'],(int)$target['id']); $two=max((int)$me['id'],(int)$target['id']);
    $c=$pdo->prepare('SELECT id FROM conversations WHERE user_one_id=? AND user_two_id=? LIMIT 1'); $c->execute([$one,$two]); $cid=(int)($c->fetchColumn()?:0);
    if(!$cid){$pdo->prepare('INSERT INTO conversations (user_one_id,user_two_id) VALUES (?,?)')->execute([$one,$two]);$cid=(int)$pdo->lastInsertId();}
    $ins=$pdo->prepare('INSERT INTO messages (conversation_id,sender_id,receiver_id,content) VALUES (?,?,?,?)'); $ins->execute([$cid,$me['id'],$target['id'],$content]); $mid=(int)$pdo->lastInsertId();
    $data=json_encode(['message_id'=>$mid,'username'=>$me['username']],JSON_UNESCAPED_UNICODE);
    try{$pdo->prepare('INSERT INTO notifications (user_id,actor_id,type,data) VALUES (?,?,"message",?)')->execute([$target['id'],$me['id'],$data]);}catch(Throwable $e){}
    echo json_encode(['ok'=>true,'message'=>['id'=>$mid,'content'=>$content,'sender_id'=>(int)$me['id'],'receiver_id'=>(int)$target['id'],'created_at'=>gmdate('Y-m-d H:i:s'),'mine'=>true]],JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES); exit;
  } catch(Throwable $e){ error_log('native message send: '.$e->getMessage()); http_response_code(500); echo json_encode(['ok'=>false,'message'=>'Mesaj gönderilemedi.'],JSON_UNESCAPED_UNICODE); exit; }
}
$username=trim((string)($_GET['username']??''));
if($username===''){ echo json_encode(['ok'=>true,'messages'=>[]]); exit; }
$st=$pdo->prepare('SELECT id,username,avatar_path FROM users WHERE LOWER(username)=LOWER(?) LIMIT 1'); $st->execute([$username]); $target=$st->fetch();
if(!$target){http_response_code(404);echo json_encode(['ok'=>false,'message'=>'Kullanıcı bulunamadı.'],JSON_UNESCAPED_UNICODE);exit;}
$one=min((int)$me['id'],(int)$target['id']);$two=max((int)$me['id'],(int)$target['id']);
$c=$pdo->prepare('SELECT id FROM conversations WHERE user_one_id=? AND user_two_id=? LIMIT 1'); $c->execute([$one,$two]); $cid=(int)($c->fetchColumn()?:0);
if(!$cid){echo json_encode(['ok'=>true,'messages'=>[]]);exit;}
$m=$pdo->prepare('SELECT id,sender_id,receiver_id,content,created_at,attachment_path,attachment_type,is_deleted,edited_at FROM messages WHERE conversation_id=? ORDER BY id ASC LIMIT 200'); $m->execute([$cid]);
$rows=$m->fetchAll();
$items=[]; foreach($rows as $r){$items[]=['id'=>(int)$r['id'],'sender_id'=>(int)$r['sender_id'],'receiver_id'=>(int)$r['receiver_id'],'content'=>$r['is_deleted']?'':(string)$r['content'],'created_at'=>$r['created_at'],'mine'=>(int)$r['sender_id']===(int)$me['id'],'attachment_path'=>$r['attachment_path']??null,'attachment_type'=>$r['attachment_type']??null,'is_deleted'=>(int)$r['is_deleted']===1,'edited_at'=>$r['edited_at']??null];}
echo json_encode(['ok'=>true,'messages'=>$items,'csrf_token'=>$_SESSION['csrf_token']??''],JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES);
