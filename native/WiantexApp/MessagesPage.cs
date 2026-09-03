using WiantexApp.Services;

namespace WiantexApp;

public class MessagesPage : BaseContentPage
{
    readonly Entry target = new() { Placeholder = "Kullanıcı adı" };
    readonly Editor input = new() { Placeholder = "Mesaj yaz…", AutoSize = EditorAutoSizeOption.TextChanges, HeightRequest = 66 };
    readonly VerticalStackLayout messages = new() { Spacing = 8 };
    readonly ScrollView scroller;

    public MessagesPage() : base("Mesajlar")
    {
        var open = Primary("Konuşmayı Aç");
        open.Clicked += async (_, _) => await Load();
        var send = Primary("Gönder");
        send.Clicked += async (_, _) => await Send();
        var refresh = Secondary("Yenile");
        refresh.Clicked += async (_, _) => await Load();
        target.Completed += async (_, _) => await Load();

        scroller = new ScrollView { Content = messages, VerticalOptions = LayoutOptions.FillAndExpand };

        Content = new Grid
        {
            Padding = 18,
            RowDefinitions = { new RowDefinition(GridLength.Auto), new RowDefinition(GridLength.Star), new RowDefinition(GridLength.Auto) },
            Children =
            {
                new VerticalStackLayout { Spacing = 8, Children = { H("Mesajlar"), M("Kullanıcı adıyla konuşma başlat"), target, new HorizontalStackLayout { Spacing = 8, Children = { open, refresh } } } },
                scroller,
                new Grid { ColumnDefinitions = { new ColumnDefinition(GridLength.Star), new ColumnDefinition(88) }, ColumnSpacing = 8, Children = { input, send } }
            }
        };
        Grid.SetRow(scroller, 1);
        Grid.SetRow(input, 2);
        Grid.SetColumn(send, 1); Grid.SetRow(send, 2);
    }

    async Task Load()
    {
        messages.Children.Clear();
        if (string.IsNullOrWhiteSpace(target.Text)) { messages.Children.Add(M("Önce bir kullanıcı adı yaz.")); return; }
        try
        {
            using var doc = await AppState.Api.MessagesAsync(target.Text.Trim());
            foreach (var m in doc.RootElement.GetProperty("messages").EnumerateArray())
            {
                var mine = m.GetProperty("mine").GetBoolean();
                var text = m.GetProperty("content").GetString() ?? "";
                var sender = m.GetProperty("sender_username").GetString() ?? "Wiantex";
                var bubble = new Border
                {
                    Padding = 11,
                    HorizontalOptions = mine ? LayoutOptions.End : LayoutOptions.Start,
                    BackgroundColor = mine ? Color.FromArgb("#6047D7") : Color.FromArgb("#15132A"),
                    Stroke = Colors.Transparent,
                    StrokeShape = new RoundRectangle { CornerRadius = 14 },
                    MaximumWidthRequest = 340,
                    Content = new VerticalStackLayout { Spacing = 3, Children = { new Label { Text = sender, FontSize = 11, TextColor = Color.FromArgb("#B8B1D1") }, new Label { Text = text, FontSize = 14 } } }
                };
                messages.Children.Add(bubble);
            }
            await scroller.ScrollToAsync(0, messages.Height, false);
        }
        catch (Exception ex) { messages.Children.Add(M($"Mesajlar yüklenemedi: {ex.Message}")); }
    }

    async Task Send()
    {
        if (string.IsNullOrWhiteSpace(target.Text) || string.IsNullOrWhiteSpace(input.Text)) return;
        try
        {
            await AppState.Api.SendMessageAsync(target.Text.Trim(), input.Text.Trim(), AppState.CsrfToken ?? "");
            input.Text = string.Empty;
            await Load();
        }
        catch (Exception ex)
        {
            await DisplayAlert("Mesaj gönderilemedi", ex.Message, "Tamam");
        }
    }
}
