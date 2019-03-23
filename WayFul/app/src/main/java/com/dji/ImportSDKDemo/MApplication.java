package com.dji.ImportSDKDemo;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

import org.litepal.LitePal;

public class MApplication extends Application {

    private GetProductApplication getProductApplication;// 为添加getProductApplication而增加的代码。2018.11.14
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);

        LitePal.initialize(this);   // 配置数据库。2018.11.27

        // 为添加getProductApplication而增加的代码。2018.11.14
        if (getProductApplication == null) {
            getProductApplication = new GetProductApplication();
            getProductApplication.setContext(this);
        }
}

    @Override
    public void onCreate() {
        super.onCreate();
        getProductApplication.onCreate();
    }

}
