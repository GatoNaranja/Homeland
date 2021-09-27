package gato.naranja.homeland;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WindowWebFragment extends Fragment {
    private Message message;
    private WebView.WebViewTransport mTransport;

    private GNWebView  mWv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_window, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWv = view.findViewById(R.id.webview);
        mTransport = (WebView.WebViewTransport) message.obj;
        mTransport.setWebView(mWv);
        message.sendToTarget();
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public static WindowWebFragment newInstance() {
        Bundle bundle = new Bundle();
        WindowWebFragment fragment = new WindowWebFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}
