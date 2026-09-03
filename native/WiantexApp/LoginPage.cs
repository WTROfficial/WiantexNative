using WiantexApp.Services;

namespace WiantexApp;

public class LoginPage : BaseContentPage
{
    public LoginPage()
    {
        Title = "Giriş";

        var login = new Entry { Placeholder = "Kullanıcı adı veya e-posta", ClearButtonVisibility = ClearButtonVisibility.WhileEditing };
        var password = new Entry { Placeholder = "Şifre", IsPassword = true };
        var remember = new Label { Text = "Wiantex hesabınla güvenli şekilde bağlan" , TextColor = Color.FromArgb("#9D97B5"), FontSize = 13 };
        var status = new Label { TextColor = Color.FromArgb("#FF6685"), IsVisible = false, FontSize = 13 };
        var button = Primary("Giriş Yap");
        var progress = new ActivityIndicator { IsVisible = false, IsRunning = false, Color = Color.FromArgb("#31C8FF") };

        button.Clicked += async (_, _) =>
        {
            if (string.IsNullOrWhiteSpace(login.Text) || string.IsNullOrWhiteSpace(password.Text))
            {
                status.Text = "Kullanıcı adı/e-posta ve şifre gerekli.";
                status.IsVisible = true;
                return;
            }

            status.IsVisible = false;
            button.IsEnabled = false;
            progress.IsVisible = progress.IsRunning = true;
            try
            {
                using var doc = await AppState.Api.LoginAsync(login.Text.Trim(), password.Text);
                var root = doc.RootElement;
                var user = root.GetProperty("user");
                AppState.IsAuthenticated = true;
                AppState.UserName = user.GetProperty("username").GetString();
                AppState.CsrfToken = root.GetProperty("csrf_token").GetString();
                Application.Current!.MainPage = new AppShell();
            }
            catch (Exception ex)
            {
                status.Text = ex.Message;
                status.IsVisible = true;
            }
            finally
            {
                button.IsEnabled = true;
                progress.IsRunning = false;
                progress.IsVisible = false;
            }
        };

        Content = new Grid
        {
            RowDefinitions = { new RowDefinition(GridLength.Star), new RowDefinition(GridLength.Auto), new RowDefinition(GridLength.Star) },
            Padding = new Thickness(24),
            Children =
            {
                Card(new VerticalStackLayout
                {
                    Spacing = 14,
                    Children =
                    {
                        new Image { Source = "wiantex-logo.png", HeightRequest = 76, HorizontalOptions = LayoutOptions.Center },
                        Kicker("Wiantex Native Client"),
                        H("Hesabına giriş yap"),
                        M("Forum, mesajlar, bildirimler ve profil tek uygulamada."),
                        login,
                        password,
                        remember,
                        button,
                        progress,
                        status
                    }
                }),
            }
        };
        Grid.SetRow(Content, 1);
    }
}
