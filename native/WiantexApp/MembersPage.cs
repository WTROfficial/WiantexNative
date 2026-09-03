namespace WiantexApp;

public class MembersPage : BaseContentPage
{
    public MembersPage() : base("Üyeler")
    {
        var users = new[] { "elation", "ysfacar", "MertBaharcik", "CratOS", "BOGAbg" };
        var cv = new CollectionView
        {
            ItemsSource = users,
            SelectionMode = SelectionMode.None,
            ItemTemplate = new DataTemplate(() =>
            {
                var avatar = new Ellipse { WidthRequest = 42, HeightRequest = 42, Fill = Color.FromArgb("#7658FF") };
                var label = new Label { VerticalTextAlignment = TextAlignment.Center, FontAttributes = FontAttributes.Bold };
                label.SetBinding(Label.TextProperty, ".");
                return new Border { Padding = 13, Margin = new Thickness(0,0,0,8), Stroke = Color.FromArgb("#2A2544"), BackgroundColor = Color.FromArgb("#131126"), StrokeShape = new RoundRectangle { CornerRadius = 14 }, Content = new HorizontalStackLayout { Spacing = 12, Children = { avatar, label } } };
            })
        };
        Content = new VerticalStackLayout { Padding = 18, Children = { H("Üyeler"), M("Wiantex topluluğu"), cv } };
    }
}
