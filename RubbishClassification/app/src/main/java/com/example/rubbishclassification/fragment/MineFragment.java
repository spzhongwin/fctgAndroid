package com.example.rubbishclassification.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rubbishclassification.R;
import com.example.rubbishclassification.activity.ScanActivity;
import com.example.rubbishclassification.activity.UploadImageActivity;
import com.example.rubbishclassification.tools.AppTools;
import com.example.rubbishclassification.tools.AppURI;
import com.example.rubbishclassification.tools.OkHttp3Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MineFragment extends Fragment {
    private TextView textView4;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100:
                    Toast.makeText(getActivity(), "get我错错了", Toast.LENGTH_SHORT).show();
                    textView4.setText("get成功成 功");
                    break;
                case 101:
                    Toast.makeText(getActivity(), "post我错错了", Toast.LENGTH_SHORT).show();
                    textView4.setText("post成功成功");
                    break;
                default:
                    break;
            }
            if (msg.what == 100) {

            }
        }
    };

    public MineFragment() {
    }

    public static MineFragment newInstance() {
        MineFragment mineFragment = new MineFragment();
        return mineFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mine, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TextView textView = getActivity().findViewById(R.id.mine);
        TextView textView2 = getActivity().findViewById(R.id.mine2);
        TextView textView3 = getActivity().findViewById(R.id.mine3);
        TextView textView5 = getActivity().findViewById(R.id.mine5);
        textView4 = getActivity().findViewById(R.id.mine4);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ScanActivity.class);
                startActivity(intent);
            }
        });

//        账号：分拣员123
//        密码：md5(md5(18888888888))
//
//        账号：物业人员123
//        密码：md5(md5(18888888888))
        String name = "";
        String password ="";
        try {
            name = URLEncoder.encode("分拣员123","UTF-8");
            password = AppTools.getMD5String(AppTools.getMD5String("18888888888"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final String login_url = AppURI.login+"?name="+name+"&password="+password;
        textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OkHttp3Utils.doGet(login_url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("la", "失败失败" + e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.e("la", "成功" + response.body().string());
                        mHandler.sendEmptyMessage(100);
                    }
                });
            }
        });
        final Map<String, String> map = new HashMap<String, String>();
        map.put("name", "xiaozhu");
        map.put("password", "aaaaaa");
        textView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OkHttp3Utils.doPost(AppURI.login, map, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("la", "失败失败" + e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.e("la", "成功" + response.toString());
                        mHandler.sendEmptyMessage(101);
                    }
                });
            }
        });
        textView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UploadImageActivity.class);
                startActivity(intent);
            }
        });
    }
}
