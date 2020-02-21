package com.dji.ImportSDKDemo;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.support.annotation.RequiresApi;

import com.secneo.sdk.Helper;

import org.litepal.LitePal;

import java.util.Locale;

public class MApplication extends Application {

    private GetProductApplication getProductApplication;// 为添加getProductApplication而增加的代码。2018.11.14
    @Override
    protected void attachBaseContext(Context paramContext) {
        Context context = languageWork(paramContext); // 为多国语言添加的。   2020.02.21
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);

//        LitePal.initialize(this);   // 配置数据库。2018.11.27

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

    private Context languageWork(Context context) {
        // 8.0及以上使用createConfigurationContext设置configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return updateResources(context);
        } else {
            return context;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Context updateResources(Context context) {
        Resources resources = context.getResources();
        Locale locale = LanguageUtil.getLocale(context);
        if (locale==null) {
            return context;
        }
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLocales(new LocaleList(locale));
        return context.createConfigurationContext(configuration);
    }
}
