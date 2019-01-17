package com.example.rubbishclassification.tools;

import android.os.Environment;
import android.text.TextUtils;

import com.example.rubbishclassification.MyApplication;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class OkHttp3Utils {
    //私有的静态成员变量，只声明不创建
    private static OkHttpClient okHttpClient = null;
    //私有的构造函数
    public OkHttp3Utils() {
    }
    //提供返回实例的静态方法
    public static OkHttpClient getInstance() {
        if (okHttpClient == null){
            //加上同步安全
            synchronized(OkHttp3Utils.class){
                if (okHttpClient == null) {
                    File sdCache = new File(Environment.getExternalStorageDirectory(), "cache");
                    int cacheSize = 10 * 1024 * 1024;
                    okHttpClient = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).cache(new Cache(sdCache.getAbsoluteFile(), cacheSize)).build();
                }
            }
        }
        return okHttpClient;
    }

    /**
     * get请求
     * 参数1 url
     * 参数2 回调Callback
     */
    public static void doGet(String url, Callback callback) {
        if(!AppTools.isNetworkAvailable(MyApplication.getContext())){
            AppTools.toastLong("请检查网络");
            return;
        }
        if(TextUtils.isEmpty(url)){
            return;
        }
        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();
        //创建Request
        Request request = new Request.Builder().url(url).build();
        //得到Call对象
        Call call = okHttpClient.newCall(request);
        //执行异步请求
        call.enqueue(callback);
    }

    /**
     * post请求
     * 参数1 url
     * 参数2 回调Callback
     */
    public static void doPost(String url, Map<String, String> params, Callback callback) {
        if(TextUtils.isEmpty(url)){
            return;
        }
        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();
        //3.x版本post请求换成FormBody 封装键值对参数
        FormBody.Builder builder = new FormBody.Builder();
        //遍历集合
        for (String key : params.keySet()) {
            builder.add(key, params.get(key));
        }
        //创建Request
        Request request = new Request.Builder().url(url).post(builder.build()).build();
        //得到Call对象
        Call call = okHttpClient.newCall(request);
        //执行异步请求
        call.enqueue(callback);
    }

    /**
     * Post请求发送JSON数据
     * 参数一：请求Url
     * 参数二：请求的JSON
     * 参数三：请求回调
     */
    public static void doPostJson(String url, String jsonParams, Callback callback) {
        if(TextUtils.isEmpty(url)){
            return;
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        Call call = getInstance().newCall(request);
        call.enqueue(callback);
    }

    /**
     * post请求上传文件
     * 参数1 url
     * 参数2 回调Callback
     */
    public static void uploadPic(String url, File file, String fileName,Callback callback) {
        if(TextUtils.isEmpty(url)){
            return;
        }
        if(!file.isFile()){
            return;
        }
        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();
        //创建RequestBody 封装file参数
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        //创建RequestBody 设置类型等
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("file", fileName, fileBody).build();
        //创建Request
        Request request = new Request.Builder().url(url).post(requestBody).build();
        //得到Call
        Call call = okHttpClient.newCall(request);
        //执行请求
        call.enqueue(callback);
    }

}
