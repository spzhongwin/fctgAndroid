package com.example.rubbishclassification;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class MyApplication extends Application {
    private static MyApplication instance;
    private static Context context;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor sharedPreferencesEditor;

    public static MyApplication getInstance() {
        return instance;
    }

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        context = getApplicationContext();

        sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
    }

    /**
     * 设置 token
     * */
    public static void setUserToken(String token){
        sharedPreferencesEditor.putString("token",token);
        sharedPreferencesEditor.commit();
    }

    /**
     * 获取 token
     * */
    public static String getUserToken(){
        return sharedPreferences.getString("token","");
    }

    /**
     * 清楚所有的sharedPreferences数据
     * */
    public static void clearUserData(){
        sharedPreferencesEditor.clear();
    }

}
