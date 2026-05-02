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
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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


@SuppressLint("SetJavaScriptEnabled")
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
    val url = remember(mediaType, tmdbId, season, episode) {
        if (mediaType == "tv" && season != null && episode != null) {
            "https://www.vidking.net/embed/tv/$tmdbId/$season/$episode"
        } else {
            "https://www.vidking.net/embed/movie/$tmdbId"
        }
    }

    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val isLoadingState = remember { mutableStateOf(true) }
    var isLoading by isLoadingState

    // Auto-record watch history with actual metadata from loaded details
    LaunchedEffect(tmdbId, mediaType) {
        val title = if (mediaType == "movie") {
            viewModel.movieDetails?.title ?: "Movie #$tmdbId"
        } else {
            viewModel.tvShowDetails?.name ?: "TV Show #$tmdbId"
        }
        val posterPath = if (mediaType == "movie") {
            viewModel.movieDetails?.poster_path
        } else {
            viewModel.tvShowDetails?.poster_path
        }
        viewModel.recordWatch(
            tmdbId = tmdbId,
            title = title,
            posterPath = posterPath,
            mediaType = mediaType,
            season = season,
            episode = episode
        )
    }

    // Allowed domains
    val allowedDomains = remember {
        setOf(
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
        setOf(
            "vibe-promo.com", "timesofindia.indiatimes.com",
            "betvibe", "bet365", "1xbet",
            "doubleclick.net", "googlesyndication.com", "googleadservices.com",
            "adclick", "popads", "popcash", "propellerads",
            "adsterra", "juicyads", "exoclick", "trafficjunky",
            "clickadu", "pushground",
            // Additional common ad networks
            "adnxs.com", "taboola.com", "outbrain.com",
            "pubmatic.com", "openx.net", "criteo.com",
            "amazon-adsystem.com", "moatads.com"
        )
    }

    // Pre-compute for fast lookup
    val isAllowedUrl = remember(allowedDomains) {
        { checkUrl: String? ->
            if (checkUrl == null) false
            else {
                val host = try { Uri.parse(checkUrl).host?.lowercase() ?: "" } catch (_: Exception) { "" }
                allowedDomains.any { host.contains(it) }
            }
        }
    }

    val isBlockedUrl = remember(blockedDomains) {
        { checkUrl: String? ->
            if (checkUrl == null) false
            else {
                val lower = checkUrl.lowercase()
                blockedDomains.any { lower.contains(it) }
            }
        }
    }

    // CSS + JS injection to block ads and improve performance
    val adBlockScript = remember {
        """
        javascript:(function() {
            // CSS: hide ad elements
            var style = document.createElement('style');
            style.innerHTML = `
                [id*='ad'], [class*='ad-'], [class*='popup'],
                [class*='overlay'], [id*='overlay'],
                [class*='modal'], [id*='modal'],
                [class*='banner'], [id*='banner'],
                iframe[src*='ad'], iframe[src*='pop'],
                div[onclick], a[target='_blank'][rel*='nofollow'] {
                    display: none !important;
                    visibility: hidden !important;
                    pointer-events: none !important;
                }
            `;
            document.head.appendChild(style);

            // Remove ad iframes dynamically
            var observer = new MutationObserver(function(mutations) {
                mutations.forEach(function(mutation) {
                    mutation.addedNodes.forEach(function(node) {
                        if (node.tagName === 'IFRAME' && node.src && 
                            (node.src.includes('ad') || node.src.includes('pop') || node.src.includes('banner'))) {
                            node.remove();
                        }
                        if (node.tagName === 'DIV' && node.onclick) {
                            node.onclick = null;
                        }
                    });
                });
            });
            observer.observe(document.body, { childList: true, subtree: true });
        })()
        """.trimIndent()
    }

    // ==============================
    // KEY FIX: Single retained WebView instance
    // Using remember{} so the SAME WebView persists across recompositions
    // (orientation changes). This prevents URL reload on rotation.
    // ==============================
    val webView = remember {
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
                    return true // Block unknown domains
                }

                // Block ad network requests at the resource level (faster than CSS hiding)
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    val reqUrl = request?.url?.toString() ?: return null
                    if (isBlockedUrl(reqUrl)) {
                        // Return empty response for blocked URLs — no network request made
                        return WebResourceResponse(
                            "text/plain", "utf-8",
                            java.io.ByteArrayInputStream(ByteArray(0))
                        )
                    }
                    return null
                }

                override fun onPageStarted(view: WebView?, pageUrl: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, pageUrl, favicon)
                    isLoadingState.value = true
                }

                override fun onPageFinished(view: WebView?, pageUrl: String?) {
                    super.onPageFinished(view, pageUrl)
                    isLoadingState.value = false
                    view?.evaluateJavascript(adBlockScript, null)
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

            // --- Performance-optimized WebSettings ---
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                mediaPlaybackRequiresUserGesture = false
                loadWithOverviewMode = true
                useWideViewPort = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                setSupportMultipleWindows(false)
                javaScriptCanOpenWindowsAutomatically = false
                setSupportZoom(false)
                allowFileAccess = false

                // --- Caching for faster loads ---
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

                // --- Rendering optimizations ---
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            }

            // Hardware-accelerated rendering
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            setBackgroundColor(android.graphics.Color.BLACK)

            // Load the URL once
            loadUrl(url)
        }
    }



    // Cleanup WebView on dispose to prevent memory leaks
    DisposableEffect(Unit) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

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

    // Back handler — try WebView back first, then nav back
    BackHandler {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            navController.popBackStack()
        }
    }

    // ==============================
    // UI: Single WebView used in both orientations
    // The SAME webView instance is re-parented, NOT recreated
    // ==============================
    if (isLandscape) {
        // --- LANDSCAPE: Full immersive player ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { webView },
                modifier = Modifier.fillMaxSize()
            )

            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                CircularProgressIndicator(color = Color(0xFFE50914))
            }
        }
    } else {
        // --- PORTRAIT: Top bar + player ---
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
                AndroidView(
                    factory = { webView },
                    modifier = Modifier.fillMaxSize()
                )

                AnimatedVisibility(
                    visible = isLoading,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    CircularProgressIndicator(color = Color(0xFFE50914))
                }
            }
        }
    }
}
