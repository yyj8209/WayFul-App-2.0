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

public class Fragment2 extends Fragment {

    private SendMessageCommunitor sendMessage;
    Button btn3,btn4;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate( R.layout.fragment2,container,false);
        btn3 = view.findViewById( R.id.btn_fire);
        btn4 = view.findViewById( R.id.btn_trim);
        btn3.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage.sendMessage( btn3.getId() );
//                Toast.makeText( getActivity(),"点击第一个按钮",Toast.LENGTH_SHORT ).show();
            }
        } );
        btn4.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage.sendMessage( btn4.getId() );
//                Toast.makeText( getActivity(),"点击第一个按钮",Toast.LENGTH_SHORT ).show();
            }
        } );
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
//        Log.d(TAG, "onActivityCreated");
        if(savedInstanceState != null) {
//            Log.d(TAG, "saved variable number: " + savedInstanceState.getInt("number"));

        }
        super.onActivityCreated(savedInstanceState);
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
