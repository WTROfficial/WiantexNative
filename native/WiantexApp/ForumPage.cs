using WiantexApp.Services;
namespace WiantexApp;
public class ForumPage:BaseContentPage
{
    readonly CollectionView list=new(){SelectionMode=SelectionMode.None};
    public ForumPage(){Title="Forum";list.ItemTemplate=new DataTemplate(()=>new Border{Padding=14,Margin=new Thickness(0,0,0,8),BackgroundColor=Color.FromArgb("#151222"),Stroke=Color.FromArgb("#28223D"),StrokeShape=new RoundRectangle{CornerRadius=14},Content=new VerticalStackLayout{Children={new Label{FontSize=16,FontAttributes=FontAttributes.Bold},new Label{FontSize=12,TextColor=Color.FromArgb("#9B95AF")}}}});Content=new VerticalStackLayout{Padding=18,Children={H("Forum"),M("Güncel konular"),list}};Loaded+=async(_,_)=>await Load();}
    async Task Load(){try{using var d=await AppState.Api.ForumAsync();var items=new List<object>();foreach(var t in d.RootElement.GetProperty("topics").EnumerateArray())items.Add(new {title=t.GetProperty("title").GetString(),meta=$"{t.GetProperty("username").GetString()} · {t.GetProperty("category_name").GetString()}"});list.ItemsSource=items;}catch(Exception ex){list.ItemsSource=new[]{new{title="Forum yüklenemedi",meta=ex.Message}};}}
}
