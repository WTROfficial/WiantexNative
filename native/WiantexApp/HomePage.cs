using System.Text.Json;
using WiantexApp.Services;

namespace WiantexApp;

public class HomePage : BaseContentPage
{
    readonly Label welcome = new();
    readonly Label topicCount = new();
    readonly VerticalStackLayout recent = new() { Spacing = 8 };

    public HomePage() : base("Ana Sayfa")
    {
        var forumBtn = Secondary("Foruma Git");
        forumBtn.Clicked += async (_, _) => await Shell.Current.GoToAsync("//forum");
        var profileBtn = Secondary("Profilim");
        profileBtn.Clicked += async (_, _) => await Shell.Current.GoToAsync("//profile");
        var refresh = Secondary("Yenile");
        refresh.Clicked += async (_, _) => await Load();

        welcome.Text = "Hoş geldin";
        welcome.FontSize = 17;
        welcome.FontAttributes = FontAttributes.Bold;
        topicCount.TextColor = Color.FromArgb("#9D97B5");

        Content = new ScrollView
        {
            Content = new VerticalStackLayout
            {
                Padding = 18,
                Spacing = 10,
                Children =
                {
                    Kicker("Wiantex"), H("Ana Sayfa"), welcome,
                    Card(new VerticalStackLayout
                    {
                        Spacing = 8,
                        Children = { M("TOPLULUK"), topicCount, new HorizontalStackLayout { Spacing = 8, Children = { forumBtn, profileBtn, refresh } } }
                    }),
                    Kicker("Son Konular"),
                    Card(recent)
                }
            }
        };
        Loaded += async (_, _) => await Load();
    }

    async Task Load()
    {
        try
        {
            if (!AppState.IsAuthenticated)
            {
                recent.Children.Clear();
                recent.Children.Add(M("Giriş yaptıktan sonra topluluk akışını görebilirsin."));
                return;
            }

            welcome.Text = $"Hoş geldin, {AppState.UserName}";
            using var doc = await AppState.Api.ForumAsync();
            var root = doc.RootElement;
            var total = root.GetProperty("total").GetInt32();
            topicCount.Text = $"Toplam {total:N0} konu ve güncel topluluk akışı burada.";
            recent.Children.Clear();

            foreach (var t in root.GetProperty("topics").EnumerateArray().Take(5))
            {
                var title = t.GetProperty("title").GetString() ?? "Konu";
                var meta = $"{t.GetProperty("username").GetString()} · {t.GetProperty("category_name").GetString()}";
                recent.Children.Add(new VerticalStackLayout
                {
                    Spacing = 2,
                    Children = { new Label { Text = title, FontSize = 16, FontAttributes = FontAttributes.Bold }, M(meta) }
                });
            }
        }
        catch (Exception ex)
        {
            recent.Children.Clear();
            recent.Children.Add(M($"Bağlantı kurulamadı: {ex.Message}"));
        }
    }
}
