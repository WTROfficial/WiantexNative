# Wiantex Native Android + Windows — Connected API Build

Native MAUI app (no WebView) connected to Wiantex JSON APIs for login, forum, messages, profile and notifications.

Backend patch:
- `/api/native/login`
- `/api/native/me`
- `/api/native/forum`
- `/api/native/messages`
- `/api/native/profile`
- `/api/native/notifications`

The PHP backend keeps the existing authentication/session and SQLite data. The native client persists the PHP session cookie in its `CookieContainer` and stores the CSRF token returned by login.

Build requirements: .NET 9 SDK + MAUI workloads on the developer machine, plus the normal Android/Windows build tooling.
