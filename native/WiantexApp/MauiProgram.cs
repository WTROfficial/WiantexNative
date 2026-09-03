using Microsoft.Extensions.Logging;
namespace WiantexApp;
public static class MauiProgram
{ public static MauiApp CreateMauiApp(){var b=MauiApp.CreateBuilder();b.UseMauiApp<App>();#if DEBUG b.Logging.AddDebug();#endif return b.Build();} }
