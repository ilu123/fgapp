package com.fashiongo.app.views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fashiongo.app.R;
import com.fashiongo.app.commons.ShareAdapter;
import com.fashiongo.app.commons.ThreadManager;
import com.fashiongo.app.commons.Utils;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.GridHolder;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.util.UUID;

import wendu.webviewjavascriptbridge.WVJBWebView;

/**
 * Created by stevejobs on 18/1/31.
 */

public class MainActivity extends BaseActivity {

    Tencent mTencent = null;
    String mAppid = "1106716594";
    WVJBWebView mWebView = null;

    private Object shareData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (mTencent == null) {
            mTencent = Tencent.createInstance(mAppid, this);
        }

        mWebView = (WVJBWebView) findViewById(R.id.webview);

        mWebView.registerHandler("clickShare", new WVJBWebView.WVJBHandler() {
            @Override
            public void handler(Object data, WVJBWebView.WVJBResponseCallback callback) {
                //callback.onResult("Response from testJavaCallback");
                shareData = data;
                onClickShare(null);
            }
        });

//        HashMap<String, String> data = new HashMap<String, String>() {{
//            put("greetingFromJava", "Hi there, JS!");
//        }};
//        mWebView.callHandler("callJavascriptHandler", new JSONObject(data), new WVJBWebView.WVJBResponseCallback() {
//            @Override
//            public void onResult(Object data) {
//                Log.d("testJavascriptHandler", data.toString());
//            }
//        });

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAllowContentAccess(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);

//        mWebView.setWebChromeClient(new WebChromeClient() {
//
//        });
//
//        mWebView.setWebViewClient(new WebViewClient() {
//
//            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//                //view.setVisibility(View.INVISIBLE);
//            }
//        });
        mWebView.loadUrl("http://123.207.136.167:8888/fgapp/login.html");

    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) mWebView.goBack();
        else
            super.onBackPressed();
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
        if (shareData == null || shareData.getClass() != JSONObject.class) return;

        JSONObject j = (JSONObject)shareData;
        final String title = j.optString("title", "");
        final String desc = j.optString("description", "");
        final String image = j.optString("imageUrl", "");
        final String target = j.optString("targetUrl", "");

        DialogPlus dialog = DialogPlus.newDialog(this).setContentHolder(new GridHolder(3))
                .setAdapter(new ShareAdapter(this, true))
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                        if (position == 0) {
                            // QQ
                            final Bundle params = new Bundle();
                            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
                            params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
                            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, desc);
                            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, target);
                            params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, image);
                            params.putString(QQShare.SHARE_TO_QQ_APP_NAME, getString(R.string.app_name));

                            // QQ分享要在主线程做
                            ThreadManager.getMainHandler().post(new Runnable() {

                                @Override
                                public void run() {
                                    if (null != mTencent) {
                                        mTencent.shareToQQ(MainActivity.this, params, qqShareListener);
                                    }
                                }
                            });
                        } else if (position == 1 || position == 2) {
                            // Wx
                            WXWebpageObject w = new WXWebpageObject();
                            w.webpageUrl = target;
                            WXMediaMessage m = new WXMediaMessage(w);
                            m.title = title;
                            m.description = desc;
                            Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                            m.thumbData = Utils.bmpToByteArray(b, false);
                            SendMessageToWX.Req req = new SendMessageToWX.Req();
                            req.transaction = String.valueOf(System.currentTimeMillis());
                            req.message = m;
                            req.scene = position == 2 ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
                            Application.sendWx(req);
                        }
                    }
                })
                .setExpanded(false)  // This will enable the expand feature, (similar to android L share dialog)
                .create();
        dialog.show();
    }

}