<?php
declare(strict_types=1);
header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');

$input = json_decode((string)file_get_contents('php://input'), true);
if (!is_array($input)) $input = $_POST;
$login = trim((string)($input['login'] ?? ''));
$password = (string)($input['password'] ?? '');
if ($login === '' || $password === '') { http_response_code(400); echo json_encode(['ok'=>false,'message'=>'Giriş bilgileri eksik.'], JSON_UNESCAPED_UNICODE); exit; }
[$ok,$message] = Auth::attempt($login,$password);
if (!$ok) { http_response_code(401); echo json_encode(['ok'=>false,'message'=>$message], JSON_UNESCAPED_UNICODE); exit; }
$user = Auth::user();
echo json_encode(['ok'=>true,'message'=>$message,'csrf_token'=>$_SESSION['csrf_token'] ?? '', 'user'=>[
'id'=>(int)$user['id'],'username'=>$user['username'],'avatar_path'=>$user['avatar_path'] ?? null,'role_name'=>$user['role_name'] ?? null,'role_slug'=>$user['role_slug'] ?? null,'role_color'=>$user['role_color'] ?? null,'equipped_frame_path'=>$user['equipped_frame_path'] ?? null,'woin_balance'=>(int)($user['woin_balance'] ?? 0)
]], JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES);
