package com.example.rubbishclassification.tools;

import android.text.format.DateFormat;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.example.rubbishclassification.MyApplication;

import java.io.File;
import java.util.Date;

public class UploadHelper {

    //与个人的存储区域有关
    private static final String ENDPOINT = "oss-cn-shenzhen.aliyuncs.com";
    //上传仓库名
    private static final String BUCKET_NAME = "testftcg";

    private static OSS getOSSClient() {
        OSSCredentialProvider credentialProvider =
                new OSSPlainTextAKSKCredentialProvider("LTAI7aEp1YBWVrg7" ,
                        "tyYZAt7birPCQ6igyyg7RBT59ePnhu");
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(8); // 最大并发请求数，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次

        return new OSSClient( MyApplication.getContext(), ENDPOINT, credentialProvider,conf);
    }

    /**
     * 上传方法
     *
     * @param objectKey 标识
     * @param path      需上传文件的路径
     * @return 外网访问的路径
     */
    private static String upload(String objectKey, String path) {
        // 构造上传请求
        PutObjectRequest request =
                new PutObjectRequest(BUCKET_NAME,
                        objectKey, path);
        try {
            //得到client
            OSS client = getOSSClient();
            //上传获取结果
            PutObjectResult result = client.putObject(request);
            //获取可访问的url
            String url = client.presignPublicObjectURL(BUCKET_NAME, objectKey);
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 上传方法
     *
     * @param objectKey 标识
     * @param bytes      需上传文件byte[]
     * @return 外网访问的路径
     */
    private static String upload(String objectKey, byte[] bytes) {
        // 构造上传请求
        PutObjectRequest request =
                new PutObjectRequest(BUCKET_NAME,
                        objectKey, bytes);
        try {
            //得到client
            OSS client = getOSSClient();
            //上传获取结果
            PutObjectResult result = client.putObject(request);
            //获取可访问的url
            String url = client.presignPublicObjectURL(BUCKET_NAME, objectKey);
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 异步上传方法
     *
     * @param objectKey 标识
     * @param
     * @return 外网访问的路径
     */
    public static String uploadAsync(String objectKey, String path,OSSCompletedCallback<PutObjectRequest, PutObjectResult>  completedCallback) {
        // 构造上传请求
        PutObjectRequest request =
                new PutObjectRequest(BUCKET_NAME,
                        objectKey, path);
        try {
            //得到client
            OSS client = getOSSClient();
            //上传获取结果
            client.asyncPutObject(request,completedCallback);
            //获取可访问的url
            String url = client.presignPublicObjectURL(BUCKET_NAME, objectKey);
            Log.e("上传图片返回到url",url);
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    /**
     * 上传普通图片
     *
     * @param path 本地地址
     * @return 服务器地址
     */
    public static String uploadImage(String path) {
        String key = getObjectImageKey(path);
        return upload(key, path);
    }

    /**
     * 上传头像
     *
     * @param path 本地地址
     * @return 服务器地址
     */
    public static String uploadPortrait(String path) {
        String key = getObjectPortraitKey(path);
        return upload(key, path);
    }

    /**
     * 上传audio
     *
     * @param path 本地地址
     * @return 服务器地址
     */
    public static String uploadAudio(String path) {
        String key = getObjectAudioKey(path);
        return upload(key, path);
    }


    /**
     * 获取时间
     *
     * @return 时间戳 例如:201805
     */
    private static String getDateString() {
        return DateFormat.format("yyyyMM", new Date()).toString();
    }

    /**
     * 返回key
     *
     * @param path 本地路径
     * @return key
     */
    //格式: image/201805/sfdsgfsdvsdfdsfs.jpg
    public static String getObjectImageKey(String path) {
        String fileMd5 = AppTools.getMD5String(new File(path));
        return String.format("%s.jpg", fileMd5);
    }

    //格式: portrait/201805/sfdsgfsdvsdfdsfs.jpg
    private static String getObjectPortraitKey(String path) {
        String fileMd5 = AppTools.getMD5String(new File(path));
        String dateString = getDateString();
        return String.format("portrait/%s/%s.jpg", dateString, fileMd5);
    }

    //格式: audio/201805/sfdsgfsdvsdfdsfs.mp3
    private static String getObjectAudioKey(String path) {
        String fileMd5 = AppTools.getMD5String(new File(path));
        String dateString = getDateString();
        return String.format("audio/%s/%s.mp3", dateString, fileMd5);
    }

}