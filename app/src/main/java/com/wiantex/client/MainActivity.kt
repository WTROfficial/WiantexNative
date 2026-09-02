package com.wiantex.client

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var api: Api
    private val bgColor = Color.rgb(9, 8, 20)
    private val purple = Color.rgb(96, 71, 215)
    private var csrf = ""
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = Api(this)
        username = api.username()
        csrf = api.csrf()
        show(if (username.isNullOrBlank()) "login" else "home")
    }

    private fun base(title: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor)
            setPadding(18, 18, 18, 18)

            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 26f
                setTextColor(Color.WHITE)
                setPadding(0, 0, 0, 16)
            })
        }
    }

    private fun label(value: String, size: Float = 14f): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(Color.LTGRAY)
            setPadding(0, 6, 0, 6)
        }
    }

    private fun button(title: String, action: () -> Unit): MaterialButton {
        return MaterialButton(this).apply {
            text = title
            setTextColor(Color.WHITE)
            setBackgroundColor(purple)
            setOnClickListener { action() }
        }
    }

    private fun scroll(): ScrollView {
        return ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
    }

    private fun show(page: String) {
        when (page) {
            "login" -> login()
            "home" -> home()
            "forum" -> forum()
            "messages" -> messages()
            "notifications" -> notifications()
            "profile" -> profile()
        }
    }

    private fun login() {
        val layout = base("Wiantex")
        layout.addView(label("Native Android Client", 14f))

        val usernameInput = EditText(this).apply {
            hint = "Kullanıcı adı veya e-posta"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            setSingleLine(true)
        }
        val passwordInput = EditText(this).apply {
            hint = "Şifre"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            setSingleLine(true)
        }
        val status = label("")

        layout.addView(usernameInput)
        layout.addView(passwordInput)
        layout.addView(button("Giriş Yap") {
            lifecycleScope.launch {
                try {
                    val result = api.login(
                        usernameInput.text.toString().trim(),
                        passwordInput.text.toString()
                    )
                    if (result.optBoolean("ok")) {
                        val user = result.getJSONObject("user")
                        username = user.optString("username")
                        csrf = result.optString("csrf_token")
                        api.saveSession(username.orEmpty(), csrf)
                        show("home")
                    } else {
                        status.text = result.optString("message", "Giriş başarısız")
                    }
                } catch (e: Exception) {
                    status.text = e.message ?: "Bağlantı hatası"
                }
            }
        })
        layout.addView(status)
        setContentView(layout)
    }

    private fun navButtons(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            val pages = listOf(
                "Ana Sayfa" to "home",
                "Forum" to "forum",
                "Mesaj" to "messages",
                "Bildirim" to "notifications",
                "Profil" to "profile"
            )
            pages.forEach { (title, page) ->
                val b = button(title) { show(page) }
                b.textSize = 11f
                addView(b, LinearLayout.LayoutParams(0, 58, 1f))
            }
        }
    }

    private fun attachNav(layout: LinearLayout) {
        layout.addView(navButtons(), LinearLayout.LayoutParams.MATCH_PARENT, 64)
    }

    private fun home() {
        val layout = base("Ana Sayfa")
        layout.addView(label("Hoş geldin, ${username ?: "Wiantex"}", 18f))
        layout.addView(label("Forum, mesajlar, bildirimler ve profil tek uygulamada."))
        layout.addView(button("Forum") { show("forum") })
        layout.addView(button("Profilim") { show("profile") })
        attachNav(layout)
        setContentView(layout)
    }

    private fun forum() {
        val layout = base("Forum")
        val scrollView = scroll()
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scrollView.addView(content)
        layout.addView(scrollView)
        attachNav(layout)
        setContentView(layout)

        lifecycleScope.launch {
            try {
                val topics = api.forum().getJSONArray("topics")
                for (i in 0 until topics.length()) {
                    val topic = topics.getJSONObject(i)
                    content.addView(
                        label(
                            "${topic.optString("title")}\n${topic.optString("username")} · ${topic.optString("category_name")}",
                            16f
                        )
                    )
                }
            } catch (e: Exception) {
                content.addView(label(e.message ?: "Hata"))
            }
        }
    }

    private fun messages() {
        val layout = base("Mesajlar")
        val target = EditText(this).apply {
            hint = "Kullanıcı adı"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            setSingleLine(true)
        }
        val body = EditText(this).apply {
            hint = "Mesaj yaz…"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            minLines = 3
        }
        val scrollView = scroll()
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scrollView.addView(content)

        layout.addView(target)
        layout.addView(button("Konuşmayı Aç") { loadMessages(target, content) })
        layout.addView(scrollView)
        layout.addView(body)
        layout.addView(button("Gönder") {
            val targetName = target.text.toString().trim()
            val message = body.text.toString().trim()
            if (targetName.isBlank() || message.isBlank()) return@button

            lifecycleScope.launch {
                try {
                    api.sendMessage(targetName, message, csrf)
                    body.setText("")
                    loadMessages(target, content)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        e.message ?: "Mesaj gönderilemedi",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
        attachNav(layout)
        setContentView(layout)
    }

    private fun loadMessages(target: EditText, content: LinearLayout) {
        val targetName = target.text.toString().trim()
        if (targetName.isBlank()) return

        lifecycleScope.launch {
            try {
                val messages = api.messages(targetName).getJSONArray("messages")
                content.removeAllViews()
                for (i in 0 until messages.length()) {
                    val message = messages.getJSONObject(i)
                    content.addView(
                        label(
                            "${message.optString("sender_username")}: ${message.optString("content")}",
                            15f
                        )
                    )
                }
            } catch (e: Exception) {
                content.removeAllViews()
                content.addView(label(e.message ?: "Hata"))
            }
        }
    }

    private fun notifications() {
        val layout = base("Bildirimler")
        val scrollView = scroll()
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scrollView.addView(content)
        layout.addView(scrollView)
        attachNav(layout)
        setContentView(layout)

        lifecycleScope.launch {
            try {
                val result = api.notifications()
                content.addView(label("Okunmamış: ${result.optInt("unread_count")}", 18f))
                val notifications = result.getJSONArray("notifications")
                for (i in 0 until notifications.length()) {
                    val notification = notifications.getJSONObject(i)
                    content.addView(
                        label(
                            "${notification.optString("actor_username", "Wiantex")} · ${notification.optString("type", "Bildirim")}",
                            15f
                        )
                    )
                }
            } catch (e: Exception) {
                content.addView(label(e.message ?: "Hata"))
            }
        }
    }

    private fun profile() {
        val layout = base("Profil")
        val lookup = EditText(this).apply {
            hint = "Kullanıcı adı (opsiyonel)"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            setSingleLine(true)
        }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        layout.addView(lookup)
        layout.addView(button("Profili Aç") { loadProfile(lookup.text.toString(), content) })
        layout.addView(content, LinearLayout.LayoutParams.MATCH_PARENT, 0).apply {
            (this as LinearLayout.LayoutParams).weight = 1f
        }
        layout.addView(button("Çıkış Yap") {
            api.clear()
            username = null
            csrf = ""
            show("login")
        })
        attachNav(layout)
        setContentView(layout)
        loadProfile("", content)
    }

    private fun loadProfile(name: String, content: LinearLayout) {
        lifecycleScope.launch {
            try {
                val profile = api.profile(name.ifBlank { null }).getJSONObject("profile")
                content.removeAllViews()
                content.addView(label(profile.optString("username"), 24f))
                content.addView(label(profile.optString("role_name", "Üye"), 16f))
                content.addView(label(profile.optString("bio", "")))
                content.addView(
                    label(
                        "Konular ${profile.optInt("topic_count")} · " +
                            "Mesajlar ${profile.optInt("post_count")} · " +
                            "Beğeniler ${profile.optInt("like_count")}"
                    )
                )
            } catch (e: Exception) {
                content.removeAllViews()
                content.addView(label(e.message ?: "Hata"))
            }
        }
    }
}
