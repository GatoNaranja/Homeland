package gato.naranja.homeland;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class GNWebView extends WebView {

    private GNWebChromeClient webChromeClient;
    private GNWebViewClient webViewClient;

    public GNWebView(Context context) {
        super(context);
        initWebView();
    }

    public GNWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWebView();
    }

    public GNWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        webChromeClient = new GNWebChromeClient();
        webViewClient = new GNWebViewClient();
        setWebChromeClient(webChromeClient);
        setWebViewClient(webViewClient);
        WebSettings webviewSettings = getSettings();
        // 不支持缩放
        webviewSettings.setSupportZoom(true);
        // 自适应屏幕大小
        webviewSettings.setUseWideViewPort(true);
        webviewSettings.setLoadWithOverviewMode(true);
        String cacheDirPath = getContext().getFilesDir().getAbsolutePath() + "cache/";
        webviewSettings.setAppCachePath(cacheDirPath);
        webviewSettings.setAppCacheEnabled(true);
        webviewSettings.setDomStorageEnabled(true);
        webviewSettings.setAllowFileAccess(true);
//        webviewSettings.setAppCacheMaxSize(1024 * 1024 * 8);
        webviewSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webviewSettings.setBuiltInZoomControls(true);
        webviewSettings.setJavaScriptEnabled(true);
    }

    public void setOpenFileChooserCallBack(GNWebChromeClient.OpenFileChooserCallBack callBack) {
        webChromeClient.setOpenFileChooserCallBack(callBack);
    }

    public void setCreateWindowCallBack(GNWebChromeClient.CreateWindowCallBack callBack) {
        webChromeClient.setCreateWindowCallBack(callBack);
    }

    public void setSwipeRefreshLayout(SwipeRefreshLayout srl){
        webViewClient.setSwipeRefreshLayout(srl);
    }
}
