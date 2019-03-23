package com.dji.ImportSDKDemo.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dji.ImportSDKDemo.R;


/**
 * Created by Administrator on 2019/2/13.
 */

public class Fragment4 extends Fragment {
    public static final String TAG_1 = "Fragment4";
    //...
    private SendMessageCommunitor sendMessage;
    Button btn1,btn2,btn3,btn4;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate( R.layout.fragment4,container,false);

        btn1 = view.findViewById( R.id.btn_downactive);
        btn2 = view.findViewById( R.id.btn_downcheck);
        btn3 = view.findViewById( R.id.btn_browselog);
        btn4 = view.findViewById( R.id.btn_exportlog);
        btn1.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage.sendMessage( btn1.getId() );
            }
        } );
        btn2.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage.sendMessage( btn2.getId() );
            }
        } );
        btn3.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage.sendMessage( btn3.getId() );
            }
        } );
        btn4.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage.sendMessage( btn4.getId() );
            }
        } );

        return view;
    }

//接口回调
    /**
     * 用于fragment传递事件给activity
     */
    public interface SendMessageCommunitor {
        /**从fragment发送消息
         * @param btnId 消息内容
         * */
        void sendMessage(int btnId);
    }

        /**
     * 绑定到activity
     * @param context
     */
    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
    public void onAttach(Context context) {
        super.onAttach(context);

        sendMessage = (SendMessageCommunitor) context;
    }

}
