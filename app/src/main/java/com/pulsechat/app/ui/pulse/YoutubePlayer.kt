package com.pulsechat.app.ui.pulse

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * YouTube embed via WebView.
 * Uses youtube-nocookie + mobile UA to reduce "Video unavailable" errors.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YoutubePlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    autoplay: Boolean = true
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            // Mobile Chrome UA — desktop UA often triggers "unavailable" in embeds
            settings.userAgentString =
                "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean = false
            }
            setBackgroundColor(0xFF000000.toInt())
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { webView },
        update = { view ->
            val html = buildEmbedHtml(videoId, autoplay)
            view.loadDataWithBaseURL(
                "https://www.youtube-nocookie.com",
                html,
                "text/html",
                "utf-8",
                null
            )
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.destroy()
        }
    }
}

@Composable
fun YoutubeShortPlayer(
    videoId: String,
    modifier: Modifier = Modifier
) {
    YoutubePlayer(videoId = videoId, modifier = modifier, autoplay = true)
}

private fun buildEmbedHtml(videoId: String, autoplay: Boolean): String {
    val ap = if (autoplay) "1" else "0"
    // nocookie + modestbranding + playsinline reduces block rates on Android WebView
    val src =
        "https://www.youtube-nocookie.com/embed/$videoId" +
            "?playsinline=1&autoplay=$ap&mute=0&rel=0&modestbranding=1" +
            "&controls=1&fs=1&enablejsapi=1&iv_load_policy=3&origin=https://www.youtube-nocookie.com"

    return """
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<style>
  * { margin:0; padding:0; box-sizing:border-box; }
  html, body { width:100%; height:100%; background:#000; overflow:hidden; }
  .wrap { position:fixed; inset:0; }
  iframe { width:100%; height:100%; border:0; }
</style>
</head>
<body>
<div class="wrap">
<iframe
  id="player"
  src="$src"
  title="YouTube"
  frameborder="0"
  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; fullscreen; web-share"
  allowfullscreen
  referrerpolicy="strict-origin-when-cross-origin"
></iframe>
</div>
</body>
</html>
""".trimIndent()
}
