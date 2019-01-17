package com.example.rubbishclassification.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.example.rubbishclassification.MyApplication;
import com.example.rubbishclassification.R;
import com.example.rubbishclassification.bean.UserBean;
import com.example.rubbishclassification.tools.AppTools;
import com.example.rubbishclassification.tools.AppURI;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

public class LaunchActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_launch, false);
        if (TextUtils.isEmpty(MyApplication.getUserToken())) {
            //如果token不存在或为空，直接跳转到登录页面
            Integer time = 2000;    //设置等待时间，单位为毫秒
            Handler handler = new Handler();
            //当计时结束时，跳转至主界面
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(LaunchActivity.this, LoginActivity.class));
                    LaunchActivity.this.finish();
                }
            }, time);
        } else {
            //调用自动登录接口
            String url =  AppURI.autoSign + "?token=" + MyApplication.getUserToken();
            DOGET(url,1);
        }

    }

    @Override
    public void requestSuccess(int urlId, String result) {
        super.requestSuccess(urlId, result);
        switch (urlId) {
            case 1: {
                Log.e("---", result);
                UserBean userBean = null;
                try{
                    JSONObject jsonObject = new JSONObject(result);
                    String code = jsonObject.getString("code");
                    if("1".equals(code)){
                        String data = jsonObject.getString("data");
                        userBean = new GsonBuilder().serializeNulls().create().fromJson(data,UserBean.class);
                        if(userBean.getRole().equals("2")){
                            String villageInfoName = jsonObject.getJSONObject("data").getJSONObject("region").getJSONObject("villageInfo").getString("name");
                            userBean.setVillageInfoName(villageInfoName);
                        }
                        UserBean.setUserBean(userBean);
                        MyApplication.setUserToken(userBean.getToken());

                        final Intent intent = new Intent(LaunchActivity.this, MyMainActivity.class);
                        Integer time = 2000;    //设置等待时间，单位为毫秒
                        Handler handler = new Handler();
                        //当计时结束时，跳转至主界面
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(intent);
                                LaunchActivity.this.finish();
                            }
                        }, time);
                    } else {
                        String msg = jsonObject.getString("msg");
                        requestFailure(1,msg);
                    }

                }catch (Exception e){
                    Log.e("-----","解析异常");
                    requestFailure(1,"数据解析异常");
                }
                break;
            }
            default:
                break;
        }

    }

    @Override
    public void requestFailure(int urlId, String result) {
        super.requestFailure(urlId, result);
        switch (urlId) {
            case 1: {
                Log.e("---", result);
                //清空用户数据，token，跳转到登录页面
                AppTools.toastShort(result);
                MyApplication.setUserToken("");
                Integer time = 2000;    //设置等待时间，单位为毫秒
                Handler handler = new Handler();
                //当计时结束时，跳转至主界面
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(LaunchActivity.this, LoginActivity.class));
                        LaunchActivity.this.finish();
                    }
                }, time);
                break;
            }
            default:
                break;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
