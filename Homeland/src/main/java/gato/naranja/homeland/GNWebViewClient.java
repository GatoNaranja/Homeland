package gato.naranja.homeland;

import android.graphics.Bitmap;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static gato.naranja.homeland.MainActivity.sp;

public class GNWebViewClient extends WebViewClient {

    private SwipeRefreshLayout srl;

    public void setSwipeRefreshLayout(SwipeRefreshLayout srl){
        this.srl = srl;
    }

    @Override
    public void onPageFinished(WebView view, String url) {//页面加载完成
        srl.setRefreshing(false);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {//页面开始加载
        srl.setRefreshing(true);
        CookieManager cm = CookieManager.getInstance();
        String cookie = cm.getCookie(url);
        sp.edit().putString("cookie", cookie).apply();
    }


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return super.shouldOverrideUrlLoading(view, url);
    }

}
