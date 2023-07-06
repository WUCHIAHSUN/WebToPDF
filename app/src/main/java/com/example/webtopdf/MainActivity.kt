package com.example.webtopdf

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.http.SslError
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.*

class MainActivity : AppCompatActivity() {

    private var webView: WebView ?= null
    private var webViewClient: WebViewClient? = null
    private var layout: LinearLayoutCompat ?= null
    private var newWebView: WebView?= null
    private val h52PdfTask = H52PdfTask()
    private var PdfSetting: PdfSetting?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview)
        layout = findViewById(R.id.layout)
        val signatureLayout: RelativeLayout = findViewById(R.id.signature_layout)
        val signatureView: SignatureView = findViewById(R.id.signature)
        signatureView.setPaintColor(Color.BLACK)
        signatureView.setPaintWidth(15)
        signatureView.setCanvasColor(Color.TRANSPARENT)
        val button: Button = findViewById(R.id.button)
        val btWrite: Button = findViewById(R.id.bt_write)
        button.setOnClickListener(View.OnClickListener {
//            if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()){
//                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
//                startActivity(intent)
//            }else {
                h52PdfTask.webViewToPdf(webView, this)
                signatureLayout.visibility = View.VISIBLE
                webView?.visibility = View.GONE
            //轉Uri
//            val uri = FileProvider.getUriForFile(applicationContext, BuildConfig.APPLICATION_ID + ".provider", h52PdfTask.pdfFile)
//            }
        })
        btWrite.setOnClickListener(View.OnClickListener {
            PdfSetting = PdfSetting(h52PdfTask.pdfFile, signatureView.getBitmap())
            PdfSetting?.saveBitmapPdf(PdfSetting?.PdfToBitmap(this))
            signatureLayout.visibility = View.GONE
            webView?.visibility = View.VISIBLE

        })
        openWeb("https://www.tentenmall.tv/")
    }


    private fun openWeb(webUrl: String){
        try {
            webView!!.loadUrl(webUrl)
        }catch (e : UnsupportedEncodingException) {
            e.printStackTrace();
        }
        setup_wv()
        setup_wv_client()
        setup_wv_chrome_client()
    }

    private fun setup_wv() {
        webView!!.setBackgroundColor(Color.TRANSPARENT)
        webView!!.settings.javaScriptEnabled = true // ◆ WebView 支援 JavaScript：後端頁面中含有 JavaScript
        webView!!.settings.javaScriptCanOpenWindowsAutomatically = true // 支持 JavaScript 調用 window.open()
        webView!!.settings.setSupportMultipleWindows(true) // 設置允許開啟多窗口
    }

    private fun setup_wv_client()
    {
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, newUrl: String): Boolean {
                return super.shouldOverrideUrlLoading(view, newUrl)
            }

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {

            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
            }
        }
        webView!!.setWebViewClient(webViewClient!!)
    }

    private fun setup_wv_chrome_client() {
        webView!!.setWebChromeClient(object : WebChromeClient() {
            override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
                newWebView = WebView(this@MainActivity)
                layout?.addView(newWebView) // WebView 加載到界面上
                val transport = resultMsg.obj as WebView.WebViewTransport // 讓新的 WebView 加載對應網址
                transport.webView = newWebView
                resultMsg.sendToTarget()
                return true
            }

            override fun onCloseWindow(window: WebView) { // JS 調用 .close() → 呼叫此方法
                super.onCloseWindow(window)
                if (newWebView != null) layout?.removeView(newWebView)
            }
        })
    }
}