namespace WiantexApp;
public partial class AppShell : Shell
{
  public AppShell(){InitializeComponent(); Routing.RegisterRoute(nameof(HomePage), typeof(HomePage)); Routing.RegisterRoute(nameof(ForumPage), typeof(ForumPage)); Routing.RegisterRoute(nameof(MessagesPage), typeof(MessagesPage)); Routing.RegisterRoute(nameof(MembersPage), typeof(MembersPage)); Routing.RegisterRoute(nameof(ProfilePage), typeof(ProfilePage)); Routing.RegisterRoute(nameof(LoginPage), typeof(LoginPage)); Routing.RegisterRoute(nameof(NotificationsPage), typeof(NotificationsPage));}
}
