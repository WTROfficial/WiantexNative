namespace WiantexApp;
public class HomePage:BaseContentPage
{ public HomePage():base("Ana Sayfa"){Content=new ScrollView{Content=new VerticalStackLayout{Padding=18,Spacing=8,Children={H("Wiantex"),M("Forumun yerli, hızlı ve modern hali."),Card(new VerticalStackLayout{Children={new Label{Text="Son konular",FontAttributes=FontAttributes.Bold,FontSize=17},M("Forum içerikleri burada listelenecek.")}}),Card(new VerticalStackLayout{Children={new Label{Text="Popüler",FontAttributes=FontAttributes.Bold,FontSize=17},M("Popüler başlıklar ve son mesajlar.")}})}}};} }
