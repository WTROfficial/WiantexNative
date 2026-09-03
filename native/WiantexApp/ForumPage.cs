using WiantexApp.Services;

namespace WiantexApp;

public class ForumPage : BaseContentPage
{
    readonly CollectionView list = new() { SelectionMode = SelectionMode.None, RemainingItemsThreshold = 3 };
    readonly Label status = new();
    int page = 1;

    public ForumPage() : base("Forum")
    {
        // Custom cell for anonymous objects through runtime bindings.
        list.ItemTemplate = new DataTemplate(() =>
        {
            var title = new Label { FontSize = 16, FontAttributes = FontAttributes.Bold };
            title.SetBinding(Label.TextProperty, "title");
            var meta = new Label { FontSize = 12, TextColor = Color.FromArgb("#9D97B5") };
            meta.SetBinding(Label.TextProperty, "meta");
            return new Border
            {
                Padding = 15,
                Margin = new Thickness(0, 0, 0, 9),
                BackgroundColor = Color.FromArgb("#131126"),
                Stroke = Color.FromArgb("#2A2544"),
                StrokeShape = new RoundRectangle { CornerRadius = 14 },
                Content = new VerticalStackLayout { Spacing = 5, Children = { title, meta } }
            };
        });

        var refresh = Secondary("Yenile");
        refresh.Clicked += async (_, _) => { page = 1; await Load(reset: true); };
        list.RemainingItemsThresholdReached += async (_, _) => await Load();

        Content = new VerticalStackLayout
        {
            Padding = 18,
            Children = { H("Forum"), M("Güncel Wiantex konuları"), new HorizontalStackLayout { Spacing = 8, Children = { refresh, status } }, list }
        };
        Loaded += async (_, _) => await Load(reset: true);
    }

    async Task Load(bool reset = false)
    {
        try
        {
            if (reset) list.ItemsSource = null;
            using var doc = await AppState.Api.ForumAsync(page);
            var items = new List<object>();
            foreach (var t in doc.RootElement.GetProperty("topics").EnumerateArray())
            {
                items.Add(new
                {
                    title = t.GetProperty("title").GetString() ?? "Konu",
                    meta = $"{t.GetProperty("username").GetString() ?? "Wiantex"} · {t.GetProperty("category_name").GetString() ?? "Genel"}"
                });
            }
            if (reset) list.ItemsSource = items;
            else
            {
                var current = (list.ItemsSource as System.Collections.IEnumerable)?.Cast<object>().ToList() ?? new List<object>();
                current.AddRange(items);
                list.ItemsSource = current;
            }
            status.Text = items.Count == 0 ? "Son sayfa" : $"Sayfa {page}";
            if (items.Count > 0) page++;
        }
        catch (Exception ex)
        {
            status.Text = ex.Message;
        }
    }
}
