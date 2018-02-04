package com.fashiongo.app.views;

import android.util.Log;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by stevejobs on 18/2/3.
 */

public class Application extends android.app.Application {

    public static final String WX_APPID = "wxe495693dd346acad";

    private static Application sApp = null;

    private IWXAPI mWxApi = null;


    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;

        initWx();
    }

    public static void sendWx(BaseReq req){
        if (sApp != null) {
            boolean a = sApp.mWxApi.sendReq(req);
            Log.d("wx", ""+a);
        }
    }

    private void initWx() {
        mWxApi = WXAPIFactory.createWXAPI(this, WX_APPID, true);
        mWxApi.registerApp(WX_APPID);
    }
}
