package com.dji.ImportSDKDemo;

import com.dji.ImportSDKDemo.model.Order;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.PopupMenu;
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
import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** Main activity that displays three choices to user */
public class MainActivity extends Activity implements View.OnClickListener{
//    private static final String TAG = "MainActivity";
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private DJISDKManager.SDKManagerCallback registrationCallback = new DJISDKManager.SDKManagerCallback() {

        // 又遇到不注册的问题，断点设在这里没有反应，我检查了网络，重启了手机，改回compileSdkVersion，没有效果。重新同步dji-uxsdk包，没有效果。Debug，
        //
        //我又电脑里找到前几天编译的可以正常注册的app，奇怪的是，还不注册。
        // 我休息了十分钟，可以注册了。
        // 结论：是网络不稳定。2019.02.18
        @Override
        public void onRegister(DJIError error) {
            isRegistrationInProgress.set(false);
            if (error == DJISDKError.REGISTRATION_SUCCESS) {
                DJISDKManager.getInstance().startConnectionToProduct();
                Toast.makeText(getApplicationContext(), "正在注册...", Toast.LENGTH_SHORT).show();
                loginAccount();
                findViewById(R.id.complete_ui_widgets).setBackgroundColor( getColor( R.color.medium_green ) );

            } else {
                Toast.makeText(getApplicationContext(),
                               "注册失败，请检查网络。",
                               Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onProductChange(BaseProduct djiBaseProduct, BaseProduct djiBaseProduct1) {
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
        findViewById(R.id.complete_ui_widgets).setOnClickListener(this);

        checkAndRequestPermissions();
    }

    @Override
    protected void onDestroy() {
        // Prevent memory leak by releasing DJISDKManager's references to this activity
        if (DJISDKManager.getInstance() != null) {
            DJISDKManager.getInstance().destroy();
        }
        super.onDestroy();
    }

    private void loginAccount(){
        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Toast.makeText(getApplicationContext(), "注册成功！", Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onFailure(DJIError error) {
                        Toast.makeText(getApplicationContext(), "注册失败…", Toast.LENGTH_LONG).show();
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
            Toast.makeText(getApplicationContext(), "onRequestPermissionsResult 失败!", Toast.LENGTH_LONG).show();
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            Toast.makeText(getApplicationContext(), "设备未授权!", Toast.LENGTH_LONG).show();
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
            Toast.makeText(getApplicationContext(), "startSDKRegistration 失败!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();
        if (id == R.id.complete_ui_widgets) {
            Class nextActivityClass;
            nextActivityClass = CompleteWidgetActivity.class;
            Intent intent = new Intent(this, nextActivityClass);
            startActivity(intent);
        }
        else{
            return;
        }

    }

}
