package com.dji.ImportSDKDemo.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.dji.ImportSDKDemo.R;

/**
 * Created by Administrator on 2019/2/13.
 */

public class Fragment2 extends Fragment {

    private SendMessageCommunitor sendMessage;
    Button btn1,btn2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate( R.layout.fragment2,container,false);
        btn1 = view.findViewById( R.id.btnTrim);
        btn2 = view.findViewById( R.id.btnFireAllMode);
        btn1.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage.sendMessage( btn1.getId() );
//                Toast.makeText( getActivity(),"点击微调按钮",Toast.LENGTH_SHORT ).show();
            }
        } );
        btn2.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage.sendMessage( btn2.getId() );
//                Toast.makeText( getActivity(),"点击第发射按钮",Toast.LENGTH_SHORT ).show();
            }
        } );
        return view;
    }

//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
////        Log.d(TAG, "onActivityCreated");
//        if(savedInstanceState != null) {
////            Log.d(TAG, "saved variable number: " + savedInstanceState.getInt("number"));
//
//        }
//        super.onActivityCreated(savedInstanceState);
//    }

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
