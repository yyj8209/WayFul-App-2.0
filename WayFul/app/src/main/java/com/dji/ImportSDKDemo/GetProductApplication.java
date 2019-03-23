package com.dji.ImportSDKDemo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.DJISDKManager;

public class GetProductApplication extends Application{
    public static final String FLAG_GET_PRODUCT = "get_product";

    private static BaseProduct mProduct;

    private Application instance;

    private static WaypointMissionOperator WNOinstance;

    public void setContext(Application application) {
        instance = application;
    }

    @Override
    public Context getApplicationContext() {
        return instance;
    }

    public GetProductApplication() {

    }

    /**
     * This function is used to get the instance of DJIBaseProduct.
     * If no product is connected, it returns null.
     */
    public static synchronized BaseProduct getProductInstance() {
        if (null == mProduct) {
            mProduct = DJISDKManager.getInstance().getProduct();
        }
        return mProduct;
    }

    public static synchronized FlightController getFlightControllerInstance() {

        if (getProductInstance() == null) return null;

        FlightController mFlightController = ((Aircraft) getProductInstance()).getFlightController();

        return mFlightController;
    }

    public static synchronized WaypointMissionOperator getWaypointMissionOperator() {
        if (WNOinstance == null) {
            WNOinstance = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
        }
        return WNOinstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

}
