package com.fashiongo.app.views;

import android.os.Bundle;
import android.view.View;

import com.fashiongo.app.R;
import com.fashiongo.app.commons.ThreadManager;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.util.HashMap;

import okhttp3.internal.Util;
import wendu.webviewjavascriptbridge.WVJBWebView;

/**
 * Created by stevejobs on 18/1/31.
 */

public class MainActivity extends BaseActivity {

    Tencent mTencent = null;
    String mAppid = "1106716594";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (mTencent == null) {
            mTencent = Tencent.createInstance(mAppid, this);
        }

        final WVJBWebView webView = (WVJBWebView) findViewById(R.id.webview);

        webView.registerHandler("testJavaCallback", new WVJBWebView.WVJBHandler() {
            @Override
            public void handler(Object data, WVJBWebView.WVJBResponseCallback callback) {
                callback.onResult("Response from testJavaCallback");
            }
        });

        HashMap<String, String> data = new HashMap<String, String>() {{
            put("greetingFromJava", "Hi there, JS!");
        }};
//        webView.callHandler("testJavascriptHandler", new JSONObject(data), new WVJBWebView.WVJBResponseCallback() {
//            @Override
//            public void onResult(Object data) {
//                Log.d("testJavascriptHandler", data.toString());
//            }
//        });

        webView.loadUrl("http://139.199.13.114:8888/fgapp/login.html");

    }

    IUiListener qqShareListener = new IUiListener() {
        @Override
        public void onCancel() {

        }
        @Override
        public void onComplete(Object response) {

        }
        @Override
        public void onError(UiError e) {

        }
    };

    public void onClickShare(View view) {
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, "要分享的标题");
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  "要分享的摘要");
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  "http://www.qq.com/news/1.html");
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, "http://imgcache.qq.com/qzone/space_item/pre/0/66768.gif");
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  getString(R.string.app_name));

        // QQ分享要在主线程做
        ThreadManager.getMainHandler().post(new Runnable() {

            @Override
            public void run() {
                if (null != mTencent) {
                    mTencent.shareToQQ(MainActivity.this, params, qqShareListener);
                }
            }
        });
    }

}