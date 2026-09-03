using WiantexApp.Services;

namespace WiantexApp;

public partial class App : Application
{
    public App()
    {
        InitializeComponent();
        MainPage = new LoginPage();
    }

    protected override async void OnStart()
    {
        base.OnStart();
        try
        {
            using var doc = await AppState.Api.MeAsync();
            var user = doc.RootElement.GetProperty("user");
            AppState.IsAuthenticated = true;
            AppState.UserName = user.GetProperty("username").GetString();
            AppState.CsrfToken = doc.RootElement.GetProperty("csrf_token").GetString();
            MainPage = new AppShell();
        }
        catch
        {
            MainPage = new LoginPage();
        }
    }
}
