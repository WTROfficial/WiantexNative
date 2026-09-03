using WiantexApp.Services;

namespace WiantexApp;

public class NotificationsPage : BaseContentPage
{
    readonly CollectionView list = new() { SelectionMode = SelectionMode.None };
    readonly Label unread = new();

    public NotificationsPage() : base("Bildirimler")
    {
        list.ItemTemplate = new DataTemplate(() =>
        {
            var title = new Label { FontSize = 15, FontAttributes = FontAttributes.Bold };
            title.SetBinding(Label.TextProperty, "title");
            var meta = new Label { FontSize = 12, TextColor = Color.FromArgb("#9D97B5") };
            meta.SetBinding(Label.TextProperty, "meta");
            return new Border { Padding = 13, Margin = new Thickness(0,0,0,8), BackgroundColor = Color.FromArgb("#131126"), Stroke = Color.FromArgb("#2A2544"), StrokeShape = new RoundRectangle { CornerRadius = 14 }, Content = new VerticalStackLayout { Spacing = 4, Children = { title, meta } } };
        });
        var refresh = Secondary("Yenile");
        refresh.Clicked += async (_, _) => await Load();
        Content = new VerticalStackLayout { Padding = 18, Children = { H("Bildirimler"), unread, new HorizontalStackLayout { Children = { refresh } }, list } };
        Loaded += async (_, _) => await Load();
    }

    async Task Load()
    {
        try
        {
            using var doc = await AppState.Api.NotificationsAsync();
            var root = doc.RootElement;
            unread.Text = $"Okunmamış: {root.GetProperty("unread_count").GetInt32()}";
            var items = new List<object>();
            foreach (var n in root.GetProperty("notifications").EnumerateArray())
            {
                items.Add(new
                {
                    title = n.GetProperty("actor_username").GetString() ?? "Wiantex",
                    meta = n.GetProperty("type").GetString() ?? "Bildirim"
                });
            }
            list.ItemsSource = items;
        }
        catch (Exception ex) { list.ItemsSource = new[] { new { title = "Bildirimler yüklenemedi", meta = ex.Message } }; }
    }
}
