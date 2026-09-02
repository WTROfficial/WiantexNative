using WiantexApp.Services;
namespace WiantexApp;
public class LoginPage : BaseContentPage
{
    public LoginPage(){
        Title="Giriş";
        var login=new Entry{Placeholder="Kullanıcı adı veya e-posta",ClearButtonVisibility=ClearButtonVisibility.WhileEditing};
        var pass=new Entry{Placeholder="Şifre",IsPassword=true};
        var status=new Label{TextColor=Color.FromArgb("#FF7A90"),IsVisible=false};
        var button=new Button{Text="Giriş Yap"};
        button.Clicked += async (_,_)=>{
            status.IsVisible=false; button.IsEnabled=false;
            try{using var d=await AppState.Api.LoginAsync(login.Text??"",pass.Text??""); var root=d.RootElement; AppState.IsAuthenticated=true; AppState.UserName=root.GetProperty("user").GetProperty("username").GetString(); AppState.CsrfToken=root.GetProperty("csrf_token").GetString(); await Shell.Current.GoToAsync("//home");}
            catch(Exception ex){status.Text=ex.Message;status.IsVisible=true;} finally{button.IsEnabled=true;}
        };
        Content=new ScrollView{Content=new VerticalStackLayout{Padding=24,Spacing=10,Children={H("Wiantex"),M("Hesabınla giriş yap"),Card(new VerticalStackLayout{Spacing=4,Children={login,pass,button,status}})}}};
    }
}
