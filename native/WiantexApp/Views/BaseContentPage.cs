using Microsoft.Maui.Controls.Shapes;
namespace WiantexApp;
public abstract class BaseContentPage : ContentPage
{
 protected BaseContentPage(string title){Title=title; BackgroundColor=Color.FromArgb("#0B0A14");}
 protected Border Card(View content){return new Border{Stroke=Color.FromArgb("#28223D"),StrokeThickness=1,Background=Brushes.Transparent,Padding=16,Margin=new Thickness(0,0,0,12),StrokeShape=new RoundRectangle{CornerRadius=16},Content=content};}
 protected Label H(string text)=>new(){Text=text,FontSize=22,FontAttributes=FontAttributes.Bold};
 protected Label M(string text)=>new(){Text=text,FontSize=14,TextColor=Color.FromArgb("#9B95AF")};
}
