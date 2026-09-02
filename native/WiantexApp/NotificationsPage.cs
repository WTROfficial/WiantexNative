using WiantexApp.Services;
using System.Text.Json;
namespace WiantexApp;
public class NotificationsPage:BaseContentPage
{
    readonly CollectionView list=new(){SelectionMode=SelectionMode.None};
    public NotificationsPage(){Title="Bildirimler"; list.ItemTemplate=new DataTemplate(()=>new Border{Padding=12,Margin=new Thickness(0,0,0,8),BackgroundColor=Color.FromArgb("#151222"),Stroke=Color.FromArgb("#28223D"),StrokeShape=new RoundRectangle{CornerRadius=14},Content=new Label{TextColor=Color.FromArgb("#F7F5FF")}}); Content=new VerticalStackLayout{Padding=18,Children={H("Bildirimler"),list}}; Loaded+=async(_,_)=>await Load();}
    async Task Load(){try{using var d=await AppState.Api.NotificationsAsync();var items=new List<string>();foreach(var n in d.RootElement.GetProperty("notifications").EnumerateArray())items.Add($"{n.GetProperty("actor_username").GetString() ?? "Wiantex"} · {n.GetProperty("type").GetString()}");list.ItemsSource=items;}catch(Exception ex){list.ItemsSource=new[]{ex.Message};}}
}
