<?php
declare(strict_types=1);

require __DIR__ . '/app/includes/bootstrap.php';
require __DIR__ . '/app/includes/icons.php';
require __DIR__ . '/app/includes/repositories.php';

$pagesDir = __DIR__ . '/app/pages';

$uri = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH) ?? '/';
$uri = rawurldecode($uri);
$path = trim($uri, '/');
$segments = $path === '' ? [] : explode('/', $path);

/**
 * Basit route tablosu: [pattern parcalari, dosya, param anahtarlari]
 * '*' bir segmenti parametre olarak yakalar.
 */
$routes = [
    [[],                              'home.php',                     []],
    [['forum'],                       'forum.php',                     []],
    [['category', '*'],               'category.php',                  ['slug']],
    [['topic', 'new'],                'topic_new.php',                 []],
    [['topic', '*'],                  'topic.php',                     ['slug']],
    [['members'],                     'members.php',                   []],
    [['register'],                    'register.php',                  []],
    [['login'],                       'login.php',                     []],
    [['logout'],                      'logout.php',                    []],
    [['forgot-password'],             'forgot_password.php',            []],
    [['reset-password'],              'reset_password.php',             []],
    [['search'],                      'search.php',                    []],
    [['notifications'],               'notifications.php',              []],
    [['messages'],                    'messages.php',                   []],
    [['messages', '*'],               'messages.php',                   ['username']],
    [['settings'],                    'settings.php',                   []],
    [['market'],                      'market.php',                     []],
    [['paketler'],                    'paketler.php',                  []],
    [['paketsatinal'],                'paketsatinal.php',              []],
    [['yetkibasvuru'],                'yetkibasvuru.php',                []],
    [['yetki-basvuru'],               'yetkibasvuru.php',                []],
    [['admin'],                       'admin/dashboard.php',            []],
    [['admin', 'yetki-kontrol'],      'admin/yetki-kontrol.php',           []],
    [['admin', 'market'],             'admin/market.php',                []],
    [['admin', 'users'],              'admin/users.php',                 []],
    [['admin', 'categories'],         'admin/categories.php',            []],
    [['admin', 'topics'],             'admin/topics.php',                []],
    [['admin', 'reports'],            'admin/reports.php',               []],
    [['admin', 'roles'],              'admin/roles.php',                 []],
    [['admin', 'password-resets'],    'admin/password_resets.php',       []],
    [['admin', 'appearance'],         'admin/appearance.php',             []],
    [['admin', 'dm-themes'],          'admin/dm_themes.php',              []],
    [['admin', 'logs'],              'admin/logs.php',                []],
    [['admin', 'settings'],           'admin/settings.php',             []],
    [['admin', 'site-themes'],         'admin/site_themes.php',            []],
    [['rules'],                       'static.php',                     []],
    [['support'],                     'support.php',                     []],

    // Wiantex hukuki sayfalari - dosyalar dogrudan app/pages/ altindadir.
    [['privacy'],                     'gizlilik-politikasi.php',          []],
    [['distance-sales'],              'mesafeli-satis-sozlesmesi.php',   []],
    [['delivery-return'],             'teslimat-iade.php',                []],
    [['about'],                       'hakkimizda.php',                  []],
    [['contact'],                     'iletisim.php',                     []],

    [['admin', 'support'],            'admin/support.php',                []],
    [['api', 'posts', '*', 'like'],   'api/like.php',                      ['id']],
    [['api', 'users', 'search'],      'api/user_search.php',               []],
    [['api', 'live-chat'],            'api/live_chat.php',                   []],
    [['api', 'messages'],              'api/messages.php',                    []],
    [['api', 'heartbeat'],             'api/heartbeat.php',                   []],
    [['api', 'presence', '*'],           'api/presence.php',                  ['username']],
    [['api', 'native', 'login'],         'api/native/login.php',              []],
    [['api', 'native', 'me'],            'api/native/me.php',                 []],
    [['api', 'native', 'forum'],         'api/native/forum.php',              []],
    [['api', 'native', 'messages'],      'api/native/messages.php',           []],
    [['api', 'native', 'profile'],       'api/native/profile.php',            []],
    [['api', 'native', 'notifications'], 'api/native/notifications.php',      []],
];

$matched = null;
$params = [];

// /@kullaniciadi -> profil sayfasi
if (
    count($segments) === 1 &&
    isset($segments[0][0]) &&
    $segments[0][0] === '@' &&
    strlen($segments[0]) > 1
) {
    $matched = 'profile.php';
    $params = ['username' => substr($segments[0], 1)];
}

// Eski /user/kullaniciadi linkleri -> yeni /@kullaniciadi adresine kalici yonlendirme
if (
    $matched === null &&
    count($segments) === 2 &&
    $segments[0] === 'user' &&
    $segments[1] !== ''
) {
    header('Location: ' . base_url('@' . $segments[1]), true, 301);
    exit;
}

if ($matched === null) {
    foreach ($routes as [$pattern, $file, $paramKeys]) {
        if (count($pattern) !== count($segments)) {
            continue;
        }

        $ok = true;
        $captured = [];

        foreach ($pattern as $i => $part) {
            if ($part === '*') {
                $captured[] = $segments[$i];
            } elseif ($part !== $segments[$i]) {
                $ok = false;
                break;
            }
        }

        if ($ok) {
            $matched = $file;
            $params = array_combine($paramKeys, $captured) ?: [];
            break;
        }
    }
}

if ($matched === null) {
    http_response_code(404);
    require $pagesDir . '/errors/404.php';
    exit;
}

foreach ($params as $key => $value) {
    ${$key} = $value;
}

$pageFile = $pagesDir . '/' . $matched;

if (!is_file($pageFile)) {
    http_response_code(404);
    require $pagesDir . '/errors/404.php';
    exit;
}

require $pageFile;
