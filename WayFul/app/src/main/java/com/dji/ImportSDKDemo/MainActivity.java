package com.dji.ImportSDKDemo;

import com.dji.ImportSDKDemo.model.Order;
import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.litepal.LitePal;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.log.DJILog;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/** Main activity that displays three choices to user */
public class MainActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "MainActivity";
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private Button btnUI;
    private Switch swchLanguage;
    private String strLanguage = "中文";
    private DJISDKManager.SDKManagerCallback registrationCallback = new DJISDKManager.SDKManagerCallback() {

        // 又遇到不注册的问题，断点设在这里没有反应，我检查了网络，重启了手机，改回compileSdkVersion，没有效果。重新同步dji-uxsdk包，没有效果。Debug，
        //
        //我又电脑里找到前几天编译的可以正常注册的app，奇怪的是，还不注册。
        // 我休息了十分钟，可以注册了。
        // 结论：是网络不稳定。2019.02.18
        // 结论：是大疆的服务器不稳定。2019.06.29
        @Override
        public void onRegister(DJIError error) {
            isRegistrationInProgress.set(false);
            if (error == DJISDKError.REGISTRATION_SUCCESS) {
                DJISDKManager.getInstance().startConnectionToProduct();
//                btnUI.setText("@string/registerring");
//                Toast.makeText(getApplicationContext(), "正在注册...", Toast.LENGTH_SHORT).show();
//                loginAccount();
                btnUI.setText(getResources().getString( R.string.start ));
                btnUI.setBackgroundColor( Color.parseColor("#60CC60"));
//                btnUI.setBackground(getDrawable( R.drawable.corner_green_btn) );

            } else {
                Toast.makeText(getApplicationContext(),"Register error, please check the internet",
                               Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onProductDisconnect() {
            Log.d("TAG", "onProductDisconnect");
//            notifyStatusChange();
        }
        @Override
        public void onProductConnect(BaseProduct baseProduct) {
            Log.d( "TAG", String.format( "onProductConnect newProduct:%s", baseProduct ) );
//            notifyStatusChange();
        }
        @Override
        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                      BaseComponent newComponent) {
            if (newComponent != null) {
                newComponent.setComponentListener(new BaseComponent.ComponentListener() {

                    @Override
                    public void onConnectivityChange(boolean isConnected) {
                        Log.d("TAG", "onComponentConnectivityChanged: " + isConnected);
//                        notifyStatusChange();
                    }
                });
            }

            Log.d("TAG",
                    String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                            componentKey,
                            oldComponent,
                            newComponent));

        }        @Override
        public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

        }
    };
    private static final String[] REQUIRED_PERMISSION_LIST = new String[] {
        Manifest.permission.VIBRATE,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE,
    };
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private List<String> missingPermission = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCollector.addActivity( this );
        btnUI = (Button) findViewById(R.id.complete_ui_widgets);
        btnUI.setOnClickListener(this);
        swchLanguage = (Switch)findViewById(R.id.switch_language);
        swchLanguage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) {
                    strLanguage = "中文";
//                    Log.e(TAG,"语言"+strLanguage);
                    LanguageUtil.updateLocale(MainActivity.this, LanguageUtil.LOCALE_CHINESE);
                    btnUI.setText( getResources().getString( R.string.start ) );
                }else {
                    strLanguage = "EN";
                    LanguageUtil.updateLocale(MainActivity.this, LanguageUtil.LOCALE_ENGLISH);
                    btnUI.setText( getResources().getString( R.string.start ) );
                }
            }
        });

        checkAndRequestPermissions();

        IntentFilter filter = new IntentFilter();
        filter.addAction( GetProductApplication.FLAG_CONNECTION_CHANGE );
        registerReceiver( mReceiver,filter );

    }
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshSDKRelativeUI();
        }
    };

    private void refreshSDKRelativeUI() {
        BaseProduct mProduct = GetProductApplication.getProductInstance();

        if (null != mProduct && mProduct.isConnected()) {
            Log.v(TAG, "refreshSDK: True");

            String str = mProduct instanceof Aircraft ? "DJIAircraft" : "DJIHandHeld";
            Toast.makeText(getApplicationContext(),"Status: " + str + " connected",
                    Toast.LENGTH_LONG).show();

        } else {
            Log.v(TAG, "refreshSDK: False");
        }
    }

    @Override
    protected void onDestroy() {
        // Prevent memory leak by releasing DJISDKManager's references to this activity
        if (DJISDKManager.getInstance() != null) {
            DJISDKManager.getInstance().destroy();
        }
        unregisterReceiver( mReceiver );
        ActivityCollector.removeActivity( this );
        super.onDestroy();
    }

    private void loginAccount(){
        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
//                        Toast.makeText(getApplicationContext(), "注册成功！", Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onFailure(DJIError error) {
//                        Toast.makeText(getApplicationContext(), "注册失败…", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();    // 这一步是执行了的
        } else {
            ActivityCompat.requestPermissions(this,
                                              missingPermission.toArray(new String[missingPermission.size()]),
                                              REQUEST_PERMISSION_CODE);
        }
    }

    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "onRequestPermissionsResult Error!", Toast.LENGTH_LONG).show();
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            Toast.makeText(getApplicationContext(), " Unwarrantted!", Toast.LENGTH_LONG).show();
        }
    }

    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {   // 这一步是正常执行
                    DJISDKManager.getInstance().registerApp(MainActivity.this, registrationCallback);
                }
            });
        }
        else
        {
            Toast.makeText(getApplicationContext(), "startSDKRegistration Error!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.complete_ui_widgets) {
            Class nextActivityClass;
            nextActivityClass = CompleteWidgetActivity.class;
            Intent intent = new Intent(this, nextActivityClass);
            intent.putExtra("app_language",strLanguage);
//            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity(intent);
        }
        else{
            return;
        }

    }

}
