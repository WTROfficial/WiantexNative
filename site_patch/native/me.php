<?php
declare(strict_types=1);
header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');
require_login();
$user=Auth::user();
echo json_encode(['ok'=>true,'csrf_token'=>$_SESSION['csrf_token'] ?? '', 'user'=>[
'id'=>(int)$user['id'],'username'=>$user['username'],'email'=>$user['email'] ?? null,'bio'=>$user['bio'] ?? '','avatar_path'=>$user['avatar_path'] ?? null,'banner_path'=>$user['banner_path'] ?? null,'created_at'=>$user['created_at'] ?? null,'role_name'=>$user['role_name'] ?? null,'role_slug'=>$user['role_slug'] ?? null,'role_color'=>$user['role_color'] ?? null,'equipped_frame_path'=>$user['equipped_frame_path'] ?? null,'woin_balance'=>(int)($user['woin_balance'] ?? 0), 'last_seen_at'=>$user['last_seen_at'] ?? null,'presence_status'=>$user['presence_status'] ?? null
]], JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES);
