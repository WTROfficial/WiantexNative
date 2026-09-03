package com.wiantex.client

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
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
    private lateinit var contentHost: LinearLayout
    private var username: String? = null
    private var csrf: String = ""

    private val bg = Color.rgb(7, 8, 18)
    private val surface = Color.rgb(12, 16, 32)
    private val surface2 = Color.rgb(19, 23, 43)
    private val purple = Color.rgb(139, 92, 246)
    private val blue = Color.rgb(59, 130, 246)
    private val cyan = Color.rgb(56, 189, 248)
    private val pink = Color.rgb(192, 132, 252)
    private val text = Color.rgb(248, 247, 255)
    private val muted = Color.rgb(166, 166, 190)
    private val border = Color.rgb(44, 46, 72)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        api = Api(this)
        username = api.username()
        csrf = api.csrf()
        if (username.isNullOrBlank()) showLogin() else showHome()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun root(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(bg)
        setPadding(dp(16), dp(10), dp(16), 0)
    }

    private fun rounded(fill: Int, radius: Int = 18, stroke: Int? = null): GradientDrawable = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = dp(radius).toFloat()
        stroke?.let { setStroke(dp(1), it) }
    }

    private fun gradientBackground(start: Int, end: Int, radius: Int = 20): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(start, end)).apply {
            cornerRadius = dp(radius).toFloat()
        }

    private fun titleView(title: String, subtitle: String? = null): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(2), dp(10), dp(2), dp(14))
        addView(TextView(this@MainActivity).apply {
            text = title
            textSize = 28f
            setTextColor(text)
            typeface = Typeface.DEFAULT_BOLD
        })
        subtitle?.takeIf { it.isNotBlank() }?.let {
            addView(TextView(this@MainActivity).apply {
                text = it
                textSize = 13f
                setTextColor(muted)
                setPadding(0, dp(4), 0, 0)
            })
        }
    }

    private fun label(value: String, size: Float = 14f, color: Int = muted): TextView = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        setPadding(0, dp(3), 0, dp(3))
    }

    private fun input(hintValue: String, password: Boolean = false): EditText = EditText(this).apply {
        hint = hintValue
        textSize = 15f
        setTextColor(text)
        setHintTextColor(Color.rgb(117, 118, 142))
        setPadding(dp(14), 0, dp(14), 0)
        background = rounded(surface2, 15, border)
        minimumHeight = dp(52)
        if (password) inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        setSingleLine(true)
    }

    private fun actionButton(value: String, onClick: () -> Unit): MaterialButton = MaterialButton(this).apply {
        text = value
        textSize = 14f
        isAllCaps = false
        setTextColor(Color.WHITE)
        backgroundTintList = android.content.res.ColorStateList.valueOf(purple)
        cornerRadius = dp(15)
        insetTop = 0
        insetBottom = 0
        minHeight = dp(46)
        setOnClickListener { onClick() }
    }

    private fun ghostButton(value: String, onClick: () -> Unit): MaterialButton = MaterialButton(this).apply {
        text = value
        textSize = 13f
        isAllCaps = false
        setTextColor(text)
        backgroundTintList = android.content.res.ColorStateList.valueOf(surface2)
        cornerRadius = dp(14)
        strokeWidth = dp(1)
        strokeColor = android.content.res.ColorStateList.valueOf(border)
        insetTop = 0
        insetBottom = 0
        minHeight = dp(44)
        setOnClickListener { onClick() }
    }

    private fun cardView(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = rounded(surface, 20, border)
        setPadding(dp(16), dp(15), dp(16), dp(15))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(10)
        }
    }

    private fun logo(size: Int): ImageView = ImageView(this).apply {
        setImageResource(com.wiantex.client.R.drawable.wiantex_logo)
        adjustViewBounds = true
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        layoutParams = LinearLayout.LayoutParams(dp(size), dp(size))
    }

    private fun textButtonRow(vararg items: Pair<String, () -> Unit>): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        items.forEach { (caption, click) ->
            addView(ghostButton(caption, click), LinearLayout.LayoutParams(0, dp(44), 1f).apply {
                leftMargin = dp(3)
                rightMargin = dp(3)
            })
        }
    }

    private fun attachShell(base: LinearLayout, selected: String) {
        contentHost = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        }
        base.addView(contentHost)

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(13, 16, 31), 22, border)
            setPadding(dp(4), dp(5), dp(4), dp(7))
        }
        val pages = listOf(
            Triple("⌂", "Ana", "home"),
            Triple("▦", "Forum", "forum"),
            Triple("✉", "Mesaj", "messages"),
            Triple("♢", "Bildirim", "notifications"),
            Triple("◉", "Profil", "profile")
        )
        pages.forEach { (icon, caption, key) ->
            nav.addView(navItem(icon, caption, key == selected) { showPage(key) }, LinearLayout.LayoutParams(0, dp(68), 1f).apply {
                leftMargin = dp(2)
                rightMargin = dp(2)
            })
        }
        base.addView(nav, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(82)).apply {
            topMargin = dp(8)
            bottomMargin = dp(8)
        })
    }

    private fun navItem(icon: String, caption: String, selected: Boolean, click: () -> Unit): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        background = rounded(if (selected) Color.rgb(39, 27, 72) else Color.TRANSPARENT, 18)
        setPadding(dp(3), dp(5), dp(3), dp(5))
        addView(TextView(this@MainActivity).apply {
            text = icon
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(if (selected) cyan else muted)
        })
        addView(TextView(this@MainActivity).apply {
            text = caption
            textSize = 10.5f
            gravity = Gravity.CENTER
            setTextColor(if (selected) text else muted)
        })
        setOnClickListener { click() }
    }

    private fun showPage(page: String) {
        when (page) {
            "home" -> showHome()
            "forum" -> showForum()
            "messages" -> showMessages()
            "notifications" -> showNotifications()
            "profile" -> showProfile()
            else -> showHome()
        }
    }

    private fun showLogin() {
        val base = root()
        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(4), dp(32), dp(4), dp(28))
        }

        val logoWrap = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            addView(logo(104))
        }
        box.addView(logoWrap)
        box.addView(TextView(this).apply {
            text = "WIANTEX"
            textSize = 30f
            setTextColor(text)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, 0)
        })
        box.addView(label("CONNECT • SHARE • GROW", 11f, cyan).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(2), 0, dp(22))
        })

        val panel = cardView()
        panel.background = gradientBackground(Color.rgb(19, 16, 38), Color.rgb(12, 20, 39), 22)
        panel.addView(TextView(this).apply {
            text = "Wiantex'a hoş geldin"
            textSize = 20f
            setTextColor(text)
            typeface = Typeface.DEFAULT_BOLD
        })
        panel.addView(label("Hesabınla giriş yap ve topluluğa katıl.", 13f))

        val userInput = input("Kullanıcı adı veya e-posta")
        val passInput = input("Şifre", true)
        val status = label("", 12f, Color.rgb(255, 106, 126))
        panel.addView(userInput, marginParams())
        panel.addView(passInput, marginParams())
        panel.addView(actionButton("Giriş Yap") {
            lifecycleScope.launch {
                try {
                    val result = api.login(userInput.text.toString().trim(), passInput.text.toString())
                    if (result.optBoolean("ok")) {
                        val u = result.getJSONObject("user")
                        username = u.optString("username")
                        csrf = result.optString("csrf_token")
                        api.saveSession(username.orEmpty(), csrf)
                        showHome()
                    } else {
                        status.text = result.optString("message", "Giriş başarısız")
                    }
                } catch (e: Exception) {
                    status.text = e.message ?: "Bağlantı hatası"
                }
            }
        }, LinearLayout.LayoutParams.MATCH_PARENT, dp(48))
        panel.addView(status)
        box.addView(panel)
        scroll.addView(box)
        base.addView(scroll, LinearLayout.LayoutParams.MATCH_PARENT, 0)
        (scroll.layoutParams as LinearLayout.LayoutParams).weight = 1f
        setContentView(base)
    }

    private fun marginParams(top: Int = 7, bottom: Int = 7): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply {
            this.topMargin = dp(top)
            this.bottomMargin = dp(bottom)
        }

    private fun showHome() {
        val base = root()
        attachShell(base, "home")
        contentHost.addView(headerWithLogo("Ana Sayfa", "Wiantex topluluğuna hızlı erişim"))

        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)

        val hero = cardView().apply {
            background = gradientBackground(Color.rgb(32, 18, 65), Color.rgb(15, 31, 61), 22)
            val row = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val copy = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
            copy.addView(label("WIANTEX MOBILE", 10f, cyan))
            copy.addView(TextView(this@MainActivity).apply {
                text = "Hoş geldin, ${username ?: "WTR"}"
                textSize = 23f
                setTextColor(text)
                typeface = Typeface.DEFAULT_BOLD
                setPadding(0, dp(5), 0, dp(4))
            })
            copy.addView(label("Keşfet, paylaş ve toplulukla bağlantıda kal.", 13f, Color.rgb(224, 220, 238)))
            row.addView(copy)
            row.addView(logo(76))
            addView(row)
        }
        list.addView(hero)
        list.addView(label("HIZLI ERİŞİM", 11f, cyan).apply {
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(2), dp(5), 0, dp(8))
        })
        list.addView(featureCard("Forum", "Yeni konuları ve aktif tartışmaları keşfet.", "Foruma Git", "▦") { showForum() })
        list.addView(featureCard("Mesajlar", "Özel konuşmalarını aç ve mesaj gönder.", "Mesajlara Git", "✉") { showMessages() })
        list.addView(featureCard("Bildirimler", "Hesabındaki güncel hareketleri takip et.", "Bildirimleri Gör", "♢") { showNotifications() })
        contentHost.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0).apply { weight = 1f })
        setContentView(base)
    }

    private fun headerWithLogo(title: String, subtitle: String): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(8), 0, dp(10))
        addView(logo(44))
        val copy = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10), 0, 0, 0) }
        copy.addView(TextView(this@MainActivity).apply {
            text = title
            textSize = 25f
            setTextColor(text)
            typeface = Typeface.DEFAULT_BOLD
        })
        copy.addView(label(subtitle, 12f))
        addView(copy, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    }

    private fun featureCard(title: String, desc: String, action: String, icon: String, click: () -> Unit): LinearLayout = cardView().apply {
        val row = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        addView(TextView(this@MainActivity).apply {
            text = icon
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(cyan)
            background = rounded(Color.rgb(18, 31, 52), 15)
            layoutParams = LinearLayout.LayoutParams(dp(50), dp(50))
        })
        val copy = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, dp(8), 0); layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
        copy.addView(TextView(this@MainActivity).apply { text = title; textSize = 16f; setTextColor(text); typeface = Typeface.DEFAULT_BOLD })
        copy.addView(label(desc, 12f))
        row.addView(copy)
        row.addView(actionButton(action, click), LinearLayout.LayoutParams(dp(112), dp(42)))
        addView(row)
    }

    private fun showForum() {
        val base = root()
        attachShell(base, "forum")
        contentHost.addView(headerWithLogo("Forum", "Topluluğun güncel konuları"))
        val tabs = textButtonRow("Popüler" to {}, "Yeni" to {}, "Takip" to {})
        contentHost.addView(tabs)
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(10), 0, dp(8)) }
        scroll.addView(list)
        contentHost.addView(scroll, LinearLayout.LayoutParams.MATCH_PARENT, 0)
        (scroll.layoutParams as LinearLayout.LayoutParams).weight = 1f
        list.addView(label("Konular yükleniyor…"))
        lifecycleScope.launch {
            try {
                val topics = api.forum().getJSONArray("topics")
                list.removeAllViews()
                if (topics.length() == 0) list.addView(emptyCard("Henüz konu bulunamadı."))
                for (i in 0 until topics.length()) {
                    val t = topics.getJSONObject(i)
                    val c = cardView()
                    c.addView(TextView(this@MainActivity).apply {
                        text = t.optString("title", "Başlıksız konu")
                        textSize = 16f
                        setTextColor(text)
                        typeface = Typeface.DEFAULT_BOLD
                    })
                    c.addView(label("${t.optString("category_name", "Genel")}  •  ${t.optString("username", "Üye")}", 12f))
                    c.setOnClickListener { Toast.makeText(this@MainActivity, "Konu: ${t.optString("title")}", Toast.LENGTH_SHORT).show() }
                    list.addView(c)
                }
            } catch (e: Exception) {
                list.removeAllViews()
                list.addView(emptyCard(e.message ?: "Forum yüklenemedi."))
            }
        }
        setContentView(base)
    }

    private fun showMessages() {
        val base = root()
        attachShell(base, "messages")
        contentHost.addView(headerWithLogo("Mesajlar", "Özel konuşmalarını yönet"))
        val target = input("Kullanıcı adı")
        contentHost.addView(target, marginParams(0, 5))
        contentHost.addView(actionButton("Konuşmayı Aç") { loadMessages(target) }, LinearLayout.LayoutParams.MATCH_PARENT, dp(44))

        val scroll = ScrollView(this)
        val messagesBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(10), 0, dp(8)) }
        scroll.addView(messagesBox)
        contentHost.addView(scroll, LinearLayout.LayoutParams.MATCH_PARENT, 0)
        (scroll.layoutParams as LinearLayout.LayoutParams).weight = 1f

        val composer = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(5), 0, dp(6)) }
        val body = input("Mesaj yaz…")
        body.setSingleLine(false)
        body.minLines = 2
        body.maxLines = 4
        composer.addView(body, LinearLayout.LayoutParams(0, dp(55), 1f).apply { rightMargin = dp(7) })
        composer.addView(actionButton("Gönder") {
            val to = target.text.toString().trim()
            val message = body.text.toString().trim()
            if (to.isBlank() || message.isBlank()) return@actionButton
            lifecycleScope.launch {
                try {
                    api.sendMessage(to, message, csrf)
                    body.setText("")
                    loadMessages(target, messagesBox)
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, e.message ?: "Mesaj gönderilemedi", Toast.LENGTH_LONG).show()
                }
            }
        }, LinearLayout.LayoutParams(dp(92), dp(55)))
        contentHost.addView(composer)
        setContentView(base)
    }

    private fun loadMessages(target: EditText, box: LinearLayout? = null) {
        val name = target.text.toString().trim()
        if (name.isBlank()) return
        val targetBox = box ?: findMessageBox()
        lifecycleScope.launch {
            try {
                val messages = api.messages(name).getJSONArray("messages")
                targetBox.removeAllViews()
                if (messages.length() == 0) targetBox.addView(emptyCard("Bu konuşmada henüz mesaj yok."))
                for (i in 0 until messages.length()) {
                    val m = messages.getJSONObject(i)
                    targetBox.addView(messageBubble(m.optString("sender_username"), m.optString("content"), m.optString("sender_username") == username))
                }
            } catch (e: Exception) {
                targetBox.removeAllViews()
                targetBox.addView(emptyCard(e.message ?: "Mesajlar yüklenemedi."))
            }
        }
    }

    private fun findMessageBox(): LinearLayout {
        for (i in 0 until contentHost.childCount) {
            val v = contentHost.getChildAt(i)
            if (v is ScrollView) return v.getChildAt(0) as LinearLayout
        }
        return LinearLayout(this)
    }

    private fun messageBubble(sender: String, message: String, mine: Boolean): View = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = if (mine) Gravity.END else Gravity.START
        setPadding(dp(4), dp(3), dp(4), dp(3))
        val bubble = TextView(this@MainActivity).apply {
            text = "$sender\n$message"
            textSize = 14f
            setTextColor(text)
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = rounded(if (mine) Color.rgb(67, 42, 145) else surface2, 17, if (mine) null else border)
            maxWidth = dp(310)
        }
        addView(bubble)
    }

    private fun showNotifications() {
        val base = root()
        attachShell(base, "notifications")
        contentHost.addView(headerWithLogo("Bildirimler", "Son hesap hareketlerin"))
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        contentHost.addView(scroll, LinearLayout.LayoutParams.MATCH_PARENT, 0)
        (scroll.layoutParams as LinearLayout.LayoutParams).weight = 1f
        list.addView(label("Bildirimler yükleniyor…"))
        lifecycleScope.launch {
            try {
                val r = api.notifications()
                list.removeAllViews()
                val unread = r.optInt("unread_count")
                list.addView(label("$unread okunmamış", 12f, cyan).apply { background = rounded(Color.rgb(11, 39, 49), 13); setPadding(dp(12), dp(7), dp(12), dp(7)) })
                val ns = r.getJSONArray("notifications")
                if (ns.length() == 0) list.addView(emptyCard("Yeni bildirimin yok."))
                for (i in 0 until ns.length()) {
                    val n = ns.getJSONObject(i)
                    val c = cardView()
                    c.addView(TextView(this@MainActivity).apply { text = n.optString("actor_username", "Wiantex"); textSize = 15f; setTextColor(text); typeface = Typeface.DEFAULT_BOLD })
                    c.addView(label(n.optString("type", "Bildirim"), 12f))
                    list.addView(c)
                }
            } catch (e: Exception) {
                list.removeAllViews()
                list.addView(emptyCard(e.message ?: "Bildirimler yüklenemedi."))
            }
        }
        setContentView(base)
    }

    private fun showProfile() {
        val base = root()
        attachShell(base, "profile")
        contentHost.addView(headerWithLogo("Profil", "Wiantex hesabın"))
        val lookup = input("Kullanıcı adı (boş = kendi profilin)")
        contentHost.addView(lookup, marginParams(0, 5))
        contentHost.addView(actionButton("Profili Aç") { loadProfile(lookup.text.toString()) }, LinearLayout.LayoutParams.MATCH_PARENT, dp(44))

        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(10), 0, dp(10)) }
        scroll.addView(box)
        contentHost.addView(scroll, LinearLayout.LayoutParams.MATCH_PARENT, 0)
        (scroll.layoutParams as LinearLayout.LayoutParams).weight = 1f
        contentHost.addView(ghostButton("Çıkış Yap") {
            api.clear()
            username = null
            csrf = ""
            showLogin()
        }, LinearLayout.LayoutParams.MATCH_PARENT, dp(44))
        loadProfile("", box)
        setContentView(base)
    }

    private fun loadProfile(name: String, target: LinearLayout? = null) {
        val box = target ?: findProfileBox()
        lifecycleScope.launch {
            try {
                val profile = api.profile(name.ifBlank { null }).getJSONObject("profile")
                box.removeAllViews()
                val hero = cardView().apply {
                    background = gradientBackground(Color.rgb(28, 18, 57), Color.rgb(13, 29, 53), 22)
                    val row = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
                    row.addView(logo(72))
                    val copy = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, 0, 0); layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
                    copy.addView(TextView(this@MainActivity).apply { text = profile.optString("username", "WTR"); textSize = 23f; setTextColor(text); typeface = Typeface.DEFAULT_BOLD })
                    copy.addView(label(profile.optString("role_name", "Üye"), 12f, cyan))
                    row.addView(copy)
                    addView(row)
                    addView(label(profile.optString("bio", "Wiantex topluluğunun bir üyesi."), 13f, Color.rgb(220, 216, 232)).apply { setPadding(dp(4), dp(12), dp(4), dp(2)) })
                }
                box.addView(hero)

                val stats = cardView()
                val row = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
                listOf(
                    profile.optInt("topic_count") to "Konu",
                    profile.optInt("post_count") to "Mesaj",
                    profile.optInt("like_count") to "Beğeni"
                ).forEach { (value, caption) ->
                    val item = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
                    item.addView(TextView(this@MainActivity).apply { text = value.toString(); textSize = 21f; setTextColor(text); typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER })
                    item.addView(label(caption, 11f).apply { gravity = Gravity.CENTER })
                    row.addView(item, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                }
                stats.addView(row)
                box.addView(stats)
            } catch (e: Exception) {
                box.removeAllViews()
                box.addView(emptyCard(e.message ?: "Profil yüklenemedi."))
            }
        }
    }

    private fun findProfileBox(): LinearLayout {
        for (i in 0 until contentHost.childCount) {
            val v = contentHost.getChildAt(i)
            if (v is ScrollView) return v.getChildAt(0) as LinearLayout
        }
        return LinearLayout(this)
    }

    private fun emptyCard(message: String): LinearLayout = cardView().apply {
        gravity = Gravity.CENTER
        addView(TextView(this@MainActivity).apply {
            text = "✦"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(cyan)
        })
        addView(label(message, 13f).apply { gravity = Gravity.CENTER })
    }
}
