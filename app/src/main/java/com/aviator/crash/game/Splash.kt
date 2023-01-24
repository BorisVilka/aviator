package com.aviator.crash.game

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.aviator.crash.game.databinding.ActivitySplashBinding

public class Splash : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var cached: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (getSharedPreferences("prefs", MODE_PRIVATE).getString("url", "") != null
            && !getSharedPreferences("prefs", MODE_PRIVATE).getString("url", "")!!.isEmpty()
        ) {
            Log.d("TAG", "CACHE")
            cached = true
            startActivity(Intent(applicationContext, ActivityWeb::class.java))
            finish()
        } else if (getSharedPreferences("prefs", MODE_PRIVATE).getString("url", "") == null) {
            Log.d("TAG", "CACHE EMPTY")
            cached = true
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)

        val settings: WebSettings = binding.web.getSettings()
        val manager = CookieManager.getInstance()
        manager.setAcceptCookie(true)
        settings.javaScriptEnabled = true
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        settings.setAppCacheEnabled(true)
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        binding.web.setWebViewClient(object : WebViewClient() {
            private var b = true
            private var count = 0
            private var count2 = 0
            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                Log.d("TAG", error.description.toString() + "")
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (cached) return
                super.onPageFinished(view, url)
                Log.d("TAG", "" + url + " " + url.contains("http://null/"))
                if (b) {
                    count2++
                    if (count == count2 && count > 1) {
                        getSharedPreferences("prefs", MODE_PRIVATE)
                            .edit()
                            .putString("url", url)
                            .apply()
                        startActivity(Intent(applicationContext, ActivityWeb::class.java))
                        finish()
                    }
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                Log.d("TAG", request.url.toString() + "")
                count++
                if (request.url.toString().contains("null")) {
                    b = false
                    getSharedPreferences("prefs", MODE_PRIVATE)
                        .edit()
                        .putString("url", null)
                        .apply()
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                }
                if (!b) {
                    getSharedPreferences("prefs", MODE_PRIVATE)
                        .edit()
                        .putString("url", "")
                        .apply()
                }
                view.loadUrl(request.url.toString())
                return true
            }
        })
        setContentView(binding.root)
        FirebaseRemoteConfig
            .getInstance()
            .fetchAndActivate()
            .addOnCompleteListener { task: Task<Boolean?>? ->
                val url = FirebaseRemoteConfig.getInstance().getString("url")
                Log.d("TAG", "CONFIG: $url")
                if (url.isEmpty()) {
                    getSharedPreferences("prefs", MODE_PRIVATE)
                        .edit()
                        .putString("url", null)
                        .apply()
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                }
                binding.web.loadUrl(url)
            }
    }
}