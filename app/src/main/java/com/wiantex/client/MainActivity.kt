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
    private lateinit var contentHost: LinearLayout

    private val bgColor = Color.rgb(7, 6, 17)
    private val surfaceColor = Color.rgb(16, 14, 28)
    private val surfaceAltColor = Color.rgb(25, 22, 43)
    private val purpleColor = Color.rgb(105, 70, 230)
    private val purpleDarkColor = Color.rgb(68, 43, 152)
    private val cyanColor = Color.rgb(77, 212, 255)
    private val mutedColor = Color.rgb(160, 156, 180)
    private val borderColor = Color.rgb(48, 43, 68)

    private var username: String? = null
    private var csrfToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bgColor
        window.navigationBarColor = bgColor

        api = Api(this)
        username = api.username()
        csrfToken = api.csrf()

        if (username.isNullOrBlank()) {
            showLogin()
        } else {
            showHome()
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun root(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(bgColor)
        setPadding(dp(16), dp(10), dp(16), 0)
    }

    private fun roundedBackground(
        color: Int,
        radiusDp: Int = 18,
        strokeColor: Int? = null
    ): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
        strokeColor?.let { setStroke(dp(1), it) }
    }

    private fun pageTitle(titleValue: String, subtitle: String? = null): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(2), dp(5), dp(2), dp(12))

            addView(TextView(this@MainActivity).apply {
                text = titleValue
                textSize = 28f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
            })

            subtitle?.takeIf { it.isNotBlank() }?.let { value ->
                addView(TextView(this@MainActivity).apply {
                    text = value
                    textSize = 13f
                    setTextColor(mutedColor)
                    setPadding(0, dp(4), 0, 0)
                })
            }
        }

    private fun label(
        value: String,
        size: Float = 14f,
        color: Int = mutedColor
    ): TextView = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        setPadding(0, dp(3), 0, dp(3))
    }

    private fun pill(
        value: String,
        backgroundColor: Int = surfaceAltColor,
        foregroundColor: Int = Color.WHITE
    ): TextView = TextView(this).apply {
        text = value
        textSize = 11f
        setTextColor(foregroundColor)
        gravity = Gravity.CENTER
        setPadding(dp(10), dp(7), dp(10), dp(7))
        background = roundedBackground(backgroundColor, 30)
    }

    private fun input(hintValue: String, password: Boolean = false): EditText =
        EditText(this).apply {
            hint = hintValue
            textSize = 15f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(112, 108, 128))
            setPadding(dp(14), 0, dp(14), 0)
            minimumHeight = dp(52)
            background = roundedBackground(surfaceAltColor, 16, borderColor)
            if (password) {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            setSingleLine(true)
        }

    private fun primaryButton(labelValue: String, click: () -> Unit): MaterialButton =
        MaterialButton(this).apply {
            text = labelValue
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(purpleColor)
            cornerRadius = dp(16)
            insetTop = 0
            insetBottom = 0
            minHeight = dp(48)
            setOnClickListener { click() }
        }

    private fun secondaryButton(labelValue: String, click: () -> Unit): MaterialButton =
        MaterialButton(this).apply {
            text = labelValue
            textSize = 13f
            isAllCaps = false
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(surfaceAltColor)
            cornerRadius = dp(16)
            strokeWidth = dp(1)
            strokeColor = ColorStateList.valueOf(borderColor)
            insetTop = 0
            insetBottom = 0
            minHeight = dp(46)
            setOnClickListener { click() }
        }

    private fun card(): MaterialCardView = MaterialCardView(this).apply {
        radius = dp(20).toFloat()
        cardElevation = 0f
        setCardBackgroundColor(surfaceColor)
        strokeWidth = dp(1)
        strokeColor = borderColor
        setLayoutParams(
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(10)
            }
        )
    }

    private fun cardContent(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(15), dp(16), dp(15))
    }

    private fun logoView(heightDp: Int, full: Boolean = true): ImageView =
        ImageView(this).apply {
            setImageResource(if (full) R.drawable.wiantex_logo else R.drawable.wiantex_mark)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(heightDp)
            )
        }

    private fun navItem(
        icon: String,
        labelValue: String,
        page: String,
        selected: Boolean
    ): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(2), dp(7), dp(2), dp(6))
        background = roundedBackground(
            if (selected) Color.rgb(43, 31, 79) else Color.TRANSPARENT,
            16
        )
        addView(TextView(this@MainActivity).apply {
            text = icon
            textSize = 19f
            gravity = Gravity.CENTER
            setTextColor(if (selected) cyanColor else mutedColor)
        }, LinearLayout.LayoutParams.MATCH_PARENT, dp(27))
        addView(TextView(this@MainActivity).apply {
            text = labelValue
            textSize = 10.5f
            gravity = Gravity.CENTER
            setTextColor(if (selected) Color.WHITE else mutedColor)
        }, LinearLayout.LayoutParams.MATCH_PARENT, dp(20))
        setOnClickListener { showPage(page) }
    }

    private fun installShell(rootView: LinearLayout, currentPage: String) {
        contentHost = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        rootView.addView(contentHost)

        val navCard = MaterialCardView(this).apply {
            radius = dp(24).toFloat()
            cardElevation = 0f
            setCardBackgroundColor(surfaceColor)
            strokeWidth = dp(1)
            strokeColor = borderColor
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(82)
            ).apply {
                topMargin = dp(8)
                bottomMargin = dp(12)
            }
        }

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(5), dp(5), dp(5), dp(5))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val navItems = listOf(
            Triple("⌂", "Ana", "home"),
            Triple("▦", "Forum", "forum"),
            Triple("✉", "Mesaj", "messages"),
            Triple("♢", "Bildirim", "notifications"),
            Triple("◉", "Profil", "profile")
        )

        navItems.forEach { (icon, labelValue, page) ->
            val item = navItem(icon, labelValue, page, currentPage == page)
            item.layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            ).apply {
                leftMargin = dp(3)
                rightMargin = dp(3)
            }
            nav.addView(item)
        }

        navCard.addView(nav)
        rootView.addView(navCard)
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

    private fun setRoot(rootView: LinearLayout) {
        setContentView(rootView)
    }

    private fun scrollContent(body: LinearLayout): ScrollView = ScrollView(this).apply {
        isFillViewport = true
        addView(body)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
    }

    private fun actionCard(
        titleValue: String,
        description: String,
        actionLabel: String,
        click: () -> Unit
    ): MaterialCardView {
        val outer = card()
        val body = cardContent()

        body.addView(TextView(this).apply {
            text = titleValue
            textSize = 17f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        })

        body.addView(label(description))

        val action = primaryButton(actionLabel, click)
        action.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(44)
        ).apply {
            topMargin = dp(9)
        }
        body.addView(action)

        outer.addView(body)
        return outer
    }

    private fun showLogin() {
        val rootView = root()
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(8), dp(22), dp(8), dp(28))
        }

        body.addView(logoView(185, true))
        body.addView(label("Wiantex Android Client", 15f, cyanColor).apply {
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        })
        body.addView(label("Forum • mesajlar • bildirimler • profil", 13f, mutedColor).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(5), 0, dp(20))
        })

        val box = card()
        val inner = cardContent()
        val userInput = input("Kullanıcı adı veya e-posta")
        val passInput = input("Şifre", true)
        val status = label("", 13f, Color.rgb(255, 100, 125)).apply {
            gravity = Gravity.CENTER
        }

        userInput.layoutParams = fieldParams()
        passInput.layoutParams = fieldParams()
        status.layoutParams = fieldParams()

        inner.addView(userInput)
        inner.addView(passInput)
        inner.addView(status)

        val loginButton = primaryButton("Giriş Yap") {
            lifecycleScope.launch {
                try {
                    val result = api.login(
                        userInput.text.toString().trim(),
                        passInput.text.toString()
                    )
                    if (result.optBoolean("ok")) {
                        val user = result.optJSONObject("user")
                        username = user?.optString("username").orEmpty()
                        csrfToken = result.optString("csrf_token")
                        api.saveSession(username.orEmpty(), csrfToken)
                        showHome()
                    } else {
                        status.text = result.optString("message", "Giriş başarısız")
                    }
                } catch (e: Exception) {
                    status.text = e.message ?: "Bağlantı hatası"
                }
            }
        }
        loginButton.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(52)
        )
        inner.addView(loginButton)
        box.addView(inner)
        body.addView(box)

        rootView.addView(scrollContent(body))
        setRoot(rootView)
    }

    private fun fieldParams(): LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        dp(52)
    ).apply {
        topMargin = dp(6)
        bottomMargin = dp(6)
    }

    private fun heroCard(): MaterialCardView {
        val outer = card()
        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(18), dp(12), dp(18), dp(18))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(purpleDarkColor, Color.rgb(28, 21, 49))
            ).apply {
                cornerRadius = dp(20).toFloat()
            }
        }

        inner.addView(logoView(105, false))
        inner.addView(TextView(this).apply {
            text = "Hoş geldin, ${username ?: "WTR"}"
            textSize = 23f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        })
        inner.addView(label(
            "Yeni konuları keşfet, mesajlaş ve topluluğa katıl.",
            13f,
            Color.rgb(226, 221, 240)
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(5), 0, 0)
        })

        outer.addView(inner)
        return outer
    }

    private fun showHome() {
        val rootView = root()
        installShell(rootView, "home")
        contentHost.addView(pageTitle("Ana Sayfa", "Wiantex topluluğuna hızlı erişim"))

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(8))
        }
        body.addView(heroCard())
        body.addView(label("HIZLI ERİŞİM", 11f, cyanColor).apply {
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(2), dp(4), 0, dp(8))
        })
        body.addView(actionCard(
            "Forum",
            "Güncel konuları ve tartışmaları görüntüle.",
            "Foruma Git"
        ) { showForum() })
        body.addView(actionCard(
            "Mesajlar",
            "Özel konuşmalarını aç ve mesaj gönder.",
            "Mesajlara Git"
        ) { showMessages() })
        body.addView(actionCard(
            "Bildirimler",
            "Hesabındaki son hareketleri kontrol et.",
            "Bildirimleri Gör"
        ) { showNotifications() })

        contentHost.addView(scrollContent(body))
        setRoot(rootView)
    }

    private fun showForum() {
        val rootView = root()
        installShell(rootView, "forum")
        contentHost.addView(pageTitle("Forum", "Topluluğun güncel konuları"))

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(8))
        }
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        body.addView(list)
        contentHost.addView(scrollContent(body))
        list.addView(label("Konular yükleniyor…"))
        setRoot(rootView)

        lifecycleScope.launch {
            try {
                val topics = api.forum().getJSONArray("topics")
                list.removeAllViews()
                if (topics.length() == 0) {
                    list.addView(emptyState("Henüz konu bulunamadı."))
                }
                for (i in 0 until topics.length()) {
                    val topic = topics.getJSONObject(i)
                    val c = card()
                    val b = cardContent()
                    b.addView(TextView(this@MainActivity).apply {
                        text = topic.optString("title", "Başlıksız konu")
                        textSize = 16f
                        setTextColor(Color.WHITE)
                        typeface = Typeface.DEFAULT_BOLD
                    })
                    b.addView(label(
                        "${topic.optString("category_name", "Genel")}  •  ${topic.optString("username", "Üye")}",
                        12f
                    ))
                    c.addView(b)
                    list.addView(c)
                }
            } catch (e: Exception) {
                list.removeAllViews()
                list.addView(emptyState(e.message ?: "Forum yüklenemedi."))
            }
        }
    }

    private fun showMessages() {
        val rootView = root()
        installShell(rootView, "messages")
        contentHost.addView(pageTitle("Mesajlar", "Özel konuşmaların burada"))

        val target = input("Kullanıcı adı")
        target.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply { bottomMargin = dp(8) }
        contentHost.addView(target)

        val openButton = primaryButton("Konuşmayı Aç") { loadMessages(target) }
        openButton.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(46)
        )
        contentHost.addView(openButton)

        val messagesBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(10), 0, dp(10))
        }
        val messageScroll = ScrollView(this).apply {
            isFillViewport = true
            addView(messagesBox)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply { topMargin = dp(4) }
        }
        contentHost.addView(messageScroll)

        val composer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(7), 0, dp(8))
        }
        val body = input("Mesaj yaz…")
        body.setSingleLine(false)
        body.maxLines = 4
        body.gravity = Gravity.TOP or Gravity.START
        body.layoutParams = LinearLayout.LayoutParams(0, dp(56), 1f).apply {
            rightMargin = dp(8)
        }
        composer.addView(body)

        val sendButton = primaryButton("Gönder") {
            val targetName = target.text.toString().trim()
            val message = body.text.toString().trim()
            if (targetName.isBlank() || message.isBlank()) return@primaryButton
            lifecycleScope.launch {
                try {
                    api.sendMessage(targetName, message, csrfToken)
                    body.setText("")
                    loadMessages(target, messagesBox)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        e.message ?: "Mesaj gönderilemedi",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        sendButton.layoutParams = LinearLayout.LayoutParams(dp(96), dp(56))
        composer.addView(sendButton)
        contentHost.addView(composer)

        setRoot(rootView)
    }

    private fun loadMessages(target: EditText, existingBox: LinearLayout? = null) {
        val targetName = target.text.toString().trim()
        if (targetName.isBlank()) return

        val box = existingBox ?: run {
            val scroll = contentHost.children().filterIsInstance<ScrollView>().firstOrNull()
                ?: return
            scroll.getChildAt(0) as? LinearLayout ?: return
        }

        lifecycleScope.launch {
            try {
                val messages = api.messages(targetName).getJSONArray("messages")
                box.removeAllViews()
                if (messages.length() == 0) {
                    box.addView(emptyState("Bu konuşmada henüz mesaj yok."))
                }
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
            setTextColor(Color.WHITE)
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = roundedBackground(
                if (mine) purpleDarkColor else surfaceAltColor,
                17,
                if (mine) null else borderColor
            )
            maxWidth = dp(315)
        })
        return row
    }

    private fun showNotifications() {
        val rootView = root()
        installShell(rootView, "notifications")
        contentHost.addView(pageTitle("Bildirimler", "Son hesap hareketlerin"))

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(8))
        }
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        body.addView(list)
        contentHost.addView(scrollContent(body))
        list.addView(label("Bildirimler yükleniyor…"))
        setRoot(rootView)

        lifecycleScope.launch {
            try {
                val result = api.notifications()
                list.removeAllViews()
                list.addView(pill(
                    "${result.optInt("unread_count")} okunmamış",
                    Color.rgb(18, 44, 55),
                    cyanColor
                ))
                val notifications = result.getJSONArray("notifications")
                if (notifications.length() == 0) {
                    list.addView(emptyState("Yeni bildirimin yok."))
                }
                for (i in 0 until notifications.length()) {
                    val n = notifications.getJSONObject(i)
                    val c = card()
                    val b = cardContent()
                    b.addView(TextView(this@MainActivity).apply {
                        text = n.optString("actor_username", "Wiantex")
                        textSize = 15f
                        setTextColor(Color.WHITE)
                        typeface = Typeface.DEFAULT_BOLD
                    })
                    b.addView(label(n.optString("type", "Bildirim"), 12f))
                    c.addView(b)
                    list.addView(c)
                }
            } catch (e: Exception) {
                list.removeAllViews()
                list.addView(emptyState(e.message ?: "Bildirimler yüklenemedi."))
            }
        }
    }

    private fun showProfile() {
        val rootView = root()
        installShell(rootView, "profile")
        contentHost.addView(pageTitle("Profil", "Wiantex hesabın"))

        val lookup = input("Kullanıcı adı (boş = kendi profilin)")
        lookup.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply { bottomMargin = dp(8) }
        contentHost.addView(lookup)

        val openButton = primaryButton("Profili Aç") {
            loadProfile(lookup.text.toString())
        }
        openButton.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(46)
        )
        contentHost.addView(openButton)

        val profileBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, dp(10))
        }
        contentHost.addView(scrollContent(profileBox))

        val logout = secondaryButton("Çıkış Yap") {
            api.clear()
            username = null
            csrfToken = ""
            showLogin()
        }
        logout.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(46)
        ).apply { bottomMargin = dp(8) }
        contentHost.addView(logout)

        setRoot(rootView)
        loadProfile("", profileBox)
    }

    private fun loadProfile(name: String, targetBox: LinearLayout? = null) {
        val box = targetBox ?: run {
            val scrolls = contentHost.children().filterIsInstance<ScrollView>()
            val scroll = scrolls.firstOrNull() ?: return
            scroll.getChildAt(0) as? LinearLayout ?: return
        }

        lifecycleScope.launch {
            try {
                val profile = api.profile(name.ifBlank { null }).getJSONObject("profile")
                box.removeAllViews()

                val profileCard = card()
                val inner = cardContent()
                inner.addView(logoView(90, false))
                inner.addView(TextView(this@MainActivity).apply {
                    text = profile.optString("username", "WTR")
                    textSize = 27f
                    setTextColor(Color.WHITE)
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                })
                inner.addView(pill(
                    profile.optString("role_name", "Üye"),
                    Color.rgb(25, 42, 49),
                    cyanColor
                ).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        dp(34)
                    ).apply {
                        topMargin = dp(6)
                        gravity = Gravity.CENTER_HORIZONTAL
                    }
                })
                inner.addView(label(
                    profile.optString("bio", "Wiantex topluluğunun bir üyesi."),
                    13.5f,
                    Color.rgb(216, 212, 228)
                ).apply {
                    gravity = Gravity.CENTER
                    setPadding(0, dp(12), 0, dp(3))
                })
                profileCard.addView(inner)
                box.addView(profileCard)

                val statsCard = card()
                val statsRow = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(dp(8), dp(14), dp(8), dp(14))
                }
                listOf(
                    profile.optInt("topic_count") to "Konu",
                    profile.optInt("post_count") to "Mesaj",
                    profile.optInt("like_count") to "Beğeni"
                ).forEach { (value, statLabel) ->
                    val stat = LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                    }
                    stat.addView(TextView(this@MainActivity).apply {
                        text = value.toString()
                        textSize = 21f
                        setTextColor(Color.WHITE)
                        typeface = Typeface.DEFAULT_BOLD
                        gravity = Gravity.CENTER
                    })
                    stat.addView(TextView(this@MainActivity).apply {
                        text = statLabel
                        textSize = 11f
                        setTextColor(mutedColor)
                        gravity = Gravity.CENTER
                        setPadding(0, dp(3), 0, 0)
                    })
                    stat.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    statsRow.addView(stat)
                }
                statsCard.addView(statsRow)
                box.addView(statsCard)
            } catch (e: Exception) {
                box.removeAllViews()
                box.addView(emptyState(e.message ?: "Profil yüklenemedi."))
            }
        }
    }

    private fun emptyState(message: String): MaterialCardView {
        val c = card()
        val b = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(24), dp(18), dp(24))
        }
        b.addView(TextView(this).apply {
            text = "✦"
            textSize = 25f
            setTextColor(cyanColor)
            gravity = Gravity.CENTER
        })
        b.addView(label(message, 13f, mutedColor).apply {
            gravity = Gravity.CENTER
        })
        c.addView(b)
        return c
    }

    private fun LinearLayout.children(): Sequence<View> = sequence {
        for (index in 0 until childCount) {
            yield(getChildAt(index))
        }
    }
}
