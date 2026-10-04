package com.pulsechat.app.ui.pulse

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YoutubePlayer(
    embedUrl: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
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
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
                setBackgroundColor(0xFF000000.toInt())
                loadDataWithBaseURL(
                    "https://www.youtube.com",
                    buildHtml(embedUrl),
                    "text/html",
                    "utf-8",
                    null
                )
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                "https://www.youtube.com",
                buildHtml(embedUrl),
                "text/html",
                "utf-8",
                null
            )
        }
    )

    DisposableEffect(Unit) {
        onDispose { /* WebView cleaned by Compose */ }
    }
}

private fun buildHtml(embedUrl: String): String = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<style>
  * { margin:0; padding:0; box-sizing:border-box; }
  html, body { width:100%; height:100%; background:#000; overflow:hidden; }
  iframe { position:absolute; top:0; left:0; width:100%; height:100%; border:0; }
</style>
</head>
<body>
<iframe
  src="$embedUrl"
  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; fullscreen"
  allowfullscreen
  playsinline
></iframe>
</body>
</html>
""".trimIndent()

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YoutubeShortPlayer(
    embedUrl: String,
    modifier: Modifier = Modifier
) {
    YoutubePlayer(embedUrl = embedUrl, modifier = modifier)
}
