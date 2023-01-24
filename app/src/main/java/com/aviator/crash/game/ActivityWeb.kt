package com.aviator.crash.game

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.WindowManager
import android.webkit.*
import com.aviator.crash.game.databinding.ActivityWebBinding

class ActivityWeb : AppCompatActivity() {

    private lateinit var binding: ActivityWebBinding
    private lateinit var webView: WebView
    private var uploadMessage: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(binding.root)
        webView = binding.web
        if(savedInstanceState!=null)
            webView.restoreState(savedInstanceState.getBundle("webViewState")!!)
        else {
            var cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            var settings = webView.settings
            settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.setGeolocationEnabled(true)
            settings.allowContentAccess = true
            settings.blockNetworkLoads = false
            settings.blockNetworkImage = false
            settings.safeBrowsingEnabled = true
            settings.loadWithOverviewMode = true
            settings.setSupportMultipleWindows(false)
            settings.offscreenPreRaster = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            settings.setAppCacheEnabled(true)
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.javaScriptEnabled = true

            webView.webChromeClient = object : WebChromeClient() {

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    if(uploadMessage!=null) {
                        uploadMessage!!.onReceiveValue(null)
                        uploadMessage = null
                    }

                    uploadMessage = filePathCallback

                    val intent = fileChooserParams!!.createIntent()

                    try {
                        startActivityForResult(intent,100)
                    } catch (e: Exception) {
                        uploadMessage = null;
                        return false
                    }

                    return true
                }

                override fun onCreateWindow(
                    view: WebView?,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message?
                ): Boolean {
                    var transport = resultMsg!!.obj as WebView.WebViewTransport
                    transport.webView = webView
                    resultMsg.sendToTarget()
                    return true
                }
            }
            webView.webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    view!!.loadUrl(request!!.url.toString())
                    return true
                }
            }
            Log.d("TAG",getSharedPreferences("prefs", MODE_PRIVATE).getString("url","")!!)
            binding.web.loadUrl(getSharedPreferences("prefs", MODE_PRIVATE).getString("url","")!!)
        }
    }

    override fun onBackPressed() {
        if(webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==100) {
            if(uploadMessage==null) return
            uploadMessage!!.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode,intent))
            uploadMessage = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var bundle = Bundle()
        webView.saveState(bundle)
        outState.putBundle("webViewState",bundle)
    }
}