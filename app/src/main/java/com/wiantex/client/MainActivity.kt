package com.wiantex.client

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var api: Api

    private val bgColor = Color.rgb(8, 7, 18)
    private val surfaceColor = Color.rgb(17, 15, 29)
    private val surfaceAltColor = Color.rgb(25, 22, 43)
    private val purpleColor = Color.rgb(111, 74, 232)
    private val purpleDarkColor = Color.rgb(67, 42, 145)
    private val cyanColor = Color.rgb(76, 207, 255)
    private val primaryTextColor = Color.WHITE
    private val mutedTextColor = Color.rgb(172, 168, 190)
    private val borderColor = Color.rgb(49, 44, 70)

    private var username: String? = null
    private var csrf = ""
    private lateinit var contentHost: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bgColor
        window.navigationBarColor = bgColor
        api = Api(this)
        username = api.username()
        csrf = api.csrf()
        if (username.isNullOrBlank()) showLogin() else showHome()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun screenRoot(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(bgColor)
        setPadding(dp(18), dp(16), dp(18), 0)
    }

    private fun pageTitle(titleValue: String, subtitle: String? = null): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(14))
            addView(TextView(this@MainActivity).apply {
                text = titleValue
                textSize = 30f
                setTextColor(primaryTextColor)
                typeface = Typeface.DEFAULT_BOLD
            })
            subtitle?.takeIf { it.isNotBlank() }?.let {
                addView(TextView(this@MainActivity).apply {
                    text = it
                    textSize = 13f
                    setTextColor(mutedTextColor)
                    setPadding(0, dp(4), 0, 0)
                })
            }
        }
    }

    private fun roundedBackground(color: Int, radiusDp: Int = 16, stroke: Int? = null): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radiusDp).toFloat()
            stroke?.let { setStroke(dp(1), it) }
        }
    }

    private fun input(hintValue: String, password: Boolean = false): EditText = EditText(this).apply {
        hint = hintValue
        textSize = 15f
        setTextColor(primaryTextColor)
        setHintTextColor(Color.rgb(116, 111, 133))
        setPadding(dp(14), 0, dp(14), 0)
        background = roundedBackground(surfaceAltColor, 16, borderColor)
        minimumHeight = dp(52)
        if (password) inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        setSingleLine(true)
    }

    private fun primaryButton(label: String, onClick: () -> Unit): MaterialButton = MaterialButton(this).apply {
        text = label
        textSize = 14f
        isAllCaps = false
        setTextColor(Color.WHITE)
        cornerRadius = dp(16)
        backgroundTintList = ColorStateList.valueOf(purpleColor)
        insetTop = 0
        insetBottom = 0
        minHeight = dp(48)
        setOnClickListener { onClick() }
    }

    private fun secondaryButton(label: String, onClick: () -> Unit): MaterialButton = MaterialButton(this).apply {
        text = label
        textSize = 13f
        isAllCaps = false
        setTextColor(primaryTextColor)
        cornerRadius = dp(16)
        backgroundTintList = ColorStateList.valueOf(surfaceAltColor)
        strokeWidth = dp(1)
        strokeColor = ColorStateList.valueOf(borderColor)
        insetTop = 0
        insetBottom = 0
        minHeight = dp(46)
        setOnClickListener { onClick() }
    }

    private fun bodyLabel(value: String, size: Float = 14f, color: Int = mutedTextColor): TextView = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        setPadding(0, dp(3), 0, dp(3))
    }

    private fun card(): MaterialCardView = MaterialCardView(this).apply {
        radius = dp(20).toFloat()
        cardElevation = 0f
        setCardBackgroundColor(surfaceColor)
        strokeWidth = dp(1)
        strokeColor = borderColor
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(10) }
    }

    private fun contentColumn(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(15), dp(16), dp(15))
    }

    private fun makeNavItem(icon: String, label: String, selected: Boolean, click: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, dp(7), 0, dp(7))
            background = roundedBackground(if (selected) Color.rgb(45, 34, 83) else Color.TRANSPARENT, 15)
            addView(TextView(this@MainActivity).apply {
                text = icon
                textSize = 19f
                gravity = Gravity.CENTER
                setTextColor(if (selected) cyanColor else mutedTextColor)
            })
            addView(TextView(this@MainActivity).apply {
                text = label
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(if (selected) primaryTextColor else mutedTextColor)
                setPadding(0, dp(1), 0, 0)
            })
            setOnClickListener { click() }
        }
    }

    private fun installShell(root: LinearLayout, current: String) {
        contentHost = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
        }
        root.addView(contentHost)

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(7), 0, dp(10))
            background = roundedBackground(surfaceColor, 20, borderColor)
        }

        val items = listOf(
            Triple("⌂", "Ana", "home"),
            Triple("▦", "Forum", "forum"),
            Triple("✉", "Mesaj", "messages"),
            Triple("♢", "Bildirim", "notifications"),
            Triple("◉", "Profil", "profile")
        )
        items.forEach { (icon, label, key) ->
            nav.addView(
                makeNavItem(icon, label, key == current) { showPage(key) },
                LinearLayout.LayoutParams(0, dp(64), 1f).apply {
                    leftMargin = dp(2)
                    rightMargin = dp(2)
                }
            )
        }
        root.addView(nav, ViewGroup.LayoutParams.MATCH_PARENT, dp(78))
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
        val root = screenRoot()
        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(4), dp(42), dp(4), dp(30))
        }
        box.addView(TextView(this).apply {
            text = "WTR"
            textSize = 46f
            setTextColor(cyanColor)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }, ViewGroup.LayoutParams.MATCH_PARENT, dp(72))
        box.addView(bodyLabel("Wiantex Mobile Client", 13f, mutedTextColor).apply { gravity = Gravity.CENTER })
        box.addView(bodyLabel("Forum, mesajlar, bildirimler ve profil.", 15f, primaryTextColor).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(24))
        })
        val userInput = input("Kullanıcı adı veya e-posta")
        val passInput = input("Şifre", true)
        box.addView(userInput, marginParams())
        box.addView(passInput, marginParams())
        val status = bodyLabel("", 13f, Color.rgb(255, 106, 126)).apply { gravity = Gravity.CENTER }
        box.addView(status, marginParams())
        val login = primaryButton("Giriş Yap") {
            lifecycleScope.launch {
                try {
                    val result = api.login(userInput.text.toString().trim(), passInput.text.toString())
                    if (result.optBoolean("ok")) {
                        val user = result.getJSONObject("user")
                        username = user.optString("username")
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
        }
        box.addView(login, ViewGroup.LayoutParams.MATCH_PARENT, dp(52))
        scroll.addView(box)
        root.addView(scroll, ViewGroup.LayoutParams.MATCH_PARENT, 0)
        (scroll.layoutParams as LinearLayout.LayoutParams).weight = 1f
        setContentView(root)
    }

    private fun marginParams(top: Int = 7, bottom: Int = 7): LinearLayout.LayoutParams =
        ViewGroup.LayoutParams.MATCH_PARENT.let {
            LinearLayout.LayoutParams(it, dp(52)).apply {
                topMargin = dp(top)
                bottomMargin = dp(bottom)
            }
        }

    private fun heroCard(): MaterialCardView {
        val c = card()
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(purpleDarkColor, Color.rgb(28, 21, 49))
            ).apply { cornerRadius = dp(20).toFloat() }
        }
        box.addView(bodyLabel("WIANТEX MOBILE", 11f, cyanColor).apply {
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.06f
        })
        box.addView(TextView(this).apply {
            text = "Hoş geldin, ${username ?: "WTR"}"
            textSize = 25f
            setTextColor(primaryTextColor)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(4), 0, dp(2))
        })
        box.addView(bodyLabel("Topluluğu keşfet ve konuşmalara katıl.", 14f, Color.rgb(226, 221, 240)))
        c.addView(box)
        return c
    }

    private fun actionCard(titleValue: String, description: String, action: String, click: () -> Unit): MaterialCardView {
        val c = card()
        val box = contentColumn()
        box.addView(TextView(this).apply {
            text = titleValue
            textSize = 17f
            setTextColor(primaryTextColor)
            typeface = Typeface.DEFAULT_BOLD
        })
        box.addView(bodyLabel(description))
        box.addView(primaryButton(action, click), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)).apply { topMargin = dp(8) })
        c.addView(box)
        return c
    }

    private fun showHome() {
        val root = screenRoot()
        installShell(root, "home")
        contentHost.addView(pageTitle("Ana Sayfa", "Wiantex topluluğuna hızlı erişim"))
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        list.addView(heroCard())
        list.addView(bodyLabel("HIZLI ERİŞİM", 11f, cyanColor).apply {
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(2), dp(4), 0, dp(8))
        })
        list.addView(actionCard("Forum", "Yeni konuları ve aktif tartışmaları gör.", "Foruma Git") { showForum() })
        list.addView(actionCard("Mesajlar", "Özel konuşmalarını aç ve mesaj gönder.", "Mesajlara Git") { showMessages() })
        list.addView(actionCard("Bildirimler", "Hesabındaki hareketleri takip et.", "Bildirimleri Gör") { showNotifications() })
        contentHost.addView(scroll, ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        setContentView(root)
    }

    private fun showForum() {
        val root = screenRoot()
        installShell(root, "forum")
        contentHost.addView(pageTitle("Forum", "Topluluğun güncel konuları"))
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        contentHost.addView(scroll, ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        list.addView(bodyLabel("Konular yükleniyor…"))
        lifecycleScope.launch {
            try {
                val result = api.forum()
                list.removeAllViews()
                val topics = result.getJSONArray("topics")
                if (topics.length() == 0) list.addView(emptyState("Henüz konu bulunamadı."))
                for (i in 0 until topics.length()) {
                    val topic = topics.getJSONObject(i)
                    val c = card()
                    val box = contentColumn()
                    box.addView(TextView(this@MainActivity).apply {
                        text = topic.optString("title", "Başlıksız konu")
                        textSize = 16f
                        setTextColor(primaryTextColor)
                        typeface = Typeface.DEFAULT_BOLD
                    })
                    box.addView(bodyLabel("${topic.optString("category_name", "Genel")}  •  ${topic.optString("username", "Üye")}", 12f))
                    c.addView(box)
                    list.addView(c)
                }
            } catch (e: Exception) {
                list.removeAllViews()
                list.addView(emptyState(e.message ?: "Forum yüklenemedi."))
            }
        }
        setContentView(root)
    }

    private fun showMessages() {
        val root = screenRoot()
        installShell(root, "messages")
        contentHost.addView(pageTitle("Mesajlar", "Özel konuşmalarını yönet"))
        val target = input("Kullanıcı adı")
        contentHost.addView(target, ViewGroup.LayoutParams.MATCH_PARENT, dp(52))
        contentHost.addView(primaryButton("Konuşmayı Aç") { loadMessages(target) }, ViewGroup.LayoutParams.MATCH_PARENT, dp(46))

        val scroll = ScrollView(this)
        val messagesBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(10), 0, dp(10))
        }
        scroll.addView(messagesBox)
        contentHost.addView(scroll, ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)

        val composer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(6), 0, dp(8))
        }
        val body = input("Mesaj yaz…")
        body.setSingleLine(false)
        body.maxLines = 4
        body.gravity = Gravity.TOP or Gravity.START
        composer.addView(body, LinearLayout.LayoutParams(0, dp(54), 1f).apply { rightMargin = dp(8) })
        composer.addView(primaryButton("Gönder") {
            val to = target.text.toString().trim()
            val message = body.text.toString().trim()
            if (to.isBlank() || message.isBlank()) return@primaryButton
            lifecycleScope.launch {
                try {
                    api.sendMessage(to, message, csrf)
                    body.setText("")
                    loadMessages(target, messagesBox)
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, e.message ?: "Mesaj gönderilemedi", Toast.LENGTH_LONG).show()
                }
            }
        }, LinearLayout.LayoutParams(dp(92), dp(54)))
        contentHost.addView(composer)
        setContentView(root)
    }

    private fun loadMessages(target: EditText, existing: LinearLayout? = null) {
        val targetName = target.text.toString().trim()
        if (targetName.isBlank()) return
        val box = existing ?: findMessageBox()
        lifecycleScope.launch {
            try {
                val messages = api.messages(targetName).getJSONArray("messages")
                box.removeAllViews()
                if (messages.length() == 0) box.addView(emptyState("Bu konuşmada henüz mesaj yok."))
                for (i in 0 until messages.length()) {
                    val message = messages.getJSONObject(i)
                    box.addView(messageBubble(
                        message.optString("content"),
                        message.optString("sender_username"),
                        message.optString("sender_username") == username
                    ))
                }
            } catch (e: Exception) {
                box.removeAllViews()
                box.addView(emptyState(e.message ?: "Mesajlar yüklenemedi."))
            }
        }
    }

    private fun findMessageBox(): LinearLayout {
        val scroll = contentHost.getChildAt(3) as ScrollView
        return scroll.getChildAt(0) as LinearLayout
    }

    private fun messageBubble(message: String, sender: String, mine: Boolean): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = if (mine) Gravity.END else Gravity.START
            setPadding(dp(4), dp(3), dp(4), dp(3))
        }
        row.addView(TextView(this).apply {
            text = "$sender\n$message"
            textSize = 14f
            setTextColor(primaryTextColor)
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = roundedBackground(if (mine) purpleDarkColor else surfaceAltColor, 17, if (mine) null else borderColor)
            maxWidth = dp(310)
        })
        return row
    }

    private fun showNotifications() {
        val root = screenRoot()
        installShell(root, "notifications")
        contentHost.addView(pageTitle("Bildirimler", "Son hesap hareketlerin"))
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        contentHost.addView(scroll, ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        list.addView(bodyLabel("Bildirimler yükleniyor…"))
        lifecycleScope.launch {
            try {
                val result = api.notifications()
                list.removeAllViews()
                val unread = result.optInt("unread_count")
                list.addView(TextView(this@MainActivity).apply {
                    text = "$unread okunmamış"
                    textSize = 12f
                    setTextColor(cyanColor)
                    setPadding(dp(12), dp(7), dp(12), dp(7))
                    background = roundedBackground(Color.rgb(18, 44, 55), 12)
                }, LinearLayout.LayoutParams.WRAP_CONTENT, dp(34))
                val notifications = result.getJSONArray("notifications")
                if (notifications.length() == 0) list.addView(emptyState("Yeni bildirimin yok."))
                for (i in 0 until notifications.length()) {
                    val n = notifications.getJSONObject(i)
                    val c = card()
                    val box = contentColumn()
                    box.addView(TextView(this@MainActivity).apply {
                        text = n.optString("actor_username", "Wiantex")
                        textSize = 15f
                        setTextColor(primaryTextColor)
                        typeface = Typeface.DEFAULT_BOLD
                    })
                    box.addView(bodyLabel(n.optString("type", "Bildirim"), 12f))
                    c.addView(box)
                    list.addView(c)
                }
            } catch (e: Exception) {
                list.removeAllViews()
                list.addView(emptyState(e.message ?: "Bildirimler yüklenemedi."))
            }
        }
        setContentView(root)
    }

    private fun showProfile() {
        val root = screenRoot()
        installShell(root, "profile")
        contentHost.addView(pageTitle("Profil", "Wiantex hesabın"))
        val lookup = input("Kullanıcı adı (boş = kendi profilin)")
        contentHost.addView(lookup, ViewGroup.LayoutParams.MATCH_PARENT, dp(52))
        contentHost.addView(primaryButton("Profili Aç") { loadProfile(lookup.text.toString()) }, ViewGroup.LayoutParams.MATCH_PARENT, dp(46))
        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(12), 0, dp(10)) }
        scroll.addView(box)
        contentHost.addView(scroll, ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        contentHost.addView(secondaryButton("Çıkış Yap") {
            api.clear()
            username = null
            csrf = ""
            showLogin()
        }, ViewGroup.LayoutParams.MATCH_PARENT, dp(46))
        loadProfile("", box)
        setContentView(root)
    }

    private fun loadProfile(name: String, target: LinearLayout? = null) {
        val box = target ?: (contentHost.getChildAt(3) as ScrollView).getChildAt(0) as LinearLayout
        lifecycleScope.launch {
            try {
                val profile = api.profile(name.ifBlank { null }).getJSONObject("profile")
                box.removeAllViews()
                val c = card()
                val inner = contentColumn()
                inner.addView(TextView(this@MainActivity).apply {
                    text = profile.optString("username", "WTR")
                    textSize = 25f
                    setTextColor(primaryTextColor)
                    typeface = Typeface.DEFAULT_BOLD
                })
                inner.addView(bodyLabel(profile.optString("role_name", "Üye"), 13f, cyanColor))
                inner.addView(bodyLabel(profile.optString("bio", "Wiantex topluluğunun bir üyesi."), 14f, Color.rgb(216, 212, 228)))
                c.addView(inner)
                box.addView(c)

                val stats = card()
                val row = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(dp(8), dp(15), dp(8), dp(15))
                }
                listOf(
                    profile.optInt("topic_count") to "Konu",
                    profile.optInt("post_count") to "Mesaj",
                    profile.optInt("like_count") to "Beğeni"
                ).forEach { (value, label) ->
                    row.addView(LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        addView(TextView(this@MainActivity).apply {
                            text = value.toString()
                            textSize = 21f
                            setTextColor(primaryTextColor)
                            typeface = Typeface.DEFAULT_BOLD
                            gravity = Gravity.CENTER
                        })
                        addView(bodyLabel(label, 11f).apply { gravity = Gravity.CENTER })
                    }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                }
                stats.addView(row)
                box.addView(stats)
            } catch (e: Exception) {
                box.removeAllViews()
                box.addView(emptyState(e.message ?: "Profil yüklenemedi."))
            }
        }
    }

    private fun emptyState(message: String): MaterialCardView {
        val c = card()
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(24), dp(18), dp(24))
        }
        box.addView(TextView(this).apply {
            text = "✦"
            textSize = 26f
            setTextColor(cyanColor)
            gravity = Gravity.CENTER
        })
        box.addView(bodyLabel(message).apply { gravity = Gravity.CENTER })
        c.addView(box)
        return c
    }
}
