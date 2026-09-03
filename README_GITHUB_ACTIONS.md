# GitHub Actions

Repository kökünde `.github/workflows/build.yml` bulunur.

GitHub → Actions → Build Wiantex Native → Run workflow

Başarılı bir çalıştırmadan sonra iki artifact beklenir:
- Wiantex-Android-APK
- Wiantex-Windows-x64

Not: Bu ortamda .NET/MAUI SDK kurulu olmadığı için yerel derleme yapılmamıştır. Workflow, GitHub runner üzerinde derleme yapar.
