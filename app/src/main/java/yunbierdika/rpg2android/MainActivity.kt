package yunbierdika.rpg2android

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.webkit.WebViewAssetLoader
import yunbierdika.rpg2android.utils.JavaScriptInterface
import yunbierdika.rpg2android.utils.WriteLogToLocal

class MainActivity : ComponentActivity() {

    // WebView实例
    private lateinit var gameWebView: WebView

    // 日志输出实例
    private lateinit var writeLogToLocal: WriteLogToLocal

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 动态设置屏幕方向为横屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // 保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 创建 OnBackPressedCallback，拦截返回键误操作
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 弹出确认框
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("提示")
                    .setMessage("您确定要退出游戏吗？未保存的存档将会丢失。")
                    .setPositiveButton(
                        "确定"
                    ) { dialog: DialogInterface?, which: Int -> finish() }
                    .setNegativeButton("取消", null)
                    .show()
            }
        }

        // 将 callback 添加到 OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, callback)

        // 仅在savedInstanceState为空时加载初始URL
        if (savedInstanceState == null) {
            // 初始化日志输出实例
            writeLogToLocal = WriteLogToLocal(this)
            // WebView 配置
            gameWebView = WebView(this).apply {
                // 启用WebView中的JavaScript支持
                settings.javaScriptEnabled = true
                // 启用DOM存储支持
                settings.domStorageEnabled = true
                // 允许媒体内容在没有用户手势的情况下自动播放
                settings.mediaPlaybackRequiresUserGesture = false
                // 允许WebView适应屏幕宽度（启用视口支持）
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                // 允许WebView访问本地文件
                settings.allowFileAccess = true
                // 设置缓存模式为本地缓存模式
                settings.cacheMode = WebSettings.LOAD_CACHE_ONLY
                // 设置WebView自动加载图片，即使页面中有图片资源，WebView也会自动加载并显示
                settings.loadsImagesAutomatically = true
                // 启用WebView的多窗口支持，允许WebView在打开新链接时支持多窗口显示
                settings.setSupportMultipleWindows(true)
                // 允许JavaScript自动打开新窗口，当页面中的JavaScript代码尝试打开新窗口时，WebView会自动处理
                settings.javaScriptCanOpenWindowsAutomatically = true

                // 设置WebView背景色默认为黑色
                setBackgroundColor(Color.BLACK)
                // 使用硬件加速
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                // 添加JavaScript接口
                addJavascriptInterface(
                    JavaScriptInterface(this@MainActivity, writeLogToLocal),
                    "AndroidBridge"
                )
                // 使用 WebViewAssetLoader 代替已弃用的 allowUniversalAccessFromFileURLs 方法
                val assetLoader = WebViewAssetLoader.Builder().addPathHandler("/assets/",
                    WebViewAssetLoader.AssetsPathHandler(this@MainActivity)).build()
                webViewClient = object : WebViewClient() {
                    // 使用 WebViewAssetLoader
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        return assetLoader.shouldInterceptRequest(request.url)
                    }

                    // 捕获 WebView 错误
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        // 捕捉加载错误
                        val errorMessage = "WebView Error: ${error?.description} URL: ${request?.url}"
                        writeLogToLocal.logError(errorMessage)
                    }

                    // 捕获 HTTP 错误
                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?
                    ) {
                        super.onReceivedHttpError(view, request, errorResponse)
                        val errorMessage = "HTTP Error: ${errorResponse?.statusCode} URL: ${request?.url}"
                        writeLogToLocal.logError(errorMessage)
                    }
                }
                // 加载游戏
                loadUrl("https://appassets.androidplatform.net/assets/index.html")
            }
        }

        setContentView(gameWebView)
    }

    // 视图重新聚焦时执行全屏模式
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val controller = window.insetsController
            controller?.let {
                it.hide(WindowInsets.Type.systemBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    override fun onPause() {
        super.onPause()
        gameWebView.onPause()   // 暂停 WebView
        gameWebView.pauseTimers()   // 暂停 WebView 中的定时器
    }

    override fun onResume() {
        super.onResume()
        gameWebView.onResume() // 恢复 WebView
        gameWebView.resumeTimers() // 恢复 WebView 中的定时器
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止加载内容
        gameWebView.stopLoading()
        gameWebView.loadUrl("about:blank")
        // 清理历史和资源
        gameWebView.clearHistory()
        gameWebView.removeAllViews()
        // 销毁 WebView
        gameWebView.destroy()
    }

    // 保存 WebView 状态，应对切换应用时刷新问题
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 保存状态
        gameWebView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // 恢复状态
        gameWebView.restoreState(savedInstanceState)
    }
}
