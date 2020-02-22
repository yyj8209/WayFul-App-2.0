package com.dji.ImportSDKDemo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


public class CheckPylonsDialog extends Dialog {
    /* 方法二：先实现功能，再考虑封装。*/
    public static final int PYLON_NUM = 8;
    private TextView[] pylon_status = new TextView[PYLON_NUM];
    private byte []status = new byte[PYLON_NUM];
    //在构造方法里预加载我们的样式，这样就不用每次创建都指定样式了
    public CheckPylonsDialog(Context context, int themeResId,byte []data) {
        super(context, themeResId);
        //加载布局并给布局的控件设置点击事件
        View contentView = getLayoutInflater().inflate(R.layout.pylons_check_widget, null);
        contentView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        super.setContentView(contentView);
        for (int i = 0;i<status.length;i++)
            status[i] = data[2*i+1];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 预先设置Dialog的一些属性
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);
        setCanceledOnTouchOutside(true);// 点击Dialog外部消失
//        setContentView(layoutResID);

        pylon_status[0] = (TextView)findViewById( R.id.fire_prepare1 );
        pylon_status[1] = (TextView)findViewById( R.id.fire_prepare2 );
        pylon_status[2] = (TextView)findViewById( R.id.fire_prepare3 );
        pylon_status[3] = (TextView)findViewById( R.id.fire_prepare4 );
        pylon_status[4] = (TextView)findViewById( R.id.fire_prepare5 );
        pylon_status[5] = (TextView)findViewById( R.id.fire_prepare6 );
        pylon_status[6] = (TextView)findViewById( R.id.fire_prepare7 );
        pylon_status[7] = (TextView)findViewById( R.id.fire_prepare8 );

        String bomb_status = "";
        for (int i = 0; i < status.length; i ++)    // 奇数位是挂架序号，第二位是状态。 考虑data状态位，status只截取了其状态位。
        {
            switch (status[i])    // 第二位是状态。与imagePath对应。
            {
                case 0X00:  bomb_status =  "无  弹  ";  break;  // bomb_empty
                case 0X01:  bomb_status =  "无  弹  ";  break;  // bomb_empty_open
//                case 0X10:  bomb_status =  "烟雾弹  正常";  break;  // bomb_smoking
//                case 0X11:  bomb_status =  "烟雾弹  就绪";  break;  // bomb_smoking_ready
//                case 0X20:  bomb_status =  "催泪弹  正常";  break;  // bomb_tear
//                case 0X21:  bomb_status =  "催泪弹  就绪";  break;  // bomb_tear_ready
//                case 0X30:  bomb_status =  "震爆弹  正常";  break;  // bomb_stun
//                case 0X31:  bomb_status =  "震爆弹  就绪";  break;  // bomb_stun_ready
                case 0X10:  bomb_status =  "烟雾弹  ";  break;  // bomb_smoking
                case 0X11:  bomb_status =  "烟雾弹  ";  break;  // bomb_smoking_ready
                case 0X20:  bomb_status =  "催泪弹  ";  break;  // bomb_tear
                case 0X21:  bomb_status =  "催泪弹  ";  break;  // bomb_tear_ready
                case 0X30:  bomb_status =  "震爆弹  ";  break;  // bomb_stun
                case 0X31:  bomb_status =  "震爆弹  ";  break;  // bomb_stun_ready
                default:
                    Toast.makeText( getContext(),
                            "返回机载状态错误！", Toast.LENGTH_SHORT ).show();
            }
            pylon_status[i].setText((i+1)+ "号："+bomb_status );
        }
    }

}
