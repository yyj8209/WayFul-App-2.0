package com.dji.ImportSDKDemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import android.widget.Toast;
/*
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
*/
import com.dji.mapkit.maps.DJIMap;
import com.dji.mapkit.models.DJILatLng;

import java.util.ArrayList;

import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.ux.widget.MapWidget;


/** Activity that shows all the UI elements together */
public class CompleteWidgetActivity extends Activity implements CompoundButton.OnCheckedChangeListener{

    public static final int PYLON_NUM = 8;

    private MapWidget mapWidget;
//    private AMap aMap;
    private ViewGroup parentView,vStatus;
    private View fpvWidget;
    private boolean isMapMini = true;
    private Button pylonsStatusButton, fireOptionsButton, fireButton;

    private int height;
    private int width;
    private int margin;
    private int deviceWidth;
    private int deviceHeight;

    // 一共是八个图标，对应八种状态。
    private CheckBox [] cb = new CheckBox[PYLON_NUM];   // 测试复选框显示图片。
    private boolean [] cbChecked = new boolean[PYLON_NUM];   // 判断是否选中。
    private  byte[] dataDown,dataUp;
    private int[] imagePath;
    private ArrayList<String> sTemp = new ArrayList<String>();  // 临时的可变长度字符数组，存储发射准备和发射的挂架名。
    private ArrayList<Integer> nTemp = new ArrayList<Integer>(); // 临时的可变长度整型数组，存储发射准备和发射的挂架号。
    private ImageButton resizeButton;
    private boolean isOriginalSize = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_default_widgets );

        height = DensityUtil.dip2px( this, 100 );
        width = DensityUtil.dip2px( this, 150 );
        margin = DensityUtil.dip2px( this, 12 );

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;

        mapWidget = (MapWidget) findViewById( R.id.map_widget );
        mapWidget.initAMap( new MapWidget.OnMapReadyListener() {
            @Override
            public void onMapReady(@NonNull DJIMap map) {
//                LatLng shenzhen = new LatLng(22.5362, 113.9454);
//                map.addMarker(new MarkerOptions().position(shenzhen).title("Marker in Shenzhen"));
//                map.moveCamera( CameraUpdateFactory.newLatLng(shenzhen));

                map.setOnMapClickListener( new DJIMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(DJILatLng latLng) {
                        onViewClick( mapWidget );// 可以将这个函数换成航迹规划的函数。2019.1.19
                    }
                } );
            }
        } );

        mapWidget.onCreate( savedInstanceState );

        parentView = (ViewGroup) findViewById( R.id.root_view );
        vStatus = (ViewGroup) findViewById( R.id.status );

        fpvWidget = findViewById( R.id.fpv_widget );
        fpvWidget.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewClick( fpvWidget );
            }
        } );
        // 在这里添加响应 按钮 的函数
        pylonsStatusButton = (Button)findViewById( R.id.custom_pylons_status);  // 检查载荷按钮
        fireOptionsButton = (Button)findViewById( R.id.fire_options );           // 发射准备按钮
        fireButton = (Button)findViewById( R.id.fire);                             // 发射按钮
        resizeButton = (ImageButton)findViewById( R.id.zoom );
        pylonsStatusButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewClick( pylonsStatusButton );
            }
        } );
        fireOptionsButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewClick( fireOptionsButton );
            }
        } );
        fireButton.setEnabled( false );    // 初始化时按钮无效，直到发射准备完毕后自动激活。
        fireButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewClick( fireButton );
            }
        } );
        resizeButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewClick( resizeButton );
            }
        } );

        Init();   // 为初始化刷新挂架状态的函数。
        UpdateImage(dataDown);    // 初始化和更新挂架状态的显示。2018.10.04
//        onDataFromOnboardToMSDK();   // 设置接收下行数据的函数。
    }

    private void Init(){
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
                R.drawable.selector_pylon8  //bomb_stun_ready,
        };
        // 弹的种类：无弹、烟雾弹、催泪弹、震爆弹，依次用0000，0001，0010，0011表示。
        // 弹的状态：盖关0000、盖开0001。
        dataDown = new byte[]{
                0X00,0X10,    // 第一字节表示1号挂架，第二字节无弹盖关
                0X01,0X11,    // 第一字节表示2号挂架，第二字节无弹盖开
                0X02,0X10,    // 第一字节表示3号挂架，第二字节烟雾弹盖关
                0X03,0X11,    // 第一字节表示4号挂架，第二字节烟雾弹盖开
                0X04,0X20,    // 第一字节表示5号挂架，第二字节催泪弹盖关
                0X05,0X21,    // 第一字节表示6号挂架，第二字节催泪弹盖开
                0X06,0X30,    // 第一字节表示7号挂架，第二字节震爆弹盖关
                0X07,0X31     // 第一字节表示8号挂架，第二字节震爆弹盖开
        };
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

    // 初始化和更新挂架状态的显示。2018.10.04
    public void UpdateImage(byte[] data) {
        int bomb_type = 0;
        for (int i = 0; i < data.length/2; i ++)    // 奇数位是挂架序号，第二位是状态。 考虑data状态位，
        {
            switch (data[2*i+1])    // 第二位是状态。与imagePath对应。
            {
                case 0X00:  bomb_type = 0;  break;  // bomb_empty
                case 0X01:  bomb_type = 1;  break;  // bomb_empty_open
                case 0X10:  bomb_type = 2;  break;  // bomb_smoking
                case 0X11:  bomb_type = 3;  break;  // bomb_smoking_ready
                case 0X20:  bomb_type = 4;  break;  // bomb_tear
                case 0X21:  bomb_type = 5;  break;  // bomb_tear_ready
                case 0X30:  bomb_type = 6;  break;  // bomb_stun
                case 0X31:  bomb_type = 7;  break;  // bomb_stun_ready
                default:
                    Toast.makeText( CompleteWidgetActivity.this,
                            "返回机载状态错误！", Toast.LENGTH_SHORT ).show();
            }
            cb[i].setButtonDrawable( imagePath[bomb_type] );
//            cb[i].setButtonDrawable(getResources().getDrawable(imagePath[bomb_type]) );
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.checkbox1:
                if (dataDown[1] ==0X00||dataDown[1] ==0X01)   // 奇数位表示挂架号，偶数位表示状态。
                {
                    showHintsDialog();
                    cb[0].setChecked( false );
                    isChecked = false;
                }
                cbChecked[0] = isChecked;
                break;
            case R.id.checkbox2:
                if (dataDown[3] ==0X00||dataDown[3] ==0X01)
                {
                    showHintsDialog();
                    cb[1].setChecked( false );
                    isChecked = false;
                }
                cbChecked[1] = isChecked;
                break;
            case R.id.checkbox3:
                if (dataDown[5] ==0X00||dataDown[5] ==0X01)
                {
                    showHintsDialog();
                    cb[2].setChecked( false );
                    isChecked = false;
                }
                cbChecked[2] = isChecked;
                break;
            case R.id.checkbox4:
                if (dataDown[7] ==0X00||dataDown[7] ==0X01)
                {
                    showHintsDialog();
                    cb[3].setChecked( false );
                    isChecked = false;
                }
                cbChecked[3] = isChecked;
                break;
            case R.id.checkbox5:
                if (dataDown[9] ==0X00||dataDown[9] ==0X01)
                {
                    showHintsDialog();
                    cb[4].setChecked( false );
                    isChecked = false;
                }
                cbChecked[4] = isChecked;
                break;
            case R.id.checkbox6:
                if (dataDown[11] ==0X00||dataDown[11] ==0X01)
                {
                    showHintsDialog();
                    cb[5].setChecked( false );
                    isChecked = false;
                }
                cbChecked[5] = isChecked;
                break;
            case R.id.checkbox7:
                if (dataDown[13] ==0X00||dataDown[13] ==0X01)
                {
                    showHintsDialog();
                    cb[6].setChecked( false );
                    isChecked = false;
                }
                cbChecked[6] = isChecked;
                break;
            case R.id.checkbox8:
                if (dataDown[15] ==0X00||dataDown[15] ==0X01)
                {
                    showHintsDialog();
                    cb[7].setChecked( false );
                    isChecked = false;
                }
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
        FlightController mFlightController = GetProductApplication.getFlightControllerInstance();
        if(mFlightController==null)
            Toast.makeText(CompleteWidgetActivity.this,
                    "无法连接到飞机，请确认安装正确并重启软件。",Toast.LENGTH_SHORT).show();
        else
            mFlightController.setOnboardSDKDeviceDataCallback(new FlightController.OnboardSDKDeviceDataCallback() {
                @Override
                public void onReceive(byte[] data) {
                    // 1、更新挂架状态；2、提示正在执行的操作。
    //                UpdateHints(dataDown,data);
                    System.arraycopy(data, 0, dataDown, 0, PYLON_NUM*2);  // 下行状态数据拷贝到dataDown。
                    UpdateImage(dataDown);  // 作为测试，这两行可以暂时不要。2018.11.14
                }
            });
    }
    private void onViewClick(View view) {
//        Class nextActivityClass = null;
        if (view == fpvWidget && !isMapMini) {
            resizeFPVWidget( RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, 0, 0 );
            ResizeAnimation mapViewAnimation = new ResizeAnimation( mapWidget, deviceWidth, deviceHeight, width, height, margin );
            mapWidget.startAnimation( mapViewAnimation );
            isMapMini = true;
        } else if (view == mapWidget && isMapMini) {
            resizeFPVWidget( width, height, margin, 2 );
            ResizeAnimation mapViewAnimation = new ResizeAnimation( mapWidget, width, height, deviceWidth, deviceHeight, 0 );
            mapWidget.startAnimation( mapViewAnimation );
            isMapMini = false;
        } else if(view == pylonsStatusButton){   // 进入到检查载荷\界面。
            OnMoreClick(pylonsStatusButton);
//            OnPylonsStatusClick();
//            nextActivityClass = PylonsWidgetActivity.class;
//            Intent intent = new Intent(this, nextActivityClass);
//            startActivity(intent);
        } else if(view == fireOptionsButton){   // 进入到\发射准备\界面。
            OnFirePrepareClick();
//            nextActivityClass = PylonsWidgetActivity.class;
//            Intent intent = new Intent(this, nextActivityClass);
//            startActivity(intent);
        } else if(view == fireButton){   // 进入到发射界面。
            OnFireClick();
//            nextActivityClass = PylonsWidgetActivity.class;
//            Intent intent = new Intent(this, nextActivityClass);
//            startActivity(intent);
        }
        else if(view == resizeButton ){   // 进入到缩放界面。
            OnResizeRange(vStatus);
//            nextActivityClass = PylonsWidgetActivity.class;
//            Intent intent = new Intent(this, nextActivityClass);
//            startActivity(intent);
        }
    }

    private void resizeFPVWidget(int width, int height, int margin, int fpvInsertPosition) {
        RelativeLayout.LayoutParams fpvParams = (RelativeLayout.LayoutParams) fpvWidget.getLayoutParams();
        fpvParams.height = height;
        fpvParams.width = width;
        fpvParams.rightMargin = margin;
        fpvParams.bottomMargin = margin;
        fpvWidget.setLayoutParams( fpvParams );

        parentView.removeView( fpvWidget );
        parentView.addView( fpvWidget, fpvInsertPosition );
    }

    // 检查载荷状态 2018.08.17
    private void OnMoreClick(View view){
//        PopupMenu popupMenu = new PopupMenu(CompleteWidgetActivity.this,view,R.style.CustomDialogStyle);
//        popupMenu.inflate(R.menu.file);
        Context wrapper = new ContextThemeWrapper(this,R.style.CustomDialogStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper,view);
        popupMenu.getMenuInflater().inflate(R.menu.file, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.down_active:
                        onDataFromOnboardToMSDK();     // 这个函数放在OnCreate中合适，调试阶段放在这里。
                        SystemClock.sleep(1000);  // 等待下行数据。
                        for(int i=0;i<dataUp.length/2;i++)
                            dataUp[2*i+1] = 0X05;
                        onDataFromMSDKToOSDK( dataUp );
                        showProgressDialog();
                        break;
                    case R.id.down_check:
                        OnPylonsStatusClick();
//                        Toast.makeText(CompleteWidgetActivity.this,"下行链路正常",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.marks_show:
//                        TableListActivity.actionStart(CompleteWidgetActivity.this);
                        Toast.makeText(CompleteWidgetActivity.this,"你选择了显示记录",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.marks_export:
                        Toast.makeText(CompleteWidgetActivity.this,"你选择了导出记录",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.exit:
                        onDestroy();
                        break;
                }
                return false;
            }
        });
    }

    // 检查载荷状态 2018.08.17
    // 已经删除了该按钮，但仍然作为函数保留下来。
    private void OnPylonsStatusClick(){
        CheckPylonsDialog dialog = new CheckPylonsDialog(CompleteWidgetActivity.this,
                R.style.CustomDialogStyle,dataDown);
        dialog.show();
//        onDataFromOnboardToMSDK();     // 这个函数放在OnCreate中合适，调试阶段放在这里。
    }

    // 发射选项，确定需要打开的弹。   2018.7.19
    private void OnFirePrepareClick(){
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
                // 1-无操作，2-发射准备，3-发射，4-取消发射，5-返回挂架状态，6-调整盖板
                SetdataUp(cbChecked,3);    // public void SetdataUp(boolean [] cbChecked,int flag)
                onDataFromMSDKToOSDK(dataUp);
//                fireButton.setBackgroundColor( getColor( R.color.red ) );
//                fireButton.setEnabled( true );    // 激活<发射>按钮。
            }
        });
        builder.setNeutralButton("调整盖板",new DialogInterface.OnClickListener(){
            @Override
            public void onClick (DialogInterface dialog, int which)
            {
                SetdataUp(cbChecked,6);    // public void SetdataUp(boolean [] cbChecked,int flag)
                onDataFromMSDKToOSDK(dataUp);
            }
        });
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
    String bomb_status = "";
    byte[] status = new byte[PYLON_NUM];
    for (int i = 0; i < status.length; i++)
        status[i] = data[2 * i + 1];   // 只取状态位

        for (int i = 0; i < status.length; i++)    // 奇数位是挂架序号，第二位是状态。 考虑data状态位，status只截取了其状态位。
        switch (status[k])    // 第二位是状态。。
        {
            case 0X00:                bomb_status = "无  弹  ";                break;  // bomb_empty
            case 0X01:                bomb_status = "无  弹  ";                break;  // bomb_empty_open
            case 0X10:                bomb_status = "烟雾弹  ";                break;  // bomb_smoking
            case 0X11:                bomb_status = "烟雾弹  ";                break;  // bomb_smoking_ready
            case 0X20:                bomb_status = "催泪弹  ";                break;  // bomb_tear
            case 0X21:                bomb_status = "催泪弹  ";                break;  // bomb_tear_ready
            case 0X30:                bomb_status = "震爆弹  ";                break;  // bomb_stun
            case 0X31:                bomb_status = "震爆弹  ";                break;  // bomb_stun_ready
            default:                  bomb_status = "";
        }

    return bomb_status;
}

// 此按钮功能已取消。2019.1.17
    private void OnFireClick()
    {
 /*       AlertDialog.Builder builder = new AlertDialog.Builder( CompleteWidgetActivity.this,R.style.CustomDialogStyle );
        builder.setIcon(R.drawable.alert_icon);
        builder.setTitle("即将执行发射操作，请确认！");
        // 只需要在界面上显示弹的状态就可以了。也就是对弹状态的监控。  2018.11.03
        final String[] hobbies = {"1号挂架", "2号挂架", "3号挂架", "4号挂架",
                "5号挂架", "6号挂架", "7号挂架", "8号挂架", };
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
                    Toast.makeText( CompleteWidgetActivity.this, nTemp.get(which)+1+" 号取消发射",
                            Toast.LENGTH_SHORT ).show();
                }
                cbChecked[nTemp.get(which)] = isChecked;
                cb[nTemp.get(which)].setChecked( isChecked);
            }
        });
        // 在这里重载确定按钮的函数。目前是按软件发送指令的思路在做。
//        完成工作：1、生成上传指令；2、上传指令；3、更新主界面中挂架的状态；4、关闭发射按钮。
        builder.setPositiveButton("确定",new DialogInterface.OnClickListener() {
            @Override
            public void onClick (DialogInterface dialog,int which)
            {
                SetdataUp(cbChecked,3);    // public void SetdataUp(boolean [] cbChecked,int flag)
                onDataFromMSDKToOSDK(dataUp);
                fireButton.setBackgroundColor( getColor( R.color.gray_light ) );
                fireButton.setEnabled( false );    // 关闭<发射>按钮。
                for(int i=0;i<cb.length;i++) {
                    cbChecked[i] = false;
                    cb[i].setChecked( false );  // 为每一个复选框设置一个监听函数。
                }

            }
        });
        builder.setNegativeButton("取消",new DialogInterface.OnClickListener(){
            @Override
            public void onClick (DialogInterface dialog, int which)
            { }
        });

//        DataFromMSDKToOSDK(dataUp);
        final AlertDialog dialog =builder.create();
        dialog.show();
        //此处设置位置窗体大小
        dialog.getWindow().setLayout(DensityUtil.dip2px(CompleteWidgetActivity.this,300),
                LinearLayout.LayoutParams.WRAP_CONTENT);
*/    }

    // 指令上传 2018.08.17
    private void onDataFromMSDKToOSDK(byte []data){
        FlightController mFlightController = GetProductApplication.getFlightControllerInstance();
        if(mFlightController.isOnboardSDKDeviceAvailable()){
            mFlightController.sendDataToOnboardSDKDevice(data, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if(djiError!=null) {
                        Toast.makeText(getApplicationContext(),djiError.toString(),Toast.LENGTH_SHORT).show();  //发送指令失败！
                    }else{
                        Toast.makeText(getApplicationContext(),"发送成功",Toast.LENGTH_SHORT).show();  //发送指令失败！
                    }
                }
            });
        }
    }
    // 根据选中的挂架和操作要求，设置上行指令。
    // 1-无操作，2-发射准备，3-发射，4-取消发射，5-返回挂架状态，6-调整盖板
    private void SetdataUp(boolean [] cbChecked,int flag)
    {
        for(int i=0;i<cbChecked.length;i++)
        {
            if(cbChecked[i]) {
                switch (flag) {
                    case 1:   // 无操作
                        dataUp[2 * i + 1] = 0X00;
                        break;
                    case 2:    // 发射准备
                        dataUp[2 * i + 1] = 0X01;
                        break;
                    case 3:    // 发射
                        dataUp[2 * i + 1] = 0X02;
                        break;
                    case 4:    // 取消发射
                        dataUp[2 * i + 1] = 0X03;
                        break;
                    case 5:    // 返回挂架状态
                        dataUp[2 * i + 1] = 0X04;
                        break;
                    case 6:    // 调整盖板
                        dataUp[2 * i + 1] = 0X06;
                        break;
                }
            }
            else
                dataUp[2 * i + 1] = 0X00;    // 其余挂架全部无操作。
        }
    }
    private void UpdateHints(byte []dataDown,byte []data){
//        无弹、烟雾弹、催泪弹、震爆弹，依次用0000，0001，0010，0011表示。
        // 弹的状态：盖关0000、盖开0001。
        ArrayList<String> sTemp = new ArrayList<String>();
        sTemp.add( "当前状态更改：\n\n" );
        int nLen = dataDown.length;
        for (int i=0;i<nLen;i++)
        {
            if((dataDown[i]-data[i])!=0)
            {
                switch(data[i]) {
                    case 0X00: sTemp.add( i+1+"号挂架--->无弹盖关\n" );break;
                    case 0X01: sTemp.add( i+1+"号挂架--->无弹盖开\n" );break;
                    case 0X10: sTemp.add( i+1+"号挂架--->烟雾弹盖关\n" );break;
                    case 0X11: sTemp.add( i+1+"号挂架--->烟雾弹盖开\n" );break;
                    case 0X20: sTemp.add( i+1+"号挂架--->催泪弹盖关\n" );break;
                    case 0X21: sTemp.add( i+1+"号挂架--->催泪弹盖开\n" );break;
                    case 0X30: sTemp.add( i+1+"号挂架--->震爆弹盖关\n" );break;
                    case 0X31: sTemp.add( i+1+"号挂架--->震爆弹盖开\n" );break;
                    default:break;
                }
            }
        }
        Toast.makeText( CompleteWidgetActivity.this, sTemp.toString(),
                Toast.LENGTH_LONG ).show();
        sTemp.clear();
    }

    private void showProgressDialog() {
        /* @setProgress 设置初始进度
         * @setProgressStyle 设置样式（水平进度条）
         * @setMax 设置进度最大值
         */
        final int MAX_PROGRESS = 100;
        final ProgressDialog progressDialog =
                new ProgressDialog(CompleteWidgetActivity.this);
        progressDialog.setProgress(0);
        progressDialog.setTitle("正在激活，请稍等……");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(MAX_PROGRESS);
        progressDialog.show();
        /* 模拟进度增加的过程
         * 新开一个线程，每个100ms，进度增加2
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                int progress= 0;
                while (progress < MAX_PROGRESS){
                    try {
                        Thread.sleep(100);
                        progress += 2;
                        progressDialog.setProgress(progress);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
                // 进度达到最大值后，窗口消失
                progressDialog.cancel();
            }
        }).start();
    }

    // On click event for button
    public void OnResizeRange(View v){

        ViewGroup.LayoutParams params = vStatus.getLayoutParams();
        if (isOriginalSize)
        {
            params.height = 2 * vStatus.getHeight();
            params.width = 2 * vStatus.getWidth();
            resizeButton.setBackground(getDrawable(R.drawable.zoom_in ));
        } else {
            params.height = vStatus.getHeight()/2;
            params.width = vStatus.getWidth()/2;
            resizeButton.setBackground(getDrawable(R.drawable.zoom_out ));
        }
        isOriginalSize = !isOriginalSize;
        vStatus.setLayoutParams(params);

    }

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
    }

    @Override
    protected void onPause() {
        mapWidget.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapWidget.onDestroy();
        // Prevent memory leak by releasing DJISDKManager's references to this activity
        if (DJISDKManager.getInstance() != null) {
            DJISDKManager.getInstance().destroy();
        }
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
