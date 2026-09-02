# GitHub Actions ile APK + Windows build

## 1) GitHub'a yükle

Bu klasörün içeriğini yeni bir GitHub repository'sine gönder.

## 2) Build başlat

GitHub repository → **Actions** → **Build Wiantex Native** → **Run workflow**.

Workflow iki ayrı çıktı üretir:

- **Wiantex-Android-APK** → `Wiantex.apk`
- **Wiantex-Windows-x64** → `Wiantex-Windows-x64.zip`

## 3) Dosyaları indir

Workflow tamamlandıktan sonra ilgili çalıştırmayı açıp **Artifacts** bölümünden dosyaları indir.

### Not

- Android build için GitHub'ın Ubuntu runner'ı kullanılır.
- Windows build için GitHub'ın Windows runner'ı kullanılır.
- Uygulamanın backend adresi `https://www.wiantex.com/` olarak kaynak kodda ayarlı.
- Android debug/test APK'sıdır; mağaza yayını için imzalama anahtarı ayrıca eklenmelidir.
