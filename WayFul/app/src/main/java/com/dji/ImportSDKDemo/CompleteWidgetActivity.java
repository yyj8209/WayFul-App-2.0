package com.dji.ImportSDKDemo;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
//import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import android.widget.TextView;
import android.widget.Toast;

//import com.amap.api.maps2d.AMap;
//import com.amap.api.maps2d.CameraUpdate;
//import com.amap.api.maps2d.CameraUpdateFactory;
//import com.amap.api.maps2d.MapView;
//import com.amap.api.maps2d.model.BitmapDescriptorFactory;
//import com.amap.api.maps2d.model.LatLng;
//import com.amap.api.maps2d.model.Marker;
//import com.amap.api.maps2d.model.MarkerOptions;

//import com.dji.ImportSDKDemo.Fragments.Fragment1;
//import com.dji.ImportSDKDemo.Fragments.Fragment2;
//import com.dji.ImportSDKDemo.Fragments.Fragment3;
//import com.dji.ImportSDKDemo.Fragments.Fragment4;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.gimbal.GimbalMode;
import dji.common.gimbal.MovementSettings;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.util.CommonCallbacks;
import dji.midware.data.model.P3.DataFlycUploadWayPointMissionMsg;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.ux.widget.MapWidget;

import static android.view.View.VISIBLE;
import static android.view.View.INVISIBLE;
import static com.dji.ImportSDKDemo.GetProductApplication.getProductInstance;
import static com.dji.ImportSDKDemo.GetProductApplication.getWaypointMissionOperator;

//import com.amap.api.maps2d.model.BitmapDescriptorFactory;
//import com.amap.api.maps2d.model.CameraPosition;
//import com.amap.api.maps2d.model.LatLng;
//import com.amap.api.maps2d.model.Marker;
//import com.amap.api.maps2d.model.MarkerOptions;
import com.dji.ImportSDKDemo.CircleMenuLayout;
import com.dji.ImportSDKDemo.CircleMenuLayout.OnMenuItemClickListener;
import com.dji.mapkit.core.camera.DJICameraUpdate;
import com.dji.mapkit.core.camera.DJICameraUpdateFactory;
import com.dji.mapkit.core.maps.DJIMap;
import com.dji.mapkit.core.models.DJIBitmapDescriptorFactory;
import com.dji.mapkit.core.models.DJILatLng;
import com.dji.mapkit.core.models.annotations.DJIMarker;
import com.dji.mapkit.core.models.annotations.DJIMarkerOptions;
import com.google.android.gms.common.util.Strings;

import dji.sdk.gimbal.Gimbal;

/** Activity that shows all the UI elements together */
public class CompleteWidgetActivity extends FragmentActivity  implements CompoundButton.OnCheckedChangeListener
//public class CompleteWidgetActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener
//                                                                            ,Fragment1.SendMessageCommunitor
//                                                                            ,Fragment2.SendMessageCommunitor
//                                                                            ,Fragment3.SendMessageCommunitor
//                                                                            ,Fragment4.SendMessageCommunitor
{

    protected static final String TAG_COM = "CompleteWidgetActivity";
    public static final int PYLON_NUM = 8;
    private String language = "";

    private MapWidget mapWidget;
    private DJIMap aMap;
    private ViewGroup parentView;
    private View fpvWidget;
    private boolean isMapMini = true;
    private Button btnExchangeView;
    private int height;
    private int width;
    private int margin;
    private int deviceWidth;
    private int deviceHeight;


    // 一共是八个图标，对应八种状态。
    private CheckBox [] cb = new CheckBox[PYLON_NUM];   // 测试复选框显示图片。
    private boolean [] cbChecked = new boolean[PYLON_NUM];   // 判断是否选中。
    private  byte[] dataDown,dataUp,dataDownOld;
    private boolean bSendOrder = true;
    private int[] imagePath;
    private ArrayList<String> sTemp = new ArrayList<String>();  // 临时的可变长度字符数组，存储发射准备和发射的挂架名。
    private ArrayList<Integer> nTemp = new ArrayList<Integer>(); // 临时的可变长度整型数组，存储发射准备和发射的挂架号。
    private FlightController mFlightController;
    private Gimbal mGimbal;

//    // 航线飞行部分变量
//    private MapWidget mapWidget;
//    private AMap aMap;
//    private double droneLocationLat = 181, droneLocationLng = 181;   // 飞机位置。
//    private DJIMarker droneMarker = null;
//    private boolean isAdd = false;
//    private final Map<Integer, DJIMarker> mMarkers = new ConcurrentHashMap<Integer, DJIMarker>();
//    private float altitude = 100.0f;
//    private float mSpeed = 10.0f;
//    private List<Waypoint> waypointList = new ArrayList<>();
//    public static WaypointMission.Builder waypointMissionBuilder;
//    private WaypointMissionOperator WMOinstance;
//    private WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
//    private WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.AUTO;

    // 导航栏部分
//    private TabLayout tabLayout = null;
//    private ViewPager vp_pager;
//    private AutofitViewPager vp_pager;
//    private List<Fragment> fragments;
//    private String[] titles;

    // 圆形菜单部分 2019.07.03
    private CircleMenuLayout mCircleMenuLayout;
//    private String[] mItemTexts = new String[] { "激活下行 ", "新增航点", "上传航点",
//            "清除航点", "航线飞行", "停止航飞", "更多设置" };
    private String[] mItemTexts = new String[] { };
    private int[] mItemImgs = new int[] { };
    private boolean bLightState = false, bBlinkState = false, bShoutState = false;
//            R.drawable.clear, R.drawable.start,
//            R.drawable.stop, R.drawable.settings };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_default_widgets );
        ActivityCollector.addActivity( this );

        Intent intent = getIntent();
        language = intent.getStringExtra("app_language");
        Log.e(TAG_COM,"语言"+language);
        if(language.contains("中文"))
            LanguageUtil.updateLocale(CompleteWidgetActivity.this, LanguageUtil.LOCALE_CHINESE);
        else
            LanguageUtil.updateLocale(CompleteWidgetActivity.this, LanguageUtil.LOCALE_ENGLISH);

        height = DensityUtil.dip2px( this, 100 );
        width = DensityUtil.dip2px( this, 150 );
        margin = DensityUtil.dip2px( this, 10 );
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;

        parentView = (ViewGroup) findViewById( R.id.root_view );
        fpvWidget = findViewById( R.id.fpv_widget );
        fpvWidget.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewClick( fpvWidget );
            }
        } );

        mapWidget = (MapWidget) findViewById( R.id.map_widget );
        mapWidget.initAMap(new MapWidget.OnMapReadyListener() {
            @Override
            public void onMapReady(@NonNull DJIMap map) {
                map.setOnMapClickListener(new DJIMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(DJILatLng latLng) {
                        onViewClick(mapWidget);
                    }
                });
            }
        });
//        initMap();
        mapWidget.onCreate( savedInstanceState );

        btnExchangeView = (Button)findViewById( R.id.btn_exchng );
        btnExchangeView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exchangeView(isMapMini);
            }
        } );

        initMainUI();   // 为初始化刷新挂架状态的函数。
        updateImage(dataDown);    // 初始化和更新挂架状态的显示。2018.10.04

        mCircleMenuLayout = (CircleMenuLayout) findViewById(R.id.id_menulayout);
        mItemTexts = new String[] { getResources().getString( R.string.status_refresh ),
                getResources().getString( R.string.aim_on), getResources().getString( R.string.pylon_off),
                getResources().getString( R.string.light_on), getResources().getString( R.string.blink_on),
                getResources().getString( R.string.shout_on), getResources().getString( R.string.reserved) };
        mItemImgs = new int[] { R.drawable.downchannel,
                R.drawable.add, R.drawable.upload,
                R.drawable.light_off, R.drawable.blink_off,
                R.drawable.shout_off, R.drawable.settings };
        updateCircleMenuUI(true );
        initCircleMenu( );

        mFlightController = GetProductApplication.getFlightControllerInstance();
//        initTabLayoutView();   // 导航栏初始化。
//        onDataFromOnboardToMSDK();   // 设置接收下行数据的函数。
//        addListener();    // 对添加航线飞行目标点的监视。    2019.2.9
    }
    // 初始化圆形菜单界面
    private void updateCircleMenuUI(boolean bAdd ){
        mCircleMenuLayout.setMenuItemIconsAndTexts(mItemImgs, mItemTexts,bAdd);

        Log.e(TAG_COM,"菜单调试：updateCircleMenuUI");
    }
    // 清除圆形菜单界面
    private void clearCircleMenuUI( ){
        int []mItemImg = new int[]{};
        String []mItemText = new String[]{};
        mCircleMenuLayout.setMenuItemIconsAndTexts(mItemImg, mItemText,false);
        Log.e(TAG_COM,"菜单调试：clearCircleMemuUI");
    }
    // 设置菜单的响应函数
    private void initCircleMenu( ){
        bSendOrder = true;
        Log.e(TAG_COM,"菜单调试：initCircleMenu 功能响应");
        mCircleMenuLayout.setOnMenuItemClickListener(new OnMenuItemClickListener()
        {
            @Override
            public void itemClick(View view, int pos)
            {
// 菜单顺序(pos+1)：1状态更新，2一键瞄准，3弹仓显/隐，4打开/关闭照明，5打开/关闭喊话，6保留
// ---------------------------- 指令协议 ----------------------------
//                0X00	无操作	0X04	喊话
//                0X01	照明	0X05	激活下行链路
//                0X02	发射	0X06	关闭照明
//                0X03	闪光	0X07	关闭喊话
                switch (pos+1) {
                    case 1:
                        onDataFromOnboardToMSDK();     // 这个函数放在OnCreate中合适，调试阶段放在这里。
                        for (int i = 0; i < dataUp.length / 2; i++)
                            dataUp[2 * i + 1] = 0X05;
                        showProgressDialog();
                        break;
                    case 2:
                        if(View.VISIBLE == findViewById( R.id.aim).getVisibility()){
                            findViewById( R.id.aim).setVisibility( View.INVISIBLE );
                            aimGimbal(false);   // 云台回到自由模式
                        }
                        else {
                            findViewById( R.id.aim ).setVisibility( View.VISIBLE );
                            aimGimbal(true);   // 云台锁定相机方向
                        }
                        bSendOrder = false;
                        break;
                    case 3:
                        if(View.VISIBLE == findViewById( R.id.status).getVisibility()) {
                            findViewById( R.id.status ).setVisibility( View.INVISIBLE );
                            mItemTexts[2] = getResources().getString(R.string.pylon_on) ;
                            Log.e(TAG_COM,"菜单调式：隐弹仓");
                        }
                        else {
                            findViewById( R.id.status ).setVisibility( View.VISIBLE );
                            mItemTexts[2] = getResources().getString(R.string.pylon_off);
                            Log.e(TAG_COM,"菜单调式：显弹仓");
                        }
                        bSendOrder = false;
                        break;
                    case 4:
                        if(!bLightState) {
                            for (int i = 0; i < dataUp.length / 2; i++)
                                dataUp[2 * i + 1] = 0X01;   // 照明
                            mItemTexts[3] = getResources().getString(R.string.light_off);
                            mItemImgs[3] = R.drawable.light_on;
                            bLightState = true;
                            Log.e(TAG_COM,"菜单调式：开照明");
                        }
                        else{
                            for (int i = 0; i < dataUp.length / 2; i++)
                                dataUp[2 * i + 1] = 0X06;   // 关闭照明
                            mItemTexts[3] = getResources().getString(R.string.light_on);
                            mItemImgs[3] = R.drawable.light_off;
                            bLightState = false;
                            Log.e(TAG_COM,"菜单调式：关照明");
                        }
                        break;
                    case 5:
                        if(!bBlinkState) {
                            for (int i = 0; i < dataUp.length / 2; i++)
                                dataUp[2 * i + 1] = 0X03;   // 闪光
                            mItemTexts[4] = getResources().getString(R.string.blink_on);
                            mItemImgs[4] = R.drawable.blink_off;
                            bBlinkState = true;
                            Log.e(TAG_COM,"菜单调式：开闪光");
                        }
                        else{
                            for (int i = 0; i < dataUp.length / 2; i++)
                                dataUp[2 * i + 1] = 0X03;   // 关闭闪光
                            mItemTexts[4] = getResources().getString(R.string.blink_off);
                            mItemImgs[4] = R.drawable.blink_on;
                            bBlinkState = false;
                            Log.e(TAG_COM,"菜单调式：关闪光");
                        }
                        break;
                    case 6:
                        if(!bShoutState) {
                            for (int i = 0; i < dataUp.length / 2; i++)
                                dataUp[2 * i + 1] = 0X04;   // 喊话
                            mItemTexts[5] = getResources().getString(R.string.shout_on);
                            mItemImgs[5] = R.drawable.shout_off;
                            bShoutState = true;
                            Log.e(TAG_COM,"菜单调式：开喊话");
                        }
                        else{
                            for (int i = 0; i < dataUp.length / 2; i++)
                                dataUp[2 * i + 1] = 0X07;   // 关闭喊话
                            mItemTexts[5] = getResources().getString(R.string.shout_off);
                            mItemImgs[5] = R.drawable.shout_on;
                            bShoutState = false;
                            Log.e(TAG_COM,"菜单调式：关喊话");
                        }
                        break;
                    case 7:
                        Toast.makeText( CompleteWidgetActivity.this, getResources().getString( R.string.reserved ),
                                Toast.LENGTH_SHORT ).show();
//                        // 1-无操作，2-照明，3-发射，4-闪光，5-喊话，6-发射，7-激光。
//                        for (int i = 0; i < dataUp.length / 2; i++)
//                            dataUp[2 * i + 1] = 0X06;   // 激光
                        break;
                    default:
                        break;
                }
//                if(bSendOrder) onDataFromMSDKToOSDK( dataUp );
                updateCircleMenuUI( false );
            }

            @Override
            public void itemCenterClick(View view)
            {
                onFireClick();
            }
        });
//        原文：https://blog.csdn.net/lmj623565791/article/details/43131133

    }
    private void initMainUI(){
        // 将挂架存储为数组形式，方便在循环中调用。  2018.10.17
        cb[0] = (CheckBox) findViewById( R.id.checkbox1 );
        cb[1] = (CheckBox)findViewById( R.id.checkbox2 );
        cb[2] = (CheckBox)findViewById( R.id.checkbox3 );
        cb[3] = (CheckBox)findViewById( R.id.checkbox4 );
        cb[4] = (CheckBox)findViewById( R.id.checkbox5 );
        cb[5] = (CheckBox)findViewById( R.id.checkbox6 );
        cb[6] = (CheckBox)findViewById( R.id.checkbox7 );
        cb[7] = (CheckBox)findViewById( R.id.checkbox8 );
        // 要对每个复选框设置监听，给cbChecked赋值。2018.10.30
        // 加一个是否选中的参数，便于在<发射准备>和<发射>操作时显示选中的挂架。
        for(int i=0;i<cb.length;i++) {
            cbChecked[i] = false;
            cb[i].setOnCheckedChangeListener(this);  // 为每一个复选框设置一个监听函数。
        }

        imagePath = new int [] {
                R.drawable.selector_pylon1,//bomb_empty,
                R.drawable.selector_pylon2,//bomb_empty_open,
                R.drawable.selector_pylon3,//bomb_smoking,
                R.drawable.selector_pylon4,//bomb_smoking_ready,
                R.drawable.selector_pylon5,//bomb_tear,
                R.drawable.selector_pylon6,//bomb_tear_ready,
                R.drawable.selector_pylon7,//bomb_stun,
                R.drawable.selector_pylon8,  //bomb_stun_ready,
                R.drawable.selector_pylon9,//bomb_stun,
                R.drawable.selector_pylon10  //bomb_stun_ready,
        };
        // 弹的种类：无弹、烟雾弹、催泪弹、震爆弹，依次用0000，0001，0010，0011表示。
        // 弹的状态：盖关0000、盖开0001。
        dataDown = new byte[]{
                0X00,0X00,    // 第一字节表示1号挂架，第二字节无弹盖关
                0X01,0X01,    // 第一字节表示2号挂架，第二字节无弹盖开
                0X02,0X10,    // 第一字节表示3号挂架，第二字节烟雾弹盖关
                0X03,0X11,    // 第一字节表示4号挂架，第二字节烟雾弹盖开
                0X04,0X20,    // 第一字节表示5号挂架，第二字节催泪弹盖关
                0X05,0X21,    // 第一字节表示6号挂架，第二字节催泪弹盖开
                0X06,0X30,    // 第一字节表示7号挂架，第二字节震爆弹盖关
                0X07,0X31     // 第一字节表示8号挂架，第二字节震爆弹盖开
        };
        dataDownOld = new byte[]{0X00,0X00,0X01,0X00,0X02,0X00,0X03,0X00,0X04,0X00,0X05,0X00,0X06,0X00,0X07,0X00};
        // 操作命令：无操作0X00、发射准备0X01、发射0X02、取消发射0X03、返回挂架状态0X04。。
        dataUp = new byte[]{
                0X00,0X00,    // 第一字节表示1号挂架，第二字节无操作
                0X01,0X01,    // 第一字节表示2号挂架，第二字节发射准备
                0X02,0X02,    // 第一字节表示3号挂架，第二字节发射
                0X03,0X03,    // 第一字节表示4号挂架，第二字节取消发射
                0X04,0X04,    // 第一字节表示5号挂架，第二字节返回挂架状态
                0X05,0X00,    // 第一字节表示6号挂架，第二字节无操作
                0X06,0X00,    // 第一字节表示7号挂架，第二字节无操作
                0X07,0X00     // 第一字节表示8号挂架，第二字节无操作
        };
    }

//    private void initMap(){
//
//        if (aMap == null) {
//            aMap = mapWidget.getMap();
//            aMap.setOnMapClickListener( new DJIMap.OnMapClickListener(){
//                @Override
//                public void onMapClick(DJILatLng latLng) {
////                    onViewClick( mapWidget );// 添加航迹规划的函数。2019.1.19
//                    if (isAdd) {
//                        markWaypoint( latLng );   // 可以添加目标点。   2019.02.08
//                        Waypoint mWaypoint = new Waypoint( latLng.latitude, latLng.longitude, altitude );
////                        Toast.makeText(CompleteWidgetActivity.this,
////                                "经度：" +String.valueOf(latLng.latitude)+
////                                        "，纬度：" +String.valueOf(latLng.longitude)+
////                                        "，高度：" +String.valueOf(altitude), Toast.LENGTH_SHORT).show();
//                        //Add Waypoints to Waypoint arraylist;
//                        if (waypointMissionBuilder != null) {
//                            waypointList.add( mWaypoint );
//                            waypointMissionBuilder.waypointList( waypointList ).waypointCount( waypointList.size() );
//                        } else {
//                            waypointMissionBuilder = new WaypointMission.Builder();
//                            waypointList.add( mWaypoint );
//                            waypointMissionBuilder.waypointList( waypointList ).waypointCount( waypointList.size() );
//                        }
//                        Log.d( TAG_COM,"地图目标数量：" +String.valueOf( waypointList.size() ) );
//                    }
////                    else{
////                        Toast.makeText(CompleteWidgetActivity.this,"请点击添加后添加目标点。",
////                                Toast.LENGTH_SHORT).show();
////                    }
//                }
//            } );// add the listener for click for amap object
//        }
//
////        LatLng hefei = new LatLng(31.83, 117.25);
////        aMap.addMarker(new MarkerOptions().position(hefei).title("Marker in HeFei"));
//        initFlightController();
////        updateDroneLocation();
////        aMap.moveCamera( CameraUpdateFactory.newLatLng(hefei));
//    }

//    private void initFlightController() {
//
//        BaseProduct product = GetProductApplication.getProductInstance();
//        if (product != null && product.isConnected()) {
//            if (product instanceof Aircraft) {
//                mFlightController = ((Aircraft) product).getFlightController();
//            }
//        }
//
//        if (mFlightController != null) {
//
//            mFlightController.setStateCallback(
//                    new FlightControllerState.Callback() {
//                        @Override
//                        public void onUpdate(FlightControllerState
//                                                     djiFlightControllerCurrentState) {
//                            droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
//                            droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
//                            updateDroneLocation();
//                        }
//                    });
//
//        }
//    }

    // 初始化和更新挂架状态的显示。2018.10.04
    public void updateImage(byte[] data) {
        int bomb_type = 0;
        int tmp = 0X00;
        for (int i = 0; i < data.length/2; i ++)    // 奇数位是挂架序号，第二位是状态。 考虑data状态位，
        {
            tmp = data[2*i+1];
            if(tmp == 0X00)  bomb_type = 0;
            else if(tmp >= 0X01 && tmp <= 0X07) bomb_type = 1;
            else if(tmp == 0X10)  bomb_type = 2;
            else if(tmp >= 0X11 && tmp <= 0X17) bomb_type = 3;
            else if(tmp == 0X20)  bomb_type = 4;
            else if(tmp >= 0X21 && tmp <= 0X27) bomb_type = 5;
            else if(tmp == 0X30)  bomb_type = 6;
            else if(tmp >= 0X31 && tmp <= 0X37) bomb_type = 7;
            else if(tmp == 0X40)  bomb_type = 8;
            else if(tmp >= 0X41 && tmp <= 0X47) bomb_type = 9;
            else
                  Toast.makeText( CompleteWidgetActivity.this,
                            "Pylons Status Error！", Toast.LENGTH_SHORT ).show();

//            switch (data[2*i+1])    // 第二位是状态。与imagePath对应。
//            {
//                case 0X00:  bomb_type = 0;  break;  // bomb_empty
//                case 0X01:  bomb_type = 1;  break;  // bomb_empty_open
//                case 0X10:  bomb_type = 2;  break;  // bomb_smoking
//                case 0X11:  bomb_type = 3;  break;  // bomb_smoking_ready
//                case 0X20:  bomb_type = 4;  break;  // bomb_tear
//                case 0X21:  bomb_type = 5;  break;  // bomb_tear_ready
//                case 0X30:  bomb_type = 6;  break;  // bomb_stun
//                case 0X31:  bomb_type = 7;  break;  // bomb_stun_ready
//                default:
//                    Toast.makeText( CompleteWidgetActivity.this,
//                            "返回机载状态错误！", Toast.LENGTH_SHORT ).show();
//            }
            cb[i].setButtonDrawable( imagePath[bomb_type] );
//            cb[i].setButtonDrawable(getResources().getDrawable(imagePath[bomb_type]) );
        }

    }
//    private void initTabLayoutView() {
//        tabLayout = (TabLayout) findViewById(R.id.tablayout);
//        vp_pager = (ViewPager)findViewById(R.id.tab_viewpager);
//        fragments = new ArrayList<>();
//        fragments.add(new Fragment1());
//        fragments.add(new Fragment2());
//        fragments.add(new Fragment3());
//        fragments.add(new Fragment4());
////        TabLayout.Tab tab1 = tabLayout.newTab().setText( "弹仓状态" ).setIcon( getDrawable( R.drawable.alert_icon ) );
//        TabLayout.Tab tab1 = tabLayout.newTab().setText( "弹仓状态" );
//        TabLayout.Tab tab2 = tabLayout.newTab().setText( "发射操作" );
//        TabLayout.Tab tab3 = tabLayout.newTab().setText( "航线飞行" );
//        TabLayout.Tab tab4 = tabLayout.newTab().setText( "更多功能" );
//        tabLayout.addTab( tab1 );
//        tabLayout.addTab( tab2 );
//        tabLayout.addTab( tab3 );
//        tabLayout.addTab( tab4 );
//
//        titles = new String[]{"弹仓状态","发射操作","航线飞行","更多功能"};
//        MyAdapter adapter = new MyAdapter(getSupportFragmentManager(),fragments,titles);
//        vp_pager.setAdapter(adapter);
//        tabLayout.setupWithViewPager(vp_pager);   //关联TabLayout和ViewPager
//        // 添加竖直分割线
//        LinearLayout linearLayout = (LinearLayout) tabLayout.getChildAt(0);
//        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
//        linearLayout.setDividerDrawable( ContextCompat.getDrawable(this,
//                R.drawable.divide_vitercle));
//    }
////
//    public class MyAdapter extends FragmentPagerAdapter {
//        private List<Fragment> fragments;
//        private String[] tabNames;//tab选项名字
//
//
//        public MyAdapter(FragmentManager fm,List<Fragment> fragments,String[] tabNames) {
//            super(fm);
//            this.fragments = fragments;
//            this.tabNames = tabNames;
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            return fragments.get(position);  //返回碎片集合的第几项
//        }
//
//        @Override
//        public int getCount() {
//            return fragments.size();    //返回碎片集合大小
//        }
//
//        @Override
//        public CharSequence getPageTitle(int position) {
//            return tabNames[position];    //返回标题的第几项
//        }
//    }

//    @Override
//    public void sendMessage(int btnId) {
//        //...写上你想执行的代码
//        switch (btnId) {
//            case R.id.btnDispStatus:
//                vStatus.setVisibility( View.VISIBLE );
////                Toast.makeText( getApplicationContext(), "显示弹仓状态", Toast.LENGTH_SHORT ).show();
//                break;
//            case R.id.btnHideStatus:
//                vStatus.setVisibility( View.GONE );
////                Toast.makeText( getApplicationContext(), "隐藏弹仓状态", Toast.LENGTH_SHORT ).show();
//                break;
//            case R.id.btnSimpleMode:
//                Toast.makeText( getApplicationContext(), "敬请关注……", Toast.LENGTH_SHORT ).show();
//                break;
//            case R.id.btnFireAllMode:
//                onFireClick();
////                Toast.makeText( getApplicationContext(), "发射", Toast.LENGTH_SHORT ).show();
//                break;
//            case R.id.btnTrim:
//                onFireClick();
////                Toast.makeText( getApplicationContext(), "微调", Toast.LENGTH_SHORT ).show();
//                break;
//            case R.id.btnAddMarks:
//                isAdd = true;
////                Toast.makeText( getApplicationContext(), "添加航点", Toast.LENGTH_SHORT ).show();
//                break;
//            case R.id.btnUpload:
//                isAdd = false;
//                uploadWayPointMission();
////                Toast.makeText( getApplicationContext(), "上传航点", Toast.LENGTH_SHORT ).show();
//                break;
//            case R.id.btnClearMarks:
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        aMap.clear();
//                    }
//                });
//                waypointList.clear();
//                waypointMissionBuilder.waypointList(waypointList);
//                updateDroneLocation();
//                Toast.makeText(CompleteWidgetActivity.this,"清除目标点成功",Toast.LENGTH_SHORT).show();
//                break;
//            case R.id.btnStart:
//                startWaypointMission();
////                Toast.makeText( getApplicationContext(), "开始航线飞行", Toast.LENGTH_SHORT ).show();
//                break;
//            case R.id.btnStop:
//                stopWaypointMission();
////                Toast.makeText( getApplicationContext(), "停止航线飞行", Toast.LENGTH_SHORT ).show();
//                break;
//            case R.id.btnSettings:
//                showSettingDialog();
////                Toast.makeText( getApplicationContext(), "航线飞行设置", Toast.LENGTH_SHORT ).show();
//                break;
//            case R.id.btnDownActive:
//                onDataFromOnboardToMSDK();     // 这个函数放在OnCreate中合适，调试阶段放在这里。
////                SystemClock.sleep(1000);  // 等待下行数据。
//                for(int i=0;i<dataUp.length/2;i++)
//                    dataUp[2*i+1] = 0X05;
//                onDataFromMSDKToOSDK( dataUp );
//                showProgressDialog();
////                Toast.makeText( getApplicationContext(), "激活下行链路", Toast.LENGTH_SHORT ).show();
//                break;
//            case R.id.btnDownCheck:
//                onPylonsStatusClick();
////                Toast.makeText( getApplicationContext(), "检查下行链路", Toast.LENGTH_SHORT ).show();
//                break;
//            case R.id.btnBrowseLog:
//                Toast.makeText( getApplicationContext(), "查看日志", Toast.LENGTH_SHORT ).show();
//                break;
//            case R.id.btnExportLog:
//                Toast.makeText( getApplicationContext(), "导出日志", Toast.LENGTH_SHORT ).show();
//                break;
//            default:
//                break;
//        }
//    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.checkbox1:
//                if (dataDown[1] ==0X00||dataDown[1] ==0X01)   // 奇数位表示挂架号，偶数位表示状态。
//                {
//                    showHintsDialog();
//                    cb[0].setChecked( false );
//                    isChecked = false;
//                }
                cbChecked[0] = isChecked;
                break;
            case R.id.checkbox2:
//                if (dataDown[3] ==0X00||dataDown[3] ==0X01)
//                {
//                    showHintsDialog();
//                    cb[1].setChecked( false );
//                    isChecked = false;
//                }
                cbChecked[1] = isChecked;
                break;
            case R.id.checkbox3:
//                if (dataDown[5] ==0X00||dataDown[5] ==0X01)
//                {
//                    showHintsDialog();
//                    cb[2].setChecked( false );
//                    isChecked = false;
//                }
                cbChecked[2] = isChecked;
                break;
            case R.id.checkbox4:
//                if (dataDown[7] ==0X00||dataDown[7] ==0X01)
//                {
//                    showHintsDialog();
//                    cb[3].setChecked( false );
//                    isChecked = false;
//                }
                cbChecked[3] = isChecked;
                break;
            case R.id.checkbox5:
//                if (dataDown[9] ==0X00||dataDown[9] ==0X01)
//                {
//                    showHintsDialog();
//                    cb[4].setChecked( false );
//                    isChecked = false;
//                }
                cbChecked[4] = isChecked;
                break;
            case R.id.checkbox6:
//                if (dataDown[11] ==0X00||dataDown[11] ==0X01)
//                {
//                    showHintsDialog();
//                    cb[5].setChecked( false );
//                    isChecked = false;
//                }
                cbChecked[5] = isChecked;
                break;
            case R.id.checkbox7:
//                if (dataDown[13] ==0X00||dataDown[13] ==0X01)
//                {
//                    showHintsDialog();
//                    cb[6].setChecked( false );
//                    isChecked = false;
//                }
                cbChecked[6] = isChecked;
                break;
            case R.id.checkbox8:
//                if (dataDown[15] ==0X00||dataDown[15] ==0X01)
//                {
//                    showHintsDialog();
//                    cb[7].setChecked( false );
//                    isChecked = false;
//                }
                cbChecked[7] = isChecked;
                break;
            default:
                break;
        }
    }
    // 提示挂架为空的对话框。
    private void showHintsDialog(){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(CompleteWidgetActivity.this);
        normalDialog.setIcon(R.drawable.alert_icon);
        normalDialog.setTitle("请取消选择");
        normalDialog.setMessage("你选择的挂架未装弹，请点击确定按钮取消选择！");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // 显示
        normalDialog.show();
    }
                // 接收部分。  是否需要的一个线程专门负责接收飞机状态？2018.10.04
//    在OnCreate中引用该是函数，是否会一直对接收数据进行监听？
    private void onDataFromOnboardToMSDK( ){
//        mFlightController = GetProductApplication.getFlightControllerInstance();
        if(mFlightController==null)
            Toast.makeText(CompleteWidgetActivity.this,
                    "Can not connect the drone. Please try again.",Toast.LENGTH_SHORT).show();
        else
            mFlightController.setOnboardSDKDeviceDataCallback(new FlightController.OnboardSDKDeviceDataCallback() {
                @Override
                public void onReceive(byte[] data) {
                    // 1、如果状态有变，更新挂架状态；2、发出提示音，并显示弹仓；3、当前状态为老状态。
                    //                UpdateHints(dataDown,data);
                    System.arraycopy(data, 0, dataDown, 0, PYLON_NUM*2);  // 下行状态数据拷贝到dataDown。
                    if(!dataCmp( dataDown,dataDownOld,PYLON_NUM*2 )) {   // 表示状态改变
                        updateImage( dataDown );  // 作为测试，这两行可以暂时不要。2018.11.14
                        playSound( );
                        findViewById( R.id.status).setVisibility( View.VISIBLE );
                        System.arraycopy( data,0,dataDownOld,0,PYLON_NUM*2 );
                    }
                }
            });

    }

    // 比较两个数组的异同，相同返回true，不同返回false。
    private boolean dataCmp(byte [] data1,byte [] data2,int length){
        boolean bValue = true;
        for(int k = 0;k < length; k++){
            if(data1[k] != data2[k]){
                bValue = false;
                break;
            }
        }
        return bValue;
    }

    // 播放提示音
    public void playSound( ) {
        Uri uri = RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION) ;
        Ringtone mRingtone = RingtoneManager.getRingtone(CompleteWidgetActivity.this,uri);
        mRingtone.play();

//        new Thread(new Runnable() {
//            // The wrapper thread is unnecessary, unless it blocks on the
//            // Clip finishing; see comments.
//            public void run() {
//                try {
//                    AssetFileDescriptor fileDescriptor = assetManager.openFd("CongratulationsAmazing.mp3");
//                    player.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getStartOffset());
//                    player.prepare();
//                    player.start();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    private void onViewClick(View view) {
//        if (view == fpvWidget && !isMapMini) {  //　视窗放大，地图缩小
//            resizeFPVWidget(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, 0, 0);
////            RelativeLayout.LayoutParams fpvParams = (RelativeLayout.LayoutParams) fpvWidget.getLayoutParams();
////            ResizeAnimation fpvWidgetAnimation = new ResizeAnimation(fpvWidget, width, height, deviceWidth, deviceHeight, margin);
////            fpvWidget.startAnimation(fpvWidgetAnimation);
//            ResizeAnimation mapViewAnimation = new ResizeAnimation(mapWidget, deviceWidth, deviceHeight, width, height, margin);
//            mapWidget.startAnimation(mapViewAnimation);
//            isMapMini = true;
//        } else if (view == mapWidget && isMapMini) {  // 地图放大，视窗缩小。
//            resizeFPVWidget(width, height, 0, 2);
////            RelativeLayout.LayoutParams fpvParams = (RelativeLayout.LayoutParams) fpvWidget.getLayoutParams();
////            ResizeAnimation fpvWidgetAnimation = new ResizeAnimation(fpvWidget, deviceWidth, deviceHeight, width, height, margin);
////            fpvWidget.startAnimation(fpvWidgetAnimation);
//            ResizeAnimation mapViewAnimation = new ResizeAnimation(mapWidget, width, height, deviceWidth, deviceHeight, 0);
//            mapWidget.startAnimation(mapViewAnimation);
//            isMapMini = false;
//            updateDroneLocation();   // 定位飞机 2019.07.20
////            onRouteFlightClick();
//        }
    }
    private void exchangeView(boolean isMapMiniLocal) {
        if (isMapMiniLocal) {  //　地图放大，视窗缩小。
            ResizeAnimation mapViewAnimation = new ResizeAnimation(mapWidget, width, height, deviceWidth, deviceHeight, 0);
            resizeFPVWidget(width, height, 0, 2);
//            RelativeLayout.LayoutParams fpvParams = (RelativeLayout.LayoutParams) fpvWidget.getLayoutParams();
//            ResizeAnimation fpvWidgetAnimation = new ResizeAnimation(fpvWidget, deviceWidth, deviceHeight, width, height, margin);
//            fpvWidget.startAnimation(fpvWidgetAnimation);
            mapWidget.startAnimation(mapViewAnimation);
            isMapMini = false;
//            Log.d( TAG_COM,"地图目标宽度：" +String.valueOf( deviceWidth ) );
//            Toast.makeText( CompleteWidgetActivity.this,"放大地图",Toast.LENGTH_LONG ).show();
//            updateDroneLocation();   // 定位飞机 2019.07.20
//            getDroneLocation(mFlightController);
//            onRouteFlightClick();
        } else {  // 视窗放大，地图缩小
            ResizeAnimation mapViewAnimation = new ResizeAnimation(mapWidget, deviceWidth, deviceHeight, width, height, margin);
            resizeFPVWidget(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, 0, 0);
//            RelativeLayout.LayoutParams fpvParams = (RelativeLayout.LayoutParams) fpvWidget.getLayoutParams();
//            ResizeAnimation fpvWidgetAnimation = new ResizeAnimation(fpvWidget, width, height, deviceWidth, deviceHeight, margin);
//            fpvWidget.startAnimation(fpvWidgetAnimation);
            mapWidget.startAnimation(mapViewAnimation);
            isMapMini = true;
            Toast.makeText( getApplicationContext(),"Zoom out",Toast.LENGTH_LONG ).show();
//            Log.d( TAG_COM,"地图目标宽度：" +String.valueOf( width ) );
        }
    }

    private void resizeFPVWidget(int width, int height, int margin, int fpvInsertPosition) {
        RelativeLayout.LayoutParams fpvParams = (RelativeLayout.LayoutParams) fpvWidget.getLayoutParams();
        fpvParams.height = height;
        fpvParams.width = width;
        fpvParams.rightMargin = margin;
        fpvParams.bottomMargin = margin;
//        fpvParams.alignWithParent = true;
        fpvWidget.setLayoutParams( fpvParams );

        parentView.removeView( fpvWidget );
        parentView.addView( fpvWidget, fpvInsertPosition );
//        Toast.makeText( getApplicationContext(),"resizeFPVWidget 已执行",Toast.LENGTH_SHORT).show();
    }

    // 检查载荷状态 2018.08.17
    // 已经删除了该按钮，但仍然作为函数保留下来。
    private void onPylonsStatusClick(){
        CheckPylonsDialog dialog = new CheckPylonsDialog(CompleteWidgetActivity.this,
                R.style.CustomDialogStyle,dataDown);
        dialog.show();
//        onDataFromOnboardToMSDK();     // 这个函数放在OnCreate中合适，调试阶段放在这里。
    }

    // 发射选项，确定需要打开的弹。   2018.7.19
    // 目前，此函数执行发射操作。   2019.01.27
    private void onFireClick(){
        final String[] hobbies = {"1号挂架："+ bombStatusDisp(dataDown,1), "2号挂架："+bombStatusDisp(dataDown,2),
                "3号挂架："+bombStatusDisp(dataDown,3), "4号挂架："+bombStatusDisp(dataDown,4),
                "5号挂架："+bombStatusDisp(dataDown,5), "6号挂架："+bombStatusDisp(dataDown,6),
                "7号挂架："+bombStatusDisp(dataDown,7), "8号挂架："+bombStatusDisp(dataDown,8) };
        nTemp.clear();
        sTemp.clear();
        for(int i=0;i<cbChecked.length;i++)
        {
            if(cbChecked[i])
            {
                nTemp.add( i);            // 把选中的架号存储下来
                sTemp.add(hobbies[i] );      // 挂架号字符。
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder( CompleteWidgetActivity.this,R.style.CustomDialogStyle );
        builder.setIcon(R.drawable.alert_icon);
        builder.setTitle("即将发射");
        //    设置一个多项选择下拉框
        String[] s1 = new String[sTemp.size()];   //
        builder.setMultiChoiceItems(sTemp.toArray(s1),null,new DialogInterface.OnMultiChoiceClickListener(){
            @Override
            public void onClick (DialogInterface dialog,int which, boolean isChecked)
            {
                if (isChecked) {  // 在这里需要让选中的挂架sTemp，对应到hobbies上，即在操作时，知道对哪个挂架操作。2018.10.30
                    Toast.makeText( CompleteWidgetActivity.this, nTemp.get(which)+1+" 号即将发射",
                            Toast.LENGTH_SHORT ).show();
                }
                else{  // 找到该选项的字符串“*号弹”的起始位置，删除该字符串。2018.07.19
                    Toast.makeText( CompleteWidgetActivity.this, nTemp.get(which)+1+" 号发射取消",
                            Toast.LENGTH_SHORT ).show();
                }
                cbChecked[nTemp.get(which)] = isChecked;
                cb[nTemp.get(which)].setChecked( isChecked);
            }
        });
        // 在这里重载确定按钮的函数。目前是按软件发送指令的思路在做。
//        完成工作：1、生成上传指令；2、上传指令；3、更新主界面中挂架的状态；4、激活发射按钮。
        builder.setPositiveButton("发射",new DialogInterface.OnClickListener() {
            @Override
            public void onClick (DialogInterface dialog,int which)
            {
                // 1-无操作，2-照明，3-发射，4-闪光，5-喊话，6-发射，7-激光。
                setDataUp(cbChecked,3);    // public void setDataUp(boolean [] cbChecked,int flag)
                onDataFromMSDKToOSDK(dataUp);
//                FireButton.setBackgroundColor( getColor( R.color.red ) );
//                FireButton.setEnabled( true );    // 激活<发射>按钮。
                for(int i=0;i<cbChecked.length;i++) {
                    cb[i].setChecked( false );
                }
            }
        });
//        builder.setNeutralButton("调整盖板",new DialogInterface.OnClickListener(){
//            @Override
//            public void onClick (DialogInterface dialog, int which)
//            {
//                setDataUp(cbChecked,6);    // public void setDataUp(boolean [] cbChecked,int flag)
//                onDataFromMSDKToOSDK(dataUp);
//                for(int i=0;i<cbChecked.length;i++) {
//                    cb[i].setChecked( false );
//                }
//            }
//        });
        builder.setNegativeButton("取消",new DialogInterface.OnClickListener(){
            @Override
            public void onClick (DialogInterface dialog, int which)
            { }
        });
        final AlertDialog dialog =builder.create();
        dialog.show();
        //此处设置位置窗体大小
        dialog.getWindow().setLayout(DensityUtil.dip2px(CompleteWidgetActivity.this,300),
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private String bombStatusDisp(byte []data,int k) {
//        int tmp = 0X00;
//        for (int i = 0; i < data.length/2; i ++)    // 奇数位是挂架序号，第二位是状态。 考虑data状态位，
//        {
//            tmp = data[2 * i + 1];
//            if (tmp == 0X00) bomb_type = 0;
//            else if (tmp >= 0X01 && tmp <= 0X07) bomb_type = 1;
//            else if (tmp == 0X10) bomb_type = 2;
//            else if (tmp >= 0X11 && tmp <= 0X17) bomb_type = 3;
//            else if (tmp == 0X20) bomb_type = 4;
//            else if (tmp >= 0X21 && tmp <= 0X27) bomb_type = 5;
//            else if (tmp == 0X30) bomb_type = 6;
//            else if (tmp >= 0X31 && tmp <= 0X37) bomb_type = 7;
//            else if (tmp == 0X40) bomb_type = 8;
//            else if (tmp >= 0X41 && tmp <= 0X47) bomb_type = 9;
//            else
//                Toast.makeText( CompleteWidgetActivity.this,
//                        "返回机载状态错误！", Toast.LENGTH_SHORT ).show();
//        }
        String bomb_status = "";
        byte[] status = new byte[PYLON_NUM];
        for (int i = 0; i < status.length; i++)
            status[i] = data[2 * i + 1];   // 只取状态位

//        for (int i = 0; i < status.length; i++)    // 奇数位是挂架序号，第二位是状态。 考虑data状态位，status只截取了其状态位。
//        switch (status[i]>>4)    // 第二位是状态。。
//        {
//            case 0X00:                bomb_status = "无  弹  ";                break;  // bomb_empty
//            case 0X01:                bomb_status = "烟雾弹  ";                break;  // bomb_smoking
//            case 0X02:                bomb_status = "催泪弹  ";                break;  // bomb_tear
//            case 0X03:                bomb_status = "震爆弹  ";                break;  // bomb_stun
//            case 0X04:                bomb_status = "闪光弹  ";                break;  // bomb_stun
//            default:                  bomb_status = "";
//        }
        switch (status[k-1])    // 第二位是状态。。
        {
            case 0X00:                bomb_status = "无  弹  ";                break;  // bomb_empty
            case 0X01:                bomb_status = "无  弹  ";                break;  // bomb_empty_open
            case 0X10:                bomb_status = "烟雾弹  ";                break;  // bomb_smoking
            case 0X11:                bomb_status = "烟雾弹  ";                break;  // bomb_smoking_ready
            case 0X20:                bomb_status = "催泪弹  ";                break;  // bomb_tear
            case 0X21:                bomb_status = "催泪弹  ";                break;  // bomb_tear_ready
            case 0X30:                bomb_status = "震爆弹  ";                break;  // bomb_stun
            case 0X31:                bomb_status = "震爆弹  ";                break;  // bomb_stun_ready
            case 0X40:                bomb_status = "闪光弹  ";                break;  // bomb_stun
            case 0X41:                bomb_status = "闪光弹  ";                break;  // bomb_stun_ready
            default:                  bomb_status = "";
        }

    return bomb_status;
}

    // 指令上传 2018.08.17
    private void onDataFromMSDKToOSDK(byte []data){
//        mFlightController = GetProductApplication.getFlightControllerInstance();
        if(mFlightController.isOnboardSDKDeviceAvailable()){
            mFlightController.sendDataToOnboardSDKDevice(data, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if(djiError!=null) {
                        Toast.makeText(getApplicationContext(),djiError.toString(),Toast.LENGTH_SHORT).show();  //发送指令失败！
                    }else{
                        Toast.makeText(getApplicationContext(),"Send sucessfully.",Toast.LENGTH_SHORT).show();  //发送指令失败！
                    }
                }
            });
        }
    }
    // 根据选中的挂架和操作要求，设置上行指令。
    // 1-无操作，2-照明，3-发射，4-闪光，5-喊话，6-发射，7-激光。
    private void setDataUp(boolean [] cbChecked,int flag)
    {
        for(int i=0;i<cbChecked.length;i++)
        {
            if(cbChecked[i]) {
                switch (flag) {
                    case 1:  dataUp[2 * i + 1] = 0X00;   break;
                    case 2:  dataUp[2 * i + 1] = 0X01;   break;
                    case 3:  dataUp[2 * i + 1] = 0X02;   break;
                    case 4:  dataUp[2 * i + 1] = 0X03;   break;
                    case 5:  dataUp[2 * i + 1] = 0X04;   break;
                    case 6:  dataUp[2 * i + 1] = 0X06;   break;
                    case 7:  dataUp[2 * i + 1] = 0X07;   break;
                }
            }
            else
                dataUp[2 * i + 1] = 0X00;    // 其余挂架全部无操作。
        }
    }
//    private void UpdateHints(byte []dataDown,byte []data){
////        无弹、烟雾弹、催泪弹、震爆弹，依次用0000，0001，0010，0011表示。
//        // 弹的状态：盖关0000、盖开0001。
//        ArrayList<String> sTemp = new ArrayList<String>();
//        sTemp.add( "当前状态更改：\n\n" );
//        int nLen = dataDown.length;
//        for (int i=0;i<nLen;i++)
//        {
//            if((dataDown[i]-data[i])!=0)
//            {
//                switch(data[i]) {
//                    case 0X00: sTemp.add( i+1+"号挂架--->无弹盖关\n" );break;
//                    case 0X01: sTemp.add( i+1+"号挂架--->无弹盖开\n" );break;
//                    case 0X10: sTemp.add( i+1+"号挂架--->烟雾弹盖关\n" );break;
//                    case 0X11: sTemp.add( i+1+"号挂架--->烟雾弹盖开\n" );break;
//                    case 0X20: sTemp.add( i+1+"号挂架--->催泪弹盖关\n" );break;
//                    case 0X21: sTemp.add( i+1+"号挂架--->催泪弹盖开\n" );break;
//                    case 0X30: sTemp.add( i+1+"号挂架--->震爆弹盖关\n" );break;
//                    case 0X31: sTemp.add( i+1+"号挂架--->震爆弹盖开\n" );break;
//                    default:break;
//                }
//            }
//        }
//        Toast.makeText( CompleteWidgetActivity.this, sTemp.toString(),
//                Toast.LENGTH_LONG ).show();
//        sTemp.clear();
//    }

    //用于OnboardSDK与MobileSDK通信时，激活下行通道。
    private void showProgressDialog() {
        /* @setProgress 设置初始进度
         * @setProgressStyle 设置样式（圆形进度条）
         */
        final ProgressDialog progressDialog =
                new ProgressDialog(CompleteWidgetActivity.this);
        progressDialog.setProgressStyle( ProgressDialog.STYLE_SPINNER );
        progressDialog.setCancelable( false );
        progressDialog.setIcon( R.drawable.rtk_icon );
        progressDialog.setTitle("正在激活下行链路。");
        progressDialog.setMessage( "正在激活，请勿操作……" );
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    progressDialog.cancel();
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void aimGimbal(boolean bPitch) {

        BaseProduct product = GetProductApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mGimbal = product.getGimbal();
//                mFlightController = ((Aircraft) product).getFlightController();
            }
        }

        if (mGimbal != null) {
            if (bPitch) {
                mGimbal.setMode( GimbalMode.FPV, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            Toast.makeText( getApplicationContext(), djiError.toString() + "Gimbal Model Error",
                                    Toast.LENGTH_SHORT ).show();  //发送指令失败！
                        }
                        Log.d( TAG_COM,"mGimbal.setMode FPV Correct  ");
                    }
                } );
                final Rotation rotation = new Rotation.Builder()
                        .mode( RotationMode.ABSOLUTE_ANGLE )
                        .pitch( -90 )
//                        .roll( 0 )
//                        .yaw( 0 )
                        .time( 1.5 )
                        .build();
                Log.d( TAG_COM,"pitch --> "+String.valueOf( rotation.getPitch() ));

                mGimbal.rotate( rotation, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            Toast.makeText( getApplicationContext(), djiError.toString()+" rotation Error" ,
                                    Toast.LENGTH_LONG ).show();  //发送指令失败！
                        }
                    }
                } );
            }
            else {
                mGimbal.setMode( GimbalMode.FREE, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            Toast.makeText( getApplicationContext(), djiError.toString() + "Gimbal FREE Error",
                                    Toast.LENGTH_SHORT ).show();  //发送指令失败！
                        }
                        Log.d( TAG_COM,"mGimbal.setMode FREE Correct  ");

                    }
                } );
            }
        }
    }

    /** 航线飞行部分*/
    // 航线飞行设置2019.1.26
    // 在展开地图时，获取飞机对象。2019.07.06
//    private void onRouteFlightClick(){
//        // 这三行在调试时不要。  2019.2.9
////        mFlightController = GetProductApplication.getFlightControllerInstance();
////        getDroneLocation(mFlightController);    // 初始化航线飞行。
////        cameraUpdate();
//
//    }
////    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
////        @Override
////        public void onReceive(Context context, Intent intent) {
////            onProductConnectionChange();
////        }
////    };
//
//    private void onProductConnectionChange(){
////        BaseProduct product = GetProductApplication.getProductInstance();
////        if (product != null && product.isConnected()) {
////            if (product instanceof Aircraft) {
////                mFlightController = ((Aircraft) product).getFlightController();
//////                mFlightController = GetProductApplication.getFlightControllerInstance();
////
////            }
////        }
////
////        if (mFlightController != null) {
////            mFlightController.setStateCallback(
////                    new FlightControllerState.Callback() {
////                        @Override
////                        public void onUpdate(FlightControllerState djiFlightControllerCurrentState) {
////                            droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
////                            droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
////                            updateDroneLocation();
////                        }
////                    });
////        }
//    }
////     获取飞机位置
//    private void getDroneLocation(FlightController mFlightController){
//        if (mFlightController != null) {
//            mFlightController.setStateCallback(
//                    new FlightControllerState.Callback() {
//                        @Override
//                        public void onUpdate(FlightControllerState djiFlightControllerCurrentState) {
//                            droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
//                            droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
//                            updateDroneLocation();
//
//                        }
//                    } );
//        }
//    }

//
//    private void updateDroneLocation(){
//        final DJILatLng pos = new DJILatLng(droneLocationLat, droneLocationLng);
//        //Create MarkerOptions object
//        final DJIMarkerOptions markerOptions = new DJIMarkerOptions();
//        markerOptions.position(pos);
//        markerOptions.icon( DJIBitmapDescriptorFactory.fromResource(R.drawable.ic_drone));
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (droneMarker != null) {
////                    droneMarker.remove();
//                }
//                if (checkGpsCoordination(droneLocationLat, droneLocationLng)) {
////                    aMap.addMarker(markerOptions);   // 尝试如此实现，但droneMarker 未用上。
////                     droneMarker.setPosition( pos );   // 这个函数不知道是否可行？  2019.2.8
////                   droneMarker = aMap.addMarker(markerOptions);   // 教程上是这么写的
//                }
//            }
//        });
//    }
//    private boolean checkGpsCoordination(double latitude, double longitude) {
//        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
//    }
//    private void cameraUpdate(){
//        DJILatLng pos = new DJILatLng(droneLocationLat, droneLocationLng);
//        float zoomlevel = (float) 18.0;
////        DJICameraUpdate cu = DJICameraUpdateFactory.newDJILatLngZoom(pos, zoomlevel);
////        aMap.moveCamera(cu);
//    }
//
//    private void markWaypoint(DJILatLng point){
//        //Create MarkerOptions object
//        DJIMarkerOptions markerOptions = new DJIMarkerOptions();
//        markerOptions.position(point);
//        markerOptions.icon( DJIBitmapDescriptorFactory.fromResource(R.drawable.add_marks));
////        DJIMarker marker = (DJIMarker)aMap.addMarker(markerOptions);
////        mMarkers.put(mMarkers.size(), marker);
//    }
//    // 航线飞行的参数设置。
//    private void showSettingDialog(){
//        LinearLayout wayPointSettings =
//                (LinearLayout)getLayoutInflater().inflate(R.layout.dialog_waypointsetting, null);
//        final TextView wpAltitude_TV = (TextView) wayPointSettings.findViewById(R.id.altitude);
//        RadioGroup speed_RG = (RadioGroup) wayPointSettings.findViewById(R.id.speed);
//        RadioGroup actionAfterFinished_RG = (RadioGroup) wayPointSettings.findViewById(R.id.actionAfterFinished);
//        RadioGroup heading_RG = (RadioGroup) wayPointSettings.findViewById(R.id.heading);
//
//        speed_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                if (checkedId == R.id.lowSpeed){
//                    mSpeed = 3.0f;
//                } else if (checkedId == R.id.MidSpeed){
//                    mSpeed = 5.0f;
//                } else if (checkedId == R.id.HighSpeed){
//                    mSpeed = 10.0f;    // 相当于时速36KM
//                }
//            }
//        });
//
//        actionAfterFinished_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                if (checkedId == R.id.finishNone){
//                    mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
//                } else if (checkedId == R.id.finishGoHome){
//                    mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
//                } else if (checkedId == R.id.finishAutoLanding){
//                    mFinishedAction = WaypointMissionFinishedAction.AUTO_LAND;
//                } else if (checkedId == R.id.finishToFirst){
//                    mFinishedAction = WaypointMissionFinishedAction.GO_FIRST_WAYPOINT;
//                }
//            }
//        });
//
//        heading_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                if (checkedId == R.id.headingNext) {
//                    mHeadingMode = WaypointMissionHeadingMode.AUTO;
//                } else if (checkedId == R.id.headingInitDirec) {
//                    mHeadingMode = WaypointMissionHeadingMode.USING_INITIAL_DIRECTION;
//                } else if (checkedId == R.id.headingRC) {
//                    mHeadingMode = WaypointMissionHeadingMode.CONTROL_BY_REMOTE_CONTROLLER;
//                } else if (checkedId == R.id.headingWP) {
//                    mHeadingMode = WaypointMissionHeadingMode.USING_WAYPOINT_HEADING;
//                }
//            }
//        });
//
//        new AlertDialog.Builder(this)
//                .setTitle("")
//                .setView(wayPointSettings)
//                .setPositiveButton("确定",new DialogInterface.OnClickListener(){
//                    public void onClick(DialogInterface dialog, int id) {
//                        String altitudeString = wpAltitude_TV.getText().toString();
//                        altitude = Integer.parseInt(nulltoIntegerDefault(altitudeString));
//                        Log.e(TAG_COM,"altitude "+altitude);
//                        Log.e(TAG_COM,"speed "+mSpeed);
//                        Log.e(TAG_COM, "mFinishedAction "+mFinishedAction);
//                        Log.e(TAG_COM, "mHeadingMode "+mHeadingMode);
//                        configWayPointMission();
//                    }
//                })
//                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                })
//                .create()
//                .show();
//
//    }
//    String nulltoIntegerDefault(String value){
//        if(!isIntValue(value)) value="0";
//        return value;
//    }
//
//    boolean isIntValue(String val)
//    {
//        try {
//            val=val.replace(" ","");
//            Integer.parseInt(val);
//        } catch (Exception e) {return false;}
//        return true;
//    }
//
//    public WaypointMissionOperator getWaypointMissionOperator() {
//        if (WMOinstance == null) {
//            WMOinstance = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
//        }
//        return WMOinstance;
//    }
//
//    private void configWayPointMission(){
//        if (waypointMissionBuilder == null){
//            waypointMissionBuilder = new WaypointMission.Builder().finishedAction(mFinishedAction)
//                    .headingMode(mHeadingMode)
//                    .autoFlightSpeed(mSpeed)
//                    .maxFlightSpeed(mSpeed)
//                    .flightPathMode( WaypointMissionFlightPathMode.NORMAL);
//        }else
//        {
//            waypointMissionBuilder.finishedAction(mFinishedAction)
//                    .headingMode(mHeadingMode)
//                    .autoFlightSpeed(mSpeed)
//                    .maxFlightSpeed(mSpeed)
//                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);
//        }
//
//        if (waypointMissionBuilder.getWaypointList().size() > 0){
//            for (int i=0; i< waypointMissionBuilder.getWaypointList().size(); i++){
//                waypointMissionBuilder.getWaypointList().get(i).altitude = altitude;
//            }
//
//            Toast.makeText(CompleteWidgetActivity.this,"航线高度设置成功",Toast.LENGTH_SHORT).show();
////            setResultToToast("Set Waypoint attitude successfully");
//        }
//
//        WMOinstance = getWaypointMissionOperator();
//        DJIError error = WMOinstance.loadMission(waypointMissionBuilder.build());
////        DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());
//        if (error == null) {
//            Toast.makeText(CompleteWidgetActivity.this,"航线设置成功",Toast.LENGTH_SHORT).show();
////            setResultToToast("loadWaypoint succeeded");
//        } else {
//            Toast.makeText(CompleteWidgetActivity.this,"航线设置失败",Toast.LENGTH_SHORT).show();
////            setResultToToast("loadWaypoint failed " + error.getDescription());
//        }
//
//    }

    //Add Listener for WaypointMissionOperator
//    private void addListener() {
//        if (getWaypointMissionOperator() != null) {
//            getWaypointMissionOperator().addListener(eventNotificationListener);
//        }
////        WaypointMissionOperator WMOinstance = getWaypointMissionOperator();
////        if (WMOinstance != null) {
////            WMOinstance.addListener(eventNotificationListener);
////        }
//    }
//    private void removeListener() {
//        if (getWaypointMissionOperator() != null) {
//            getWaypointMissionOperator().removeListener(eventNotificationListener);
//        }
////        WaypointMissionOperator WMOinstance = getWaypointMissionOperator();
////        if (WMOinstance != null) {
////            WMOinstance.removeListener(eventNotificationListener);
////        }
//    }
//    private WaypointMissionOperatorListener eventNotificationListener = new WaypointMissionOperatorListener() {
//        @Override
//        public void onDownloadUpdate(WaypointMissionDownloadEvent downloadEvent) {
//        }
//        @Override
//        public void onUploadUpdate(WaypointMissionUploadEvent uploadEvent) {
//        }
//        @Override
//        public void onExecutionUpdate(WaypointMissionExecutionEvent executionEvent) {
//        }
//        @Override
//        public void onExecutionStart() {
//        }
//        @Override
//        public void onExecutionFinish(@Nullable final DJIError error) {
//            Toast.makeText(CompleteWidgetActivity.this,"执行状态： " + (error == null ?
//                    "成功！" : error.getDescription()),Toast.LENGTH_LONG).show();
//        }
//    };
//    private void uploadWayPointMission() {
//        Log.d( TAG_COM,"执行 uploadWayPointMission" );
////        WaypointMissionOperator WMOinstance = getWaypointMissionOperator();
////        getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
//        WMOinstance.uploadMission(new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onResult(DJIError error) {
//                Log.d( TAG_COM,"执行 uploadMission -- > onResult" );
//                if (error == null) {
//                    Log.d( TAG_COM,"执行  if (error == null) " );
//                    Toast.makeText( getApplicationContext(), "任务上传成功！",
//                            Toast.LENGTH_SHORT ).show();
//                } else {
//                    Log.d( TAG_COM,"执行   if (error != null)" );
//                    Toast.makeText( getApplicationContext(), "任务上传失败："
//                                    + error.getDescription() + " retrying...",
//                            Toast.LENGTH_SHORT ).show();
//                    getWaypointMissionOperator().retryUploadMission(null);
//                }
//            }
//        });
//
//    }
//
//    private void startWaypointMission(){
////        WaypointMissionOperator WMOinstance = getWaypointMissionOperator();
//        WMOinstance.startMission(new CommonCallbacks.CompletionCallback() {
////        getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onResult(DJIError error) {
//                Toast.makeText(CompleteWidgetActivity.this,"航线飞行启动： " + (error == null ?
//                        "成功！" : error.getDescription()),Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void stopWaypointMission(){
////            WaypointMissionOperator WMOinstance = getWaypointMissionOperator();
////          getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback() {
//            WMOinstance.stopMission(new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onResult(DJIError error) {
//                Toast.makeText(CompleteWidgetActivity.this,"航线飞行停止： " + (error == null ?
//                        "成功！" : error.getDescription()),Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
    /* 航线飞行部分 END */


    @Override
    protected void onResume() {
        super.onResume();

        // Hide both the navigation bar and the status bar.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mapWidget.onResume();
//        initFlightController();
    }

    @Override
    protected void onPause() {
        mapWidget.onPause();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CompleteWidgetActivity.this);
        builder.setIcon(R.drawable.ic_drone);
        builder.setTitle("提示");
        builder.setMessage("确定退出吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {// 参考《第一行代码》中的随时退出程序部分。2019.04.14
                ActivityCollector.finishAll();
            }
        });
        //    设置一个NegativeButton
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
            }
        });
        //    显示出该对话框
        builder.show();
//        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mapWidget.onDestroy();
        // Prevent memory leak by releasing DJISDKManager's references to this activity
        if (DJISDKManager.getInstance() != null) {
            DJISDKManager.getInstance().destroy();
        }
//        removeListener();
        ActivityCollector.removeActivity( this );
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapWidget.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapWidget.onLowMemory();
    }

    private class ResizeAnimation extends Animation {

        private View mView;
        private int mToHeight;
        private int mFromHeight;

        private int mToWidth;
        private int mFromWidth;
        private int mMargin;

        private ResizeAnimation(View v, int fromWidth, int fromHeight, int toWidth, int toHeight, int margin) {
            mToHeight = toHeight;
            mToWidth = toWidth;
            mFromHeight = fromHeight;
            mFromWidth = fromWidth;
            mView = v;
            mMargin = margin;
            setDuration(300);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float height = (mToHeight - mFromHeight) * interpolatedTime + mFromHeight;
            float width = (mToWidth - mFromWidth) * interpolatedTime + mFromWidth;
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mView.getLayoutParams();
            p.height = (int) height;
            p.width = (int) width;
            p.rightMargin = mMargin;
            p.bottomMargin = mMargin;
            mView.requestLayout();
        }
    }

}
