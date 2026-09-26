package com.hyperion.marinareviewer

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowInsetsController
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup immersive status bar and navigation bar colors
        setupSystemBars()

        webView = WebView(this).apply {
            id = View.generateViewId()
            setBackgroundColor(ContextCompat.getColor(context, R.color.primary_variant))
        }
        setContentView(webView)

        configureWebSettings()
        setupClients()
        setupBackNavigation()
        setupDownloadListener()

        // Load the local offline application or packaged assets
        // For standalone production builds, assets are bundled in file:///android_asset/dist/index.html
        // or loaded via local asset loader
        val localAssetUrl = "file:///android_asset/dist/index.html"
        val fallbackDevUrl = "https://ais-dev-jbxucaox755rmvqdf6jooy-56793989426.asia-southeast1.run.app"

        try {
            assets.open("dist/index.html").close()
            webView.loadUrl(localAssetUrl)
        } catch (e: Exception) {
            // Assets not bundled in dev mode; load primary app instance
            webView.loadUrl(fallbackDevUrl)
        }
    }

    private fun setupSystemBars() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.status_bar)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.nav_bar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0, // Clear LIGHT status bar flag -> keep light text on dark navy background
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebSettings() {
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            allowFileAccess = true
            allowContentAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            userAgentString = "$userAgentString HyperionMARINA/1.0"
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }
    }

    private fun setupClients() {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")) {
                    return false // Let WebView handle
                }
                return true
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                return super.onConsoleMessage(consoleMessage)
            }
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupDownloadListener() {
        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
            try {
                if (url.startsWith("blob:") || url.startsWith("data:")) {
                    Toast.makeText(this, "Export downloaded", Toast.LENGTH_SHORT).show()
                    return@setDownloadListener
                }

                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setMimeType(mimetype)
                    addRequestHeader("User-Agent", userAgent)
                    setDescription("Downloading Hyperion MARINA Reviewer File")
                    setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        URLUtil.guessFileName(url, contentDisposition, mimetype)
                    )
                }

                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(this, "Downloading file to Downloads...", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Download error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
