package com.wiantex.client

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var api: Api

    private val background = Color.rgb(8, 7, 18)
    private val surface = Color.rgb(18, 16, 31)
    private val surface2 = Color.rgb(25, 22, 43)
    private val purple = Color.rgb(112, 78, 232)
    private val purpleDark = Color.rgb(72, 46, 160)
    private val cyan = Color.rgb(53, 205, 255)
    private val text = Color.WHITE
    private val muted = Color.rgb(168, 164, 184)
    private val line = Color.rgb(48, 44, 68)

    private var username: String? = null
    private var csrf = ""
    private lateinit var contentHost: LinearLayout
    private lateinit var bottomNav: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = background
        window.navigationBarColor = background
        api = Api(this)
        username = api.username()
        csrf = api.csrf()
        if (username.isNullOrBlank()) {
            showLogin()
        } else {
            showHome()
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun root(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(background)
        setPadding(dp(18), dp(16), dp(18), 0)
    }

    private fun title(textValue: String, subtitle: String? = null): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, 0, 0, dp(12))
        addView(TextView(this@MainActivity).apply {
            text = textValue
            textSize = 28f
            setTextColor(text)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        })
        if (!subtitle.isNullOrBlank()) {
            addView(TextView(this@MainActivity).apply {
                text = subtitle
                textSize = 14f
                setTextColor(muted)
                setPadding(0, dp(4), 0, 0)
            })
        }
    }

    private fun pillButton(label: String, onClick: () -> Unit, filled: Boolean = true): MaterialButton =
        MaterialButton(this).apply {
            text = label
            textSize = 14f
            setTextColor(Color.WHITE)
            isAllCaps = false
            cornerRadius = dp(18)
            insetTop = 0
            insetBottom = 0
            minHeight = dp(48)
            if (filled) {
                backgroundTintList = ColorStateList.valueOf(purple)
            } else {
                backgroundTintList = ColorStateList.valueOf(surface2)
                strokeWidth = dp(1)
                strokeColor = ColorStateList.valueOf(line)
            }
            setOnClickListener { onClick() }
        }

    private fun input(hintValue: String, password: Boolean = false): EditText = EditText(this).apply {
        hint = hintValue
        textSize = 15f
        setTextColor(text)
        setHintTextColor(Color.rgb(115, 110, 132))
        setPadding(dp(14), 0, dp(14), 0)
        background = GradientDrawable().apply {
            setColor(surface2)
            cornerRadius = dp(16).toFloat()
            setStroke(dp(1), line)
        }
        minimumHeight = dp(52)
        if (password) {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        singleLine = true
    }

    private fun card(): MaterialCardView = MaterialCardView(this).apply {
        radius = dp(20).toFloat()
        cardElevation = 0f
        setCardBackgroundColor(surface)
        strokeWidth = dp(1)
        strokeColor = line
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(10) }
    }

    private fun sectionLabel(label: String): TextView = TextView(this).apply {
        text = label.uppercase()
        textSize = 11f
        setTextColor(cyan)
        letterSpacing = 0.08f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        setPadding(0, dp(8), 0, dp(8))
    }

    private fun bodyLabel(value: String, size: Float = 14f, color: Int = muted): TextView = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        setPadding(0, dp(4), 0, dp(4))
    }

    private fun navItem(icon: String, labelValue: String, selected: Boolean, onClick: () -> Unit): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            minimumHeight = dp(58)
            setPadding(0, dp(7), 0, dp(7))
            background = GradientDrawable().apply {
                setColor(if (selected) Color.rgb(44, 32, 88) else Color.TRANSPARENT)
                cornerRadius = dp(16).toFloat()
            }
            addView(TextView(this@MainActivity).apply {
                text = icon
                textSize = 19f
                gravity = Gravity.CENTER
                setTextColor(if (selected) text else muted)
            })
            addView(TextView(this@MainActivity).apply {
                text = labelValue
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(if (selected) text else muted)
                setPadding(0, dp(1), 0, 0)
            })
            setOnClickListener { onClick() }
        }

    private fun setupShell(root: LinearLayout, current: String) {
        contentHost = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
        }
        root.addView(contentHost)

        bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(7), 0, dp(10))
            setBackgroundColor(background)
        }
        val pages = listOf(
            Triple("⌂", "Ana", "home"),
            Triple("▦", "Forum", "forum"),
            Triple("✉", "Mesaj", "messages"),
            Triple("♢", "Bildirim", "notifications"),
            Triple("◉", "Profil", "profile")
        )
        pages.forEach { (icon, labelValue, key) ->
            bottomNav.addView(
                navItem(icon, labelValue, key == current) { showPage(key) },
                LinearLayout.LayoutParams(0, dp(70), 1f).apply { setMargins(dp(2), 0, dp(2), 0) }
            )
        }
        root.addView(bottomNav)
    }

    private fun showPage(page: String) {
        when (page) {
            "home" -> showHome()
            "forum" -> showForum()
            "messages" -> showMessages()
            "notifications" -> showNotifications()
            "profile" -> showProfile()
            "login" -> showLogin()
        }
    }

    private fun showLogin() {
        val shell = root()
        val center = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            setPadding(dp(4), dp(28), dp(4), dp(20))
        }
        val logo = TextView(this).apply {
            text = "WTR"
            textSize = 42f
            setTextColor(cyan)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        center.addView(logo, LinearLayout.LayoutParams.MATCH_PARENT, dp(70))
        center.addView(bodyLabel("Wiantex Mobile", 13f, muted).apply { gravity = Gravity.CENTER })
        center.addView(bodyLabel("Topluluk, forum ve mesajlaşma tek yerde.", 15f, text).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(24))
        })

        val usernameInput = input("Kullanıcı adı veya e-posta")
        val passwordInput = input("Şifre", true)
        center.addView(usernameInput, marginParams())
        center.addView(passwordInput, marginParams())

        val status = bodyLabel("", 13f, Color.rgb(255, 105, 125))
        status.gravity = Gravity.CENTER
        center.addView(status, marginParams())
        center.addView(pillButton("Giriş Yap") {
            lifecycleScope.launch {
                try {
                    val result = api.login(usernameInput.text.toString().trim(), passwordInput.text.toString())
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
        }, true).apply { setMargins(dp(0), dp(8), dp(0), dp(0)) }, ViewGroup.LayoutParams.MATCH_PARENT, dp(52))
        shell.addView(center)
        setContentView(shell)
    }

    private fun marginParams(top: Int = 8, bottom: Int = 8): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply {
            setMargins(0, dp(top), 0, dp(bottom))
        }

    private fun heroCard(): MaterialCardView {
        val c = card()
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(purpleDark, Color.rgb(31, 23, 57)))
        }
        box.addView(sectionLabel("WIANТEX MOBILE"))
        box.addView(TextView(this@MainActivity).apply {
            text = "Hoş geldin, ${username ?: "WTR"}"
            textSize = 24f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        })
        box.addView(bodyLabel("Topluluğu keşfet, konulara katıl ve mesajlarını takip et.", 14f, Color.rgb(229, 225, 242)))
        c.addView(box)
        return c
    }

    private fun quickCard(titleValue: String, desc: String, buttonText: String, onClick: () -> Unit): MaterialCardView {
        val c = card()
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        box.addView(TextView(this@MainActivity).apply {
            text = titleValue
            textSize = 17f
            setTextColor(text)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        })
        box.addView(bodyLabel(desc))
        box.addView(pillButton(buttonText, onClick).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)).apply { topMargin = dp(8) }
        })
        c.addView(box)
        return c
    }

    private fun showHome() {
        val shell = root()
        setupShell(shell, "home")
        contentHost.addView(title("Ana Sayfa", "Wiantex topluluğuna hızlı erişim"))
        contentHost.addView(heroCard())
        contentHost.addView(sectionLabel("Hızlı Erişim"))
        contentHost.addView(quickCard("Forum", "Yeni konuları ve aktif tartışmaları görüntüle.", "Foruma Git") { showForum() })
        contentHost.addView(quickCard("Mesajlar", "Özel konuşmalarını aç ve yeni mesaj gönder.", "Mesajlara Git") { showMessages() })
        contentHost.addView(quickCard("Bildirimler", "Hesabındaki güncel hareketleri takip et.", "Bildirimleri Gör") { showNotifications() })
        setContentView(shell)
    }

    private fun showForum() {
        val shell = root()
        setupShell(shell, "forum")
        contentHost.addView(title("Forum", "Topluluğun güncel konuları"))
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        contentHost.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        val loading = bodyLabel("Konular yükleniyor…", 14f, muted)
        list.addView(loading)
        lifecycleScope.launch {
            try {
                val result = api.forum()
                list.removeAllViews()
                val topics = result.getJSONArray("topics")
                if (topics.length() == 0) {
                    list.addView(emptyState("Henüz konu bulunamadı."))
                }
                for (i in 0 until topics.length()) {
                    val topic = topics.getJSONObject(i)
                    val c = card()
                    val box = LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dp(16), dp(14), dp(16), dp(14))
                    }
                    box.addView(TextView(this@MainActivity).apply {
                        text = topic.optString("title", "Başlıksız konu")
                        textSize = 16f
                        setTextColor(text)
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    })
                    box.addView(bodyLabel(
                        "${topic.optString("category_name", "Genel")}  •  ${topic.optString("username", "Üye")}",
                        12f,
                        muted
                    ))
                    c.addView(box)
                    list.addView(c)
                }
            } catch (e: Exception) {
                list.removeAllViews()
                list.addView(emptyState(e.message ?: "Forum yüklenemedi."))
            }
        }
        setContentView(shell)
    }

    private fun showMessages() {
        val shell = root()
        setupShell(shell, "messages")
        contentHost.addView(title("Mesajlar", "Özel konuşmalarını yönet"))

        val target = input("Kullanıcı adı")
        contentHost.addView(target, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply { bottomMargin = dp(8) })
        contentHost.addView(pillButton("Konuşmayı Aç") { loadMessages(target) }, LinearLayout.LayoutParams.MATCH_PARENT, dp(46))

        val scroll = ScrollView(this)
        val messagesBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(10), 0, dp(10)) }
        scroll.addView(messagesBox)
        contentHost.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0).apply { weight = 1f })

        val composerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(6), 0, dp(8))
        }
        val body = EditText(this).apply {
            hint = "Mesaj yaz…"
            textSize = 14f
            setTextColor(text)
            setHintTextColor(Color.rgb(115, 110, 132))
            setPadding(dp(14), dp(10), dp(14), dp(10))
            minHeight = dp(52)
            maxLines = 4
            background = GradientDrawable().apply {
                setColor(surface2)
                cornerRadius = dp(16).toFloat()
                setStroke(dp(1), line)
            }
        }
        composerRow.addView(body, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = dp(8) })
        composerRow.addView(MaterialButton(this).apply {
            text = "Gönder"
            isAllCaps = false
            textSize = 13f
            cornerRadius = dp(16)
            backgroundTintList = ColorStateList.valueOf(purple)
            setTextColor(Color.WHITE)
            setPadding(dp(12), 0, dp(12), 0)
            setOnClickListener {
                val name = target.text.toString().trim()
                val message = body.text.toString().trim()
                if (name.isBlank() || message.isBlank()) return@setOnClickListener
                lifecycleScope.launch {
                    try {
                        api.sendMessage(name, message, csrf)
                        body.setText("")
                        loadMessages(target, messagesBox)
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, e.message ?: "Mesaj gönderilemedi", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }, LinearLayout.LayoutParams(dp(92), dp(52)))
        contentHost.addView(composerRow)
        setContentView(shell)
    }

    private fun loadMessages(target: EditText, existing: LinearLayout? = null) {
        val targetName = target.text.toString().trim()
        if (targetName.isBlank()) return
        val box = existing ?: findMessageBoxFromContent()
        lifecycleScope.launch {
            try {
                val messages = api.messages(targetName).getJSONArray("messages")
                box.removeAllViews()
                if (messages.length() == 0) {
                    box.addView(emptyState("Bu konuşmada henüz mesaj yok."))
                }
                for (i in 0 until messages.length()) {
                    val m = messages.getJSONObject(i)
                    val mine = m.optString("sender_username") == username
                    box.addView(messageBubble(
                        m.optString("content"),
                        m.optString("sender_username"),
                        mine
                    ))
                }
            } catch (e: Exception) {
                box.removeAllViews()
                box.addView(emptyState(e.message ?: "Mesajlar yüklenemedi."))
            }
        }
    }

    private fun findMessageBoxFromContent(): LinearLayout {
        val scroll = contentHost.getChildAt(3) as ScrollView
        return scroll.getChildAt(0) as LinearLayout
    }

    private fun messageBubble(message: String, sender: String, mine: Boolean): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = if (mine) Gravity.END else Gravity.START
            setPadding(dp(4), dp(3), dp(4), dp(3))
        }
        val bubble = TextView(this).apply {
            text = "$sender\n$message"
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(dp(13), dp(10), dp(13), dp(10))
            background = GradientDrawable().apply {
                setColor(if (mine) purpleDark else surface2)
                cornerRadius = dp(16).toFloat()
                if (!mine) setStroke(dp(1), line)
            }
            maxWidth = dp(300)
        }
        row.addView(bubble)
        return row
    }

    private fun showNotifications() {
        val shell = root()
        setupShell(shell, "notifications")
        contentHost.addView(title("Bildirimler", "Son hesap hareketlerin"))
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        contentHost.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0).apply { weight = 1f })
        list.addView(bodyLabel("Bildirimler yükleniyor…"))
        lifecycleScope.launch {
            try {
                val result = api.notifications()
                list.removeAllViews()
                val unread = result.optInt("unread_count")
                val badge = TextView(this@MainActivity).apply {
                    text = "$unread okunmamış"
                    textSize = 12f
                    setTextColor(cyan)
                    setPadding(dp(12), dp(7), dp(12), dp(7))
                    background = GradientDrawable().apply { setColor(Color.rgb(18, 43, 53)); cornerRadius = dp(12).toFloat() }
                }
                list.addView(badge, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(10) })
                val notifications = result.getJSONArray("notifications")
                if (notifications.length() == 0) list.addView(emptyState("Yeni bildirimin yok."))
                for (i in 0 until notifications.length()) {
                    val n = notifications.getJSONObject(i)
                    val c = card()
                    val box = LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dp(16), dp(14), dp(16), dp(14))
                    }
                    box.addView(TextView(this@MainActivity).apply {
                        text = n.optString("actor_username", "Wiantex")
                        textSize = 15f
                        setTextColor(text)
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    })
                    box.addView(bodyLabel(n.optString("type", "Bildirim"), 12f, muted))
                    c.addView(box)
                    list.addView(c)
                }
            } catch (e: Exception) {
                list.removeAllViews()
                list.addView(emptyState(e.message ?: "Bildirimler yüklenemedi."))
            }
        }
        setContentView(shell)
    }

    private fun showProfile() {
        val shell = root()
        setupShell(shell, "profile")
        contentHost.addView(title("Profil", "Wiantex hesabın"))
        val lookup = input("Kullanıcı adı (boş = kendi profilin)")
        contentHost.addView(lookup, LinearLayout.LayoutParams.MATCH_PARENT, dp(52))
        contentHost.addView(pillButton("Profili Aç") { loadProfile(lookup.text.toString()) }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46)).apply { topMargin = dp(8) })
        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(12), 0, dp(10)) }
        scroll.addView(box)
        contentHost.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0).apply { weight = 1f })
        contentHost.addView(pillButton("Çıkış Yap") {
            api.clear()
            username = null
            csrf = ""
            showLogin()
        }, LinearLayout.LayoutParams.MATCH_PARENT, dp(46))
        loadProfile("", box)
        setContentView(shell)
    }

    private fun loadProfile(name: String, target: LinearLayout? = null) {
        val box = target ?: (contentHost.getChildAt(3) as ScrollView).getChildAt(0) as LinearLayout
        lifecycleScope.launch {
            try {
                val profile = api.profile(name.ifBlank { null }).getJSONObject("profile")
                box.removeAllViews()
                val c = card()
                val inner = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(18), dp(18), dp(18), dp(18))
                }
                inner.addView(TextView(this@MainActivity).apply {
                    text = profile.optString("username", "WTR")
                    textSize = 25f
                    setTextColor(text)
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                })
                inner.addView(bodyLabel(profile.optString("role_name", "Üye"), 13f, cyan))
                inner.addView(bodyLabel(profile.optString("bio", "Wiantex topluluğunun bir üyesi."), 14f, Color.rgb(215, 211, 228)))
                c.addView(inner)
                box.addView(c)

                val stats = card()
                val statsRow = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(dp(10), dp(14), dp(10), dp(14))
                }
                val values = listOf(
                    profile.optInt("topic_count") to "Konu",
                    profile.optInt("post_count") to "Mesaj",
                    profile.optInt("like_count") to "Beğeni"
                )
                values.forEach { (value, labelValue) ->
                    statsRow.addView(LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        addView(TextView(this@MainActivity).apply {
                            text = value.toString()
                            textSize = 21f
                            setTextColor(text)
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            gravity = Gravity.CENTER
                        })
                        addView(bodyLabel(labelValue, 11f, muted).apply { gravity = Gravity.CENTER })
                    }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                }
                stats.addView(statsRow)
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
            setPadding(dp(18), dp(26), dp(18), dp(26))
        }
        box.addView(TextView(this@MainActivity).apply {
            text = "•"
            textSize = 28f
            setTextColor(cyan)
            gravity = Gravity.CENTER
        })
        box.addView(bodyLabel(message, 14f, muted).apply { gravity = Gravity.CENTER })
        c.addView(box)
        return c
    }
}
