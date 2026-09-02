<?php
declare(strict_types=1);
header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');
$page=max(1,(int)($_GET['page'] ?? 1));
$topics=get_topics(null,$page,20);
$cats=get_categories();
echo json_encode(['ok'=>true,'categories'=>$cats,'topics'=>$topics['items'],'total'=>(int)$topics['total'],'page'=>$page,'per_page'=>20], JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES);
