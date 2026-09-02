namespace WiantexApp.Services;
public static class AppState
{
    public static ApiClient Api { get; } = new();
    public static bool IsAuthenticated { get; set; }
    public static string? UserName { get; set; }
    public static string? CsrfToken { get; set; }
    public static void Clear(){ IsAuthenticated=false; UserName=null; CsrfToken=null; }
}
