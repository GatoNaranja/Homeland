package gato.naranja.homeland;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class GNWebChromeClient extends WebChromeClient {

    private OpenFileChooserCallBack mOpenFileChooserCallBack;
    private CreateWindowCallBack mCreateWindowCallBack;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        if (mOpenFileChooserCallBack != null) {
            mOpenFileChooserCallBack.showFileChooserCallBack(filePathCallback, fileChooserParams);
        }
        return true;
    }

    public void setOpenFileChooserCallBack(OpenFileChooserCallBack callBack) {
        mOpenFileChooserCallBack = callBack;
    }

    public void setCreateWindowCallBack(CreateWindowCallBack callBack) {
        mCreateWindowCallBack = callBack;
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        if (mCreateWindowCallBack != null) {
            mCreateWindowCallBack.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
        }
        return true;
    }

    @Override
    public boolean onJsAlert(WebView webView, String url, String message, JsResult result) {
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());
        localBuilder.setMessage(message).setPositiveButton("确定",null);
        localBuilder.setCancelable(false);
        localBuilder.create().show();

        //注意:
        //必须要这一句代码:result.confirm()表示:
        //处理结果为确定状态同时唤醒WebCore线程
        //否则不能继续点击按钮
        result.confirm();
        return true;
    }



    public interface OpenFileChooserCallBack {

        void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType);

        void showFileChooserCallBack(ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams);
    }

    public interface CreateWindowCallBack {

        void onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg);

    }

}
