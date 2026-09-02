package com.wiantex.client

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class Api(context: Context) {
    private val prefs = context.getSharedPreferences("wiantex", Context.MODE_PRIVATE)
    private val cookieJar = object: CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) { save(cookies) }
        override fun loadForRequest(url: HttpUrl): List<Cookie> = load().filter { it.matches(url) }
    }
    private val client = OkHttpClient.Builder().cookieJar(cookieJar).connectTimeout(20, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).build()
    private val base = "https://www.wiantex.com/"
    private fun save(cookies: List<Cookie>) { val old=load().associateBy{it.name}.toMutableMap(); cookies.forEach{old[it.name]=it}; val arr=org.json.JSONArray(); old.values.forEach{ val o=JSONObject(); o.put("name",it.name);o.put("value",it.value);o.put("domain",it.domain);o.put("path",it.path);o.put("expires",it.expiresAt);arr.put(o)}; prefs.edit().putString("cookies",arr.toString()).apply() }
    private fun load(): List<Cookie> { return try { val a=org.json.JSONArray(prefs.getString("cookies","[]")); (0 until a.length()).mapNotNull{val o=a.getJSONObject(it); Cookie.Builder().name(o.getString("name")).value(o.getString("value")).domain(o.getString("domain")).path(o.getString("path")).expiresAt(o.getLong("expires")).build()} } catch(_:Exception){ emptyList() } }
    private suspend fun request(method:String, path:String, body:String?=null): JSONObject = withContext(Dispatchers.IO) {
        val b=Request.Builder().url(base+path).header("Accept","application/json").header("User-Agent","WiantexAndroid/1.0")
        if(method=="POST") b.post((body?:"{}").toRequestBody("application/json; charset=utf-8".toMediaType())) else b.get()
        client.newCall(b.build()).execute().use { r -> val text=r.body?.string().orEmpty(); if(!r.isSuccessful) throw Exception("HTTP ${r.code}: ${try{JSONObject(text).optString("message",text)}catch(_:Exception){text}} "); JSONObject(text) }
    }
    suspend fun login(login:String,password:String)=request("POST","api/native/login",JSONObject().put("login",login).put("password",password).toString())
    suspend fun forum(page:Int=1)=request("GET","api/native/forum?page=$page")
    suspend fun messages(username:String)=request("GET","api/native/messages?username=${URLEncoder.encode(username,"UTF-8")}")
    suspend fun sendMessage(username:String,content:String,csrf:String)=request("POST","api/native/messages",JSONObject().put("username",username).put("content",content).put("csrf_token",csrf).toString())
    suspend fun profile(username:String?=null)=request("GET","api/native/profile${if(username.isNullOrBlank())"" else "?username=${URLEncoder.encode(username,"UTF-8")}"}")
    suspend fun notifications()=request("GET","api/native/notifications")
    suspend fun me()=request("GET","api/native/me")
    fun clear(){prefs.edit().remove("cookies").remove("csrf").remove("username").apply()}
    fun saveSession(username:String,csrf:String){prefs.edit().putString("username",username).putString("csrf",csrf).apply()}
    fun username()=prefs.getString("username",null)
    fun csrf()=prefs.getString("csrf","") ?: ""
}
