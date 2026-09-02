using System.Net;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
namespace WiantexApp.Services;

public sealed class ApiClient
{
    private readonly CookieContainer _cookies = new();
    private readonly HttpClient _http;
    private static readonly JsonSerializerOptions JsonOptions = new(JsonSerializerDefaults.Web);
    public ApiClient()
    {
        var handler = new HttpClientHandler { AutomaticDecompression = DecompressionMethods.All, UseCookies = true, CookieContainer = _cookies };
        _http = new HttpClient(handler) { BaseAddress = new Uri("https://www.wiantex.com/"), Timeout = TimeSpan.FromSeconds(20) };
        _http.DefaultRequestHeaders.UserAgent.ParseAdd("WiantexNative/1.1");
        _http.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
    }
    private async Task<JsonDocument> SendAsync(HttpRequestMessage req, CancellationToken ct)
    {
        using var r = await _http.SendAsync(req, HttpCompletionOption.ResponseHeadersRead, ct);
        var text = await r.Content.ReadAsStringAsync(ct);
        if (!r.IsSuccessStatusCode) throw new HttpRequestException($"HTTP {(int)r.StatusCode}: {text}");
        return JsonDocument.Parse(text);
    }
    public async Task<JsonDocument> GetAsync(string path, CancellationToken ct=default) => await SendAsync(new HttpRequestMessage(HttpMethod.Get,path),ct);
    public async Task<JsonDocument> LoginAsync(string login,string password,CancellationToken ct=default)
    {
        using var req=new HttpRequestMessage(HttpMethod.Post,"api/native/login");
        req.Content=new StringContent(JsonSerializer.Serialize(new {login,password},JsonOptions),Encoding.UTF8,"application/json");
        return await SendAsync(req,ct);
    }
    public async Task<JsonDocument> SendMessageAsync(string username,string content,string csrfToken,CancellationToken ct=default)
    {
        using var req=new HttpRequestMessage(HttpMethod.Post,"api/native/messages");
        req.Content=new StringContent(JsonSerializer.Serialize(new {username,content,csrf_token=csrfToken},JsonOptions),Encoding.UTF8,"application/json");
        return await SendAsync(req,ct);
    }
    public Task<JsonDocument> MeAsync(CancellationToken ct=default)=>GetAsync("api/native/me",ct);
    public Task<JsonDocument> ForumAsync(int page=1,CancellationToken ct=default)=>GetAsync($"api/native/forum?page={page}",ct);
    public Task<JsonDocument> MessagesAsync(string username,CancellationToken ct=default)=>GetAsync($"api/native/messages?username={Uri.EscapeDataString(username)}",ct);
    public Task<JsonDocument> ProfileAsync(string? username=null,CancellationToken ct=default)=>GetAsync("api/native/profile"+(string.IsNullOrWhiteSpace(username)?"":"?username="+Uri.EscapeDataString(username)),ct);
    public Task<JsonDocument> NotificationsAsync(CancellationToken ct=default)=>GetAsync("api/native/notifications",ct);
}
