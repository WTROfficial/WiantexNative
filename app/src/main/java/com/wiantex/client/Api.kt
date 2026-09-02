package com.wiantex.client

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class Api(context: Context) {
    private val prefs = context.getSharedPreferences("wiantex", Context.MODE_PRIVATE)

    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) = saveCookies(cookies)
        override fun loadForRequest(url: HttpUrl): List<Cookie> = loadCookies().filter { it.matches(url) }
    }

    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val base = "https://www.wiantex.com/"

    private fun saveCookies(cookies: List<Cookie>) {
        val existing = loadCookies().associateBy { "${it.domain}|${it.path}|${it.name}" }.toMutableMap()
        cookies.forEach { existing["${it.domain}|${it.path}|${it.name}"] = it }
        val array = JSONArray()
        existing.values.forEach { cookie ->
            array.put(
                JSONObject()
                    .put("name", cookie.name)
                    .put("value", cookie.value)
                    .put("domain", cookie.domain)
                    .put("path", cookie.path)
                    .put("expires", cookie.expiresAt)
                    .put("secure", cookie.secure)
                    .put("httpOnly", cookie.httpOnly)
            )
        }
        prefs.edit().putString("cookies", array.toString()).apply()
    }

    private fun loadCookies(): List<Cookie> = try {
        val array = JSONArray(prefs.getString("cookies", "[]"))
        (0 until array.length()).mapNotNull { index ->
            runCatching {
                val o = array.getJSONObject(index)
                Cookie.Builder()
                    .name(o.getString("name"))
                    .value(o.getString("value"))
                    .domain(o.getString("domain"))
                    .path(o.optString("path", "/"))
                    .expiresAt(o.optLong("expires", Long.MAX_VALUE))
                    .apply { if (o.optBoolean("secure")) secure() }
                    .apply { if (o.optBoolean("httpOnly")) httpOnly() }
                    .build()
            }.getOrNull()
        }
    } catch (_: Exception) {
        emptyList()
    }

    private suspend fun request(method: String, path: String, body: String? = null): JSONObject =
        withContext(Dispatchers.IO) {
            val requestBuilder = Request.Builder()
                .url(base + path)
                .header("Accept", "application/json")
                .header("User-Agent", "WiantexAndroid/1.0")

            if (method == "POST") {
                requestBuilder.post(
                    (body ?: "{}").toRequestBody("application/json; charset=utf-8".toMediaType())
                )
            } else {
                requestBuilder.get()
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val message = runCatching { JSONObject(responseText).optString("message") }
                        .getOrDefault(responseText)
                    throw Exception("HTTP ${response.code}: ${message.ifBlank { responseText }}")
                }
                JSONObject(responseText)
            }
        }

    suspend fun login(login: String, password: String) = request(
        "POST",
        "api/native/login",
        JSONObject().put("login", login).put("password", password).toString()
    )

    suspend fun forum(page: Int = 1) = request("GET", "api/native/forum?page=$page")

    suspend fun messages(username: String) = request(
        "GET",
        "api/native/messages?username=${URLEncoder.encode(username, "UTF-8")}"
    )

    suspend fun sendMessage(username: String, content: String, csrf: String) = request(
        "POST",
        "api/native/messages",
        JSONObject()
            .put("username", username)
            .put("content", content)
            .put("csrf_token", csrf)
            .toString()
    )

    suspend fun profile(username: String? = null): JSONObject = request(
        "GET",
        if (username.isNullOrBlank()) {
            "api/native/profile"
        } else {
            "api/native/profile?username=${URLEncoder.encode(username, "UTF-8")}"
        }
    )

    suspend fun notifications() = request("GET", "api/native/notifications")
    suspend fun me() = request("GET", "api/native/me")

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun saveSession(username: String, csrf: String) {
        prefs.edit().putString("username", username).putString("csrf", csrf).apply()
    }

    fun username(): String? = prefs.getString("username", null)
    fun csrf(): String = prefs.getString("csrf", "") ?: ""
}
