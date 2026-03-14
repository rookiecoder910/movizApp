package com.example.movizapp.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import com.example.movizapp.viewmodel.MovieViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    mediaType: String,
    tmdbId: Int,
    season: Int? = null,
    episode: Int? = null,
    navController: NavController,
    viewModel: MovieViewModel
) {
    val url = if (mediaType == "tv" && season != null && episode != null) {
        "https://www.vidking.net/embed/tv/$tmdbId/$season/$episode"
    } else {
        "https://www.vidking.net/embed/movie/$tmdbId"
    }

    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var isLoading by remember { mutableStateOf(true) }

    // Auto-record watch history
    LaunchedEffect(tmdbId, mediaType) {
        viewModel.recordWatch(
            tmdbId = tmdbId,
            title = if (mediaType == "tv") "TV Show #$tmdbId" else "Movie #$tmdbId",
            posterPath = null,
            mediaType = mediaType,
            season = season,
            episode = episode
        )
    }


    // Allowed domains
    val allowedDomains = remember {
        listOf(
            "vidking.net", "www.vidking.net",
            "vidfast.pro", "vidfast.co",
            "embed.su", "vidsrc.me", "vidsrc.to", "vidsrc.xyz",
            "vidsrc.cc", "vidsrc.in", "vidsrc.net",
            "2embed.cc", "2embed.org",
            "autoembed.co", "multiembed.mov",
            "player.videasy.net", "videasy.net"
        )
    }

    val blockedDomains = remember {
        listOf(
            "vibe-promo.com", "timesofindia.indiatimes.com",
            "betvibe", "bet365", "1xbet",
            "doubleclick.net", "googlesyndication.com", "googleadservices.com",
            "adclick", "popads", "popcash", "propellerads",
            "adsterra", "juicyads", "exoclick", "trafficjunky",
            "clickadu", "pushground"
        )
    }

    fun isAllowedUrl(checkUrl: String?): Boolean {
        if (checkUrl == null) return false
        val host = try { Uri.parse(checkUrl).host?.lowercase() ?: "" } catch (e: Exception) { "" }
        return allowedDomains.any { host.contains(it) }
    }

    fun isBlockedUrl(checkUrl: String?): Boolean {
        if (checkUrl == null) return false
        val lower = checkUrl.lowercase()
        return blockedDomains.any { lower.contains(it) }
    }

    val adBlockCss = """
        javascript:(function() {
            var style = document.createElement('style');
            style.innerHTML = `
                [id*='ad'], [class*='ad-'], [class*='popup'],
                [class*='overlay'], [id*='overlay'],
                [class*='modal'], [id*='modal'],
                iframe[src*='ad'], iframe[src*='pop'],
                div[onclick], a[target='_blank'][rel*='nofollow'] {
                    display: none !important;
                    visibility: hidden !important;
                    pointer-events: none !important;
                }
            `;
            document.head.appendChild(style);
        })()
    """.trimIndent()

    // Immersive mode for landscape
    DisposableEffect(isLandscape) {
        if (activity != null) {
            val window = activity.window
            val insetsController = WindowCompat.getInsetsController(window, view)

            if (isLandscape) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        onDispose {
            if (activity != null) {
                val window = activity.window
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    BackHandler {
        navController.popBackStack()
    }

    if (isLandscape) {
        // --- LANDSCAPE: Full immersive player (VidKing controls only) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            PlayerWebView(
                url = url,
                isAllowedUrl = ::isAllowedUrl,
                isBlockedUrl = ::isBlockedUrl,
                adBlockCss = adBlockCss,
                onLoadingChanged = { isLoading = it },
                modifier = Modifier.fillMaxSize()
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFE50914)
                )
            }
        }
    } else {
        // --- PORTRAIT: Top bar + VidKing player controls only ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (mediaType == "tv") "S${season} · E${episode}" else "Now Playing",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0A0A0A)
                    )
                )
            },
            containerColor = Color.Black
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Black)
            ) {
                PlayerWebView(
                    url = url,
                    isAllowedUrl = ::isAllowedUrl,
                    isBlockedUrl = ::isBlockedUrl,
                    adBlockCss = adBlockCss,
                    onLoadingChanged = { isLoading = it },
                    modifier = Modifier.fillMaxSize()
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFFE50914)
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayerWebView(
    url: String,
    isAllowedUrl: (String?) -> Boolean,
    isBlockedUrl: (String?) -> Boolean,
    adBlockCss: String,
    onLoadingChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val requestUrl = request?.url?.toString() ?: return true
                        if (isBlockedUrl(requestUrl)) return true
                        if (isAllowedUrl(requestUrl)) return false
                        return true
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        onLoadingChanged(true)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLoadingChanged(false)
                        view?.evaluateJavascript(adBlockCss, null)
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onCreateWindow(
                        view: WebView?,
                        isDialog: Boolean,
                        isUserGesture: Boolean,
                        resultMsg: android.os.Message?
                    ): Boolean = false
                }

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.setSupportMultipleWindows(false)
                settings.javaScriptCanOpenWindowsAutomatically = false
                settings.setSupportZoom(false)
                settings.allowFileAccess = false
                
                // WebView cache optimization for mobile data
                settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                settings.databaseEnabled = true

                setBackgroundColor(android.graphics.Color.BLACK)
                loadUrl(url)
            }
        },
        modifier = modifier
    )
}
