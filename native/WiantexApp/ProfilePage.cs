using WiantexApp.Services;

namespace WiantexApp;

public class ProfilePage : BaseContentPage
{
    readonly Entry lookup = new() { Placeholder = "Kullanıcı adı (opsiyonel)" };
    readonly Image avatar = new() { WidthRequest = 110, HeightRequest = 110, Aspect = Aspect.AspectFill };
    readonly Label name = new() { FontSize = 24, FontAttributes = FontAttributes.Bold, HorizontalTextAlignment = TextAlignment.Center };
    readonly Label role = new() { TextColor = Color.FromArgb("#31C8FF"), HorizontalTextAlignment = TextAlignment.Center };
    readonly Label bio = new() { TextColor = Color.FromArgb("#9D97B5"), HorizontalTextAlignment = TextAlignment.Center };
    readonly Label stats = new() { HorizontalTextAlignment = TextAlignment.Center };

    public ProfilePage() : base("Profil")
    {
        var load = Secondary("Profili Aç");
        load.Clicked += async (_, _) => await Load(lookup.Text);
        var logout = Secondary("Çıkış Yap");
        logout.Clicked += (_, _) =>
        {
            AppState.Api.ClearSession();
            AppState.Clear();
            Application.Current!.MainPage = new LoginPage();
        };

        Content = new ScrollView
        {
            Content = new VerticalStackLayout
            {
                Padding = 18,
                Spacing = 10,
                Children =
                {
                    H("Profil"), M("Kendi profilin veya başka bir kullanıcı"),
                    new HorizontalStackLayout { Spacing = 8, Children = { lookup, load } },
                    Card(new VerticalStackLayout { HorizontalOptions = LayoutOptions.Fill, Spacing = 8, Children = { avatar, name, role, bio, stats, logout } })
                }
            }
        };
        Loaded += async (_, _) => await Load(null);
    }

    async Task Load(string? username)
    {
        try
        {
            using var doc = await AppState.Api.ProfileAsync(string.IsNullOrWhiteSpace(username) ? null : username.Trim());
            var p = doc.RootElement.GetProperty("profile");
            name.Text = p.GetProperty("username").GetString() ?? "Wiantex";
            role.Text = p.GetProperty("role_name").GetString() ?? "Üye";
            bio.Text = p.GetProperty("bio").GetString() ?? "";
            stats.Text = $"Konular {p.GetProperty("topic_count").GetInt32()}  •  Mesajlar {p.GetProperty("post_count").GetInt32()}  •  Beğeniler {p.GetProperty("like_count").GetInt32()}";
            var path = p.GetProperty("avatar_path").GetString();
            avatar.Source = string.IsNullOrWhiteSpace(path) ? null : new UriImageSource { Uri = new Uri(new Uri("https://www.wiantex.com/"), path), CachingEnabled = true };
        }
        catch (Exception ex) { bio.Text = $"Profil yüklenemedi: {ex.Message}"; }
    }
}
