using Microsoft.Maui.Controls.Shapes;

namespace WiantexApp;

public abstract class BaseContentPage : ContentPage
{
    protected BaseContentPage(string title)
    {
        Title = title;
        BackgroundColor = Color.FromArgb("#090814");
        Padding = 0;
    }

    protected Border Card(View content, Thickness? margin = null)
    {
        return new Border
        {
            Stroke = Color.FromArgb("#2A2544"),
            StrokeThickness = 1,
            BackgroundColor = Color.FromArgb("#131126"),
            Padding = 16,
            Margin = margin ?? new Thickness(0, 0, 0, 12),
            StrokeShape = new RoundRectangle { CornerRadius = 16 },
            Content = content
        };
    }

    protected Label H(string text) => new()
    {
        Text = text,
        FontSize = 24,
        FontAttributes = FontAttributes.Bold,
        Margin = new Thickness(0, 2, 0, 2)
    };

    protected Label M(string text) => new()
    {
        Text = text,
        FontSize = 14,
        TextColor = Color.FromArgb("#9D97B5")
    };

    protected Label Kicker(string text) => new()
    {
        Text = text.ToUpperInvariant(),
        FontSize = 11,
        CharacterSpacing = 1.2,
        FontAttributes = FontAttributes.Bold,
        TextColor = Color.FromArgb("#31C8FF")
    };

    protected Button Primary(string text) => new() { Text = text, BackgroundColor = Color.FromArgb("#7658FF") };

    protected Button Secondary(string text) => new() { Text = text, BackgroundColor = Color.FromArgb("#201B38"), BorderColor = Color.FromArgb("#36305A"), BorderWidth = 1 };
}
