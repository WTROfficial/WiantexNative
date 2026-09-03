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
import android.widget.ImageView
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
    private var username: String? = null
    private var csrf: String = ""
    private lateinit var contentHost: LinearLayout

    private val bg = Color.rgb(8, 7, 18)
    private val surface = Color.rgb(17, 15, 29)
    private val surfaceAlt = Color.rgb(28, 24, 46)
    private val purple = Color.rgb(108, 76, 235)
    private val purpleDark = Color.rgb(64, 42, 148)
    private val cyan = Color.rgb(70, 205, 255)
    private val text = Color.rgb(248, 247, 252)
    private val muted = Color.rgb(171, 167, 189)
    private val border = Color.rgb(55, 49, 78)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        api = Api(this)
        username = api.username()
        csrf = api.csrf()
        if (username.isNullOrBlank()) showLogin() else showHome()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun root(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(bg)
        setPadding(dp(16), dp(14), dp(16), 0)
    }

    private fun title(title: String, subtitle: String? = null): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(4), dp(4), dp(4), dp(12))
        addView(TextView(this@MainActivity).apply {
            text = title
            textSize = 29f
            setTextColor(text)
            typeface = Typeface.DEFAULT_BOLD
        })
        subtitle?.let {
            addView(TextView(this@MainActivity).apply {
                text = it
                textSize = 13f
                setTextColor(muted)
                setPadding(0, dp(4), 0, 0)
            })
        }
    }

    private fun rounded(color: Int, radius: Int = 18, strokeColor: Int? = null): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            strokeColor?.let { setStroke(dp(1), it) }
        }

    private fun edit(hint: String, password: Boolean = false): EditText = EditText(this).apply {
        this.hint = hint
        textSize = 15f
        setTextColor(text)
        setHintTextColor(Color.rgb(120, 115, 139))
        setPadding(dp(14), 0, dp(14), 0)
        background = rounded(surfaceAlt, 16, border)
        minimumHeight = dp(52)
        if (password) inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        setSingleLine(true)
    }

    private fun button(label: String, click: () -> Unit): MaterialButton = MaterialButton(this).apply {
        text = label
        textSize = 14f
        isAllCaps = false
        setTextColor(Color.WHITE)
        backgroundTintList = ColorStateList.valueOf(purple)
        cornerRadius = dp(16)
        insetTop = 0
        insetBottom = 0
        minHeight = dp(48)
        setOnClickListener { click() }
    }

    private fun secondaryButton(label: String, click: () -> Unit): MaterialButton = MaterialButton(this).apply {
        text = label
        textSize = 13f
        isAllCaps = false
        setTextColor(text)
        backgroundTintList = ColorStateList.valueOf(surfaceAlt)
        cornerRadius = dp(16)
        strokeWidth = dp(1)
        strokeColor = ColorStateList.valueOf(border)
        insetTop = 0
        insetBottom = 0
        minHeight = dp(46)
        setOnClickListener { click() }
    }

    private fun label(value: String, size: Float = 14f, color: Int = muted): TextView = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        setPadding(0, dp(3), 0, dp(3))
    }

    private fun card(): MaterialCardView = MaterialCardView(this).apply {
        radius = dp(20).toFloat()
        cardElevation = 0f
        setCardBackgroundColor(surface)
        strokeWidth = dp(1)
        strokeColor = border
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(10) }
    }

    private fun cardContent(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(15), dp(16), dp(15))
    }

    private fun logoView(height: Int, full: Boolean = true): ImageView = ImageView(this).apply {
        setImageResource(if (full) R.drawable.wiantex_logo else R.drawable.wiantex_mark)
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(height)
        )
    }

    private fun navItem(icon: String, label: String, page: String, selected: Boolean): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(0, dp(7), 0, dp(7))
        background = rounded(if (selected) Color.rgb(45, 34, 83) else Color.TRANSPARENT, 14)
        addView(TextView(this@MainActivity).apply {
            text = icon
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(if (selected) cyan else muted)
        })
        addView(TextView(this@MainActivity).apply {
            text = label
            textSize = 10f
            gravity = Gravity.CENTER
            setTextColor(if (selected) Color.WHITE else muted)
        })
        setOnClickListener { showPage(page) }
    }

    private fun installShell(root: LinearLayout, current: String) {
        contentHost = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(1, 1, 1f)
        }
        root.addView(contentHost)

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(7), 0, dp(9))
            background = rounded(surface, 20, border)
        }
        val items = listOf(
            Triple("⌂", "Ana", "home"),
            Triple("▦", "Forum", "forum"),
            Triple("✉", "Mesaj", "messages"),
            Triple("♢", "Bildirim", "notifications"),
            Triple("◉", "Profil", "profile")
        )
        items.forEach { (icon, labelText, page) ->
            nav.addView(
                navItem(icon, labelText, page, current == page),
                LinearLayout.LayoutParams(0, dp(66), 1f).apply {
                    leftMargin = dp(2)
                    rightMargin = dp(2)
                }
            )
        }
        root.addView(nav, LinearLayout.LayoutParams.MATCH_PARENT, dp(78))
        setContentView(root)
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
        val root = root()
        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(4), dp(14), dp(4), dp(28))
        }

        box.addView(logoView(210, true))
        box.addView(label("Wiantex Mobile Client", 16f, cyan).apply { gravity = Gravity.CENTER })
        box.addView(label("Forum • mesajlar • bildirimler • profil", 14f, muted).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, dp(20))
        })

        val userInput = edit("Kullanıcı adı veya e-posta")
        val passInput = edit("Şifre", true)
        val status = label("", 13f, Color.rgb(255, 100, 125))

        box.addView(userInput, fieldParams())
        box.addView(passInput, fieldParams())
        box.addView(status, fieldParams())
        box.addView(button("Giriş Yap") {
            lifecycleScope.launch {
                try {
                    val result = api.login(userInput.text.toString().trim(), passInput.text.toString())
                    if (result.optBoolean("ok")) {
                        val user = result.optJSONObject("user")
                        username = user?.optString("username").orEmpty()
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
        }, LinearLayout.LayoutParams.MATCH_PARENT, dp(52))

        scroll.addView(box)
        root.addView(scroll, LinearLayout.LayoutParams.MATCH_PARENT, 0).apply {
            (this as LinearLayout.LayoutParams).weight = 1f
        }
        setContentView(root)
    }

    private fun fieldParams(): LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        dp(52)
    ).apply {
        topMargin = dp(5)
        bottomMargin = dp(7)
    }

    private fun showHome() {
        val root = root()
        installShell(root, "home")
        contentHost.addView(title("Ana Sayfa", "Wiantex topluluğuna hızlı erişim"))

        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)

        val hero = card()
        val heroBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(18))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(purpleDark, Color.rgb(28, 21, 49))
            ).apply { cornerRadius = dp(20).toFloat() }
        }
        heroBox.addView(logoView(110, false))
        heroBox.addView(TextView(this).apply {
            text = "Hoş geldin, ${username ?: "WTR"}"
            textSize = 23f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        })
        heroBox.addView(label("Yeni konuları keşfet, mesajlaş ve topluluğa katıl.", 13f, Color.rgb(226, 221, 240)).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(5), 0, 0)
        })
        hero.addView(heroBox)
        list.addView(hero)

        list.addView(label("HIZLI ERİŞİM", 11f, cyan).apply {
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(2), dp(4), 0, dp(8))
        })
        list.addView(actionCard("Forum", "Güncel konuları ve tartışmaları görüntüle.", "Foruma Git") { showForum() })
        list.addView(actionCard("Mesajlar", "Özel konuşmalarını aç ve mesaj gönder.", "Mesajlara Git") { showMessages() })
        list.addView(actionCard("Bildirimler", "Hesabındaki son hareketleri kontrol et.", "Bildirimleri Gör") { showNotifications() })

        contentHost.addView(scroll, LinearLayout.LayoutParams.MATCH_PARENT, 0).apply {
            (this as LinearLayout.LayoutParams).weight = 1f
        }
    }

    private fun actionCard(titleValue: String, description: String, action: String, click: () -> Unit): MaterialCardView {
        val c = card()
        val box = cardContent()
        box.addView(TextView(this).apply {
            text = titleValue
            textSize = 17f
            setTextColor(text)
            typeface = Typeface.DEFAULT_BOLD
        })
        box.addView(label(description, 13f))
        box.addView(button(action, click), LinearLayout.LayoutParams.MATCH_PARENT, dp(44)).apply {
            (this.layoutParams as LinearLayout.LayoutParams).topMargin = dp(9)
        }
        c.addView(box)
        return c
    }

    private fun showForum() {
        val root = root()
        installShell(root, "forum")
        contentHost.addView(title("Forum", "Topluluğun güncel konuları"))
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(8))
        }
        scroll.addView(list)
        contentHost.addView(scroll, LinearLayout.LayoutParams.MATCH_PARENT, 0).apply {
            (this as LinearLayout.LayoutParams).weight = 1f
        }
        list.addView(label("Konular yükleniyor…"))

        lifecycleScope.launch {
            try {
                val topics = api.forum().getJSONArray("topics")
                list.removeAllViews()
                if (topics.length() == 0) list.addView(emptyState("Henüz konu bulunamadı."))
                for (i in 0 until topics.length()) {
                    val topic = topics.getJSONObject(i)
                    val c = card()
                    val box = cardContent()
                    box.addView(TextView(this@MainActivity).apply {
                        text = topic.optString("title", "Başlıksız konu")
                        textSize = 16f
                        setTextColor(text)
                        typeface = Typeface.DEFAULT_BOLD
                    })
                    box.addView(label("${topic.optString("category_name", "Genel")}  •  ${topic.optString("username", "Üye")}", 12f))
                    c.addView(box)
                    list.addView(c)
                }
            } catch (e: Exception) {
                list.removeAllViews()
                list.addView(emptyState(e.message ?: "Forum yüklenemedi."))
            }
        }
    }

    private fun showMessages() {
        val root = root()
        installShell(root, "messages")
        contentHost.addView(title("Mesajlar", "Özel konuşmalarını yönet"))

        val target = edit("Kullanıcı adı")
        contentHost.addView(target, fieldParams())
        contentHost.addView(button("Konuşmayı Aç") { loadMessages(target) }, LinearLayout.LayoutParams.MATCH_PARENT, dp(46))

        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(10), 0, dp(10))
        }
        scroll.addView(box)
        contentHost.addView(scroll, LinearLayout.LayoutParams.MATCH_PARENT, 0).apply {
            (this as LinearLayout.LayoutParams).weight = 1f
        }

        val composer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(7), 0, dp(8))
        }
        val body = edit("Mesaj yaz…")
        body.setSingleLine(false)
        body.maxLines = 4
        body.gravity = Gravity.TOP or Gravity.START
        composer.addView(body, LinearLayout.LayoutParams(0, dp(56), 1f).apply { rightMargin = dp(8) })
        composer.addView(button("Gönder") {
            val to = target.text.toString().trim()
            val message = body.text.toString().trim()
            if (to.isBlank() || message.isBlank()) return@button
            lifecycleScope.launch {
                try {
                    api.sendMessage(to, message, csrf)
                    body.setText("")
                    loadMessages(target, box)
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, e.message ?: "Mesaj gönderilemedi", Toast.LENGTH_LONG).show()
                }
            }
        }, LinearLayout.LayoutParams(dp(96), dp(56)))
        contentHost.addView(composer)
    }

    private fun loadMessages(target: EditText, existing: LinearLayout? = null) {
        val targetName = target.text.toString().trim()
        if (targetName.isBlank()) return
        val box = existing ?: (contentHost.getChildAt(3) as? ScrollView)?.getChildAt(0) as? LinearLayout ?: return

        lifecycleScope.launch {
            try {
                val messages = api.messages(targetName).getJSONArray("messages")
                box.removeAllViews()
                if (messages.length() == 0) box.addView(emptyState("Bu konuşmada henüz mesaj yok."))
                for (i in 0 until messages.length()) {
                    val m = messages.getJSONObject(i)
                    box.addView(messageBubble(
                        m.optString("content"),
                        m.optString("sender_username", "Üye"),
                        m.optString("sender_username") == username
                    ))
                }
            } catch (e: Exception) {
                box.removeAllViews()
                box.addView(emptyState(e.message ?: "Mesajlar yüklenemedi."))
            }
        }
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
            setTextColor(text)
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = rounded(if (mine) purpleDark else surfaceAlt, 17, if (mine) null else border)
            maxWidth = dp(315)
        })
        return row
    }

    private fun showNotifications() {
        val root = root()
        installShell(root, "notifications")
        contentHost.addView(title("Bildirimler", "Son hesap hareketlerin"))
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        contentHost.addView(scroll, LinearLayout.LayoutParams.MATCH_PARENT, 0).apply {
            (this as LinearLayout.LayoutParams).weight = 1f
        }
        list.addView(label("Bildirimler yükleniyor…"))

        lifecycleScope.launch {
            try {
                val result = api.notifications()
                list.removeAllViews()
                list.addView(label("${result.optInt("unread_count")} okunmamış", 12f, cyan).apply {
                    background = rounded(Color.rgb(18, 44, 55), 12)
                    setPadding(dp(12), dp(7), dp(12), dp(7))
                })
                val notifications = result.getJSONArray("notifications")
                if (notifications.length() == 0) list.addView(emptyState("Yeni bildirimin yok."))
                for (i in 0 until notifications.length()) {
                    val n = notifications.getJSONObject(i)
                    val c = card()
                    val box = cardContent()
                    box.addView(TextView(this@MainActivity).apply {
                        text = n.optString("actor_username", "Wiantex")
                        textSize = 15f
                        setTextColor(text)
                        typeface = Typeface.DEFAULT_BOLD
                    })
                    box.addView(label(n.optString("type", "Bildirim"), 12f))
                    c.addView(box)
                    list.addView(c)
                }
            } catch (e: Exception) {
                list.removeAllViews()
                list.addView(emptyState(e.message ?: "Bildirimler yüklenemedi."))
            }
        }
    }

    private fun showProfile() {
        val root = root()
        installShell(root, "profile")
        contentHost.addView(title("Profil", "Wiantex hesabın"))

        val lookup = edit("Kullanıcı adı (boş = kendi profilin)")
        contentHost.addView(lookup, fieldParams())
        contentHost.addView(button("Profili Aç") { loadProfile(lookup.text.toString()) }, LinearLayout.LayoutParams.MATCH_PARENT, dp(46))

        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(12), 0, dp(10)) }
        scroll.addView(box)
        contentHost.addView(scroll, LinearLayout.LayoutParams.MATCH_PARENT, 0).apply {
            (this as LinearLayout.LayoutParams).weight = 1f
        }
        contentHost.addView(secondaryButton("Çıkış Yap") {
            api.clear()
            username = null
            csrf = ""
            showLogin()
        }, LinearLayout.LayoutParams.MATCH_PARENT, dp(46))
        loadProfile("", box)
    }

    private fun loadProfile(name: String, target: LinearLayout? = null) {
        val box = target ?: (contentHost.getChildAt(3) as? ScrollView)?.getChildAt(0) as? LinearLayout ?: return
        lifecycleScope.launch {
            try {
                val profile = api.profile(name.ifBlank { null }).getJSONObject("profile")
                box.removeAllViews()
                box.addView(logoView(120, false))
                val c = card()
                val inner = cardContent()
                inner.addView(TextView(this@MainActivity).apply {
                    text = profile.optString("username", "WTR")
                    textSize = 25f
                    setTextColor(text)
                    typeface = Typeface.DEFAULT_BOLD
                })
                inner.addView(label(profile.optString("role_name", "Üye"), 13f, cyan))
                inner.addView(label(profile.optString("bio", "Wiantex topluluğunun bir üyesi."), 14f, Color.rgb(216, 212, 228)))
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
                ).forEach { (value, labelText) ->
                    row.addView(LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        addView(TextView(this@MainActivity).apply {
                            text = value.toString()
                            textSize = 21f
                            setTextColor(text)
                            typeface = Typeface.DEFAULT_BOLD
                            gravity = Gravity.CENTER
                        })
                        addView(label(labelText, 11f).apply { gravity = Gravity.CENTER })
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
            setTextColor(cyan)
            gravity = Gravity.CENTER
        })
        box.addView(label(message, 14f).apply { gravity = Gravity.CENTER })
        c.addView(box)
        return c
    }
}
