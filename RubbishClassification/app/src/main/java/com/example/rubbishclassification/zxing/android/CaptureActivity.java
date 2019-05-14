package com.example.rubbishclassification.zxing.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rubbishclassification.R;
import com.example.rubbishclassification.activity.FenjianPageOneActivity;
import com.example.rubbishclassification.activity.FenjianPageTwoActivity;
import com.example.rubbishclassification.activity.LaJiDaiFaFangOneActivity;
import com.example.rubbishclassification.activity.LaJiDaiFaFangTwoActivity;
import com.example.rubbishclassification.activity.SelectVillageActivity;
import com.example.rubbishclassification.bean.UserBean;
import com.example.rubbishclassification.tools.AppTools;
import com.example.rubbishclassification.tools.AppURI;
import com.example.rubbishclassification.tools.OkHttp3Utils;
import com.example.rubbishclassification.zxing.bean.ZxingConfig;
import com.example.rubbishclassification.zxing.camera.CameraManager;
import com.example.rubbishclassification.zxing.common.Constant;
import com.example.rubbishclassification.zxing.decode.DecodeImgCallback;
import com.example.rubbishclassification.zxing.decode.DecodeImgThread;
import com.example.rubbishclassification.zxing.decode.ImageUtil;
import com.example.rubbishclassification.zxing.view.ViewfinderView;
import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * @author: yzq
 * @date: 2017/10/26 15:22
 * @declare :扫一扫
 */

public class CaptureActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public ZxingConfig config;
    private SurfaceView previewView;
    private ViewfinderView viewfinderView;
    //private AppCompatImageView flashLightIv;
    //private TextView flashLightTv;
    private ImageView backIv;
    //private LinearLayoutCompat flashLightLayout;
    //private LinearLayoutCompat albumLayout;
    //private LinearLayoutCompat bottomLayout;
    private boolean hasSurface;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private SurfaceHolder surfaceHolder;
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private String streetId = "";
    private String communityId = "";
    private String villageId = "";
    private String villageName = "";
    private String saomiaojieguo = "";
    //分拣进来，或者是物业人员分发袋子页面进来
    private String from = "";
    private String fangjianhao = "";
    private TextView title;
    /**
     * @param pm
     * @return 是否有闪光灯
     */
    public static boolean isSupportCameraLedFlash(PackageManager pm) {
        if (pm != null) {
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            if (features != null) {
                for (FeatureInfo f : features) {
                    if (f != null && PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 保持Activity处于唤醒状态
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.BLACK);
        }

        /*先获取配置信息*/
        try {
            config = (ZxingConfig) getIntent().getExtras().get(Constant.INTENT_ZXING_CONFIG);
        } catch (Exception e) {

            Log.i("config", e.toString());
        }

        if (config == null) {
            config = new ZxingConfig();
        }

        setContentView(R.layout.activity_capture);

        initView();
        hasSurface = false;

        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        beepManager.setPlayBeep(config.isPlayBeep());
        beepManager.setVibrate(config.isShake());
    }

    private void initView() {
        previewView = findViewById(R.id.preview_view);
        previewView.setOnClickListener(this);

        viewfinderView = findViewById(R.id.viewfinder_view);
        //给扫描框设置高度，
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) viewfinderView.getLayoutParams();
        WindowManager manager = (WindowManager) CaptureActivity.this.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        linearParams.height = (int) (display.getWidth() * 0.6);
        viewfinderView.setLayoutParams(linearParams);
        viewfinderView.setZxingConfig(config);

        backIv = findViewById(R.id.backIv);
        backIv.setOnClickListener(this);

        title = findViewById(R.id.capture_title_text);
        textView1 = findViewById(R.id.capture_text_user);
        textView2 = findViewById(R.id.capture_text_xiaoqu);
        textView3 = findViewById(R.id.capture_text_tiaoguo);

//        textView2.setOnClickListener(this);
        textView3.setOnClickListener(this);

        //设置当前操作用户
        textView1.setText("当前登录用户：" + UserBean.getUserBean().getName());
        //获取上个页面传递过来的from
        from = getIntent().getStringExtra("from");

        fangjianhao = getIntent().getStringExtra("fangjianhao");

        if (!TextUtils.isEmpty(from) && "1".equals(from)) {
            //textView2.setVisibility(View.VISIBLE);
            textView3.setVisibility(View.VISIBLE);
            title.setText("垃圾袋督导");
        } else {
            //textView2.setVisibility(View.INVISIBLE);
            textView3.setVisibility(View.INVISIBLE);
            title.setText("垃圾袋发放");
        }

        //flashLightIv = findViewById(R.id.flashLightIv);
        //flashLightTv = findViewById(R.id.flashLightTv);

        //flashLightLayout = findViewById(R.id.flashLightLayout);
        //flashLightLayout.setOnClickListener(this);
        //albumLayout = findViewById(R.id.albumLayout);
        //albumLayout.setOnClickListener(this);
        //bottomLayout = findViewById(R.id.bottomLayout);


        //switchVisibility(bottomLayout, config.isShowbottomLayout());
        //switchVisibility(flashLightLayout, config.isShowFlashLight());
        //switchVisibility(albumLayout, config.isShowAlbum());

        /*有闪光灯就显示手电筒按钮  否则不显示*/
//        if (isSupportCameraLedFlash(getPackageManager())) {
//            flashLightLayout.setVisibility(View.VISIBLE);
//        } else {
//            flashLightLayout.setVisibility(View.GONE);
//        }

    }

    /**
     * @param flashState 切换闪光灯图片
     */
    public void switchFlashImg(int flashState) {

//        if (flashState == Constant.FLASH_OPEN) {
//            flashLightIv.setImageResource(R.drawable.ic_open);
//            flashLightTv.setText("关闭手电筒");
//            flashLightTv.setTextColor(getResources().getColor(R.color.myColor));
//        } else {
//            flashLightIv.setImageResource(R.drawable.ic_close);
//            flashLightTv.setTextColor(getResources().getColor(R.color.white));
//            flashLightTv.setText("开启手电筒");
//        }
    }

    /**
     * @param rawResult 返回的扫描结果
     */
    public void handleDecode(Result rawResult) {

        inactivityTimer.onActivity();

        beepManager.playBeepSoundAndVibrate();

        saomiaojieguo = rawResult.getText();

        if (!TextUtils.isEmpty(from) && "1".equals(from)) {
            sendQueryVillage();
        } else {
            next();
        }

    }


    private void switchVisibility(View view, boolean b) {
        if (b) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        cameraManager = new CameraManager(getApplication(), config);

        viewfinderView.setCameraManager(cameraManager);
        handler = null;

        surfaceHolder = previewView.getHolder();
        if (hasSurface) {

            initCamera(surfaceHolder);
        } else {
            // 重置callback，等待surfaceCreated()来初始化camera
            surfaceHolder.addCallback(this);
        }

        beepManager.updatePrefs();
        inactivityTimer.onResume();

    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            // 打开Camera硬件设备
            cameraManager.openDriver(surfaceHolder);
            // 创建一个handler来打开预览，并抛出一个运行时异常
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("二维码/条码");
        builder.setMessage(getString(R.string.msg_camera_framework_bug));
        builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    @Override
    protected void onPause() {

        Log.i("CaptureActivity", "onPause");
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();

        if (!hasSurface) {

            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void onClick(View view) {

//        int id = view.getId();
//        if (id == R.id.flashLightLayout) {
//            /*切换闪光灯*/
//            cameraManager.switchFlashLight(handler);
//        } else if (id == R.id.albumLayout) {
//            /*打开相册*/
//            Intent intent = new Intent();
//            intent.setAction(Intent.ACTION_PICK);
//            intent.setType("image/*");
//            startActivityForResult(intent, Constant.REQUEST_IMAGE);
//        } else if (id == R.id.backIv) {
//            finish();
//        }
        switch (view.getId()) {
            case R.id.backIv:
                finish();
                break;
            case R.id.capture_text_xiaoqu:
                Intent intent = new Intent(CaptureActivity.this, SelectVillageActivity.class);
                intent.putExtra("from", "1");
                startActivityForResult(intent, 10001);
                break;
            case R.id.capture_text_tiaoguo:
                Intent intent2 = new Intent(CaptureActivity.this, FenjianPageTwoActivity.class);
                intent2.putExtra("streetId", streetId);
                intent2.putExtra("communityId", communityId);
                intent2.putExtra("villageId", villageId);
                intent2.putExtra("villageName", villageName);
                intent2.putExtra("qrCodeId", saomiaojieguo);
                intent2.putExtra("fangjianhao","");
                startActivityForResult(intent2, 10003);
                break;
            default:
                break;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                // 选择小区
                case 10001:
                    if (data != null) {
                        streetId = data.getStringExtra("streetId");
                        communityId = data.getStringExtra("communityId");
                        villageId = data.getStringExtra("villageId");
                        villageName = data.getStringExtra("villageName");
                        textView2.setText(villageName);
                    }
                    break;
                case Constant.REQUEST_IMAGE:
                    if (data != null) {
                        String path = ImageUtil.getImageAbsolutePath(this, data.getData());

                        new DecodeImgThread(path, new DecodeImgCallback() {
                            @Override
                            public void onImageDecodeSuccess(Result result) {
                                handleDecode(result);
                            }

                            @Override
                            public void onImageDecodeFailed() {
                                Toast.makeText(CaptureActivity.this, "请更换二维码试一试", Toast.LENGTH_SHORT).show();
                            }
                        }).run();
                    }
                    break;
                case 10003:
                    if (data != null) {
                        String result = data.getStringExtra("success");
                        if ("1".equals(result)) {
                            saomiaojieguo = "";
                        } else {
                            saomiaojieguo = "";
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 提交
     * */
    public void next(){
        //loading.show();
        //ToDo -设置数据
        String url = AppURI.setDomainUrl(AppURI.propertySendBags);
        url += "qrCodeId="+saomiaojieguo;
        try {
            url += "&roomNumberText="+URLEncoder.encode(fangjianhao,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        url += "&bagNumber="+"100";
        Log.e("---",url);
        OkHttp3Utils.doGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //loading.dismiss();
                        AppTools.toastLong("系统网络异常");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //对象封装
                final String result = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //loading.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            String code = jsonObject.getString("code");
                            if (code.equals("1")){
                                AppTools.toastLong("发放成功");
                                Intent intent = new Intent(CaptureActivity.this,LaJiDaiFaFangTwoActivity.class);
                                intent.putExtra("fangjianhao",fangjianhao);
                                startActivity(intent);
                                CaptureActivity.this.finish();
                            }else{
                                AppTools.toastLong(jsonObject.getString("msg"));
                                CaptureActivity.this.finish();
                            }
                        } catch (JSONException e) {
                            AppTools.toastLong("解析异常");
                            CaptureActivity.this.finish();
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    //查询相应的用户列表
    public void sendQueryVillage(){
        String url = AppURI.setDomainUrl(AppURI.sweepcCodeSorting);
        url += "qrCodeId="+saomiaojieguo;
        Log.e("---",url);
        OkHttp3Utils.doGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //loading.dismiss();
                        AppTools.toastLong("系统网络异常");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //对象封装
                final String result = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //loading.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            String code = jsonObject.getString("code");
                            if (code.equals("1")){
                                JSONObject data = jsonObject.getJSONObject("data");
                                Intent intent2 = new Intent(CaptureActivity.this, FenjianPageTwoActivity.class);
                                intent2.putExtra("streetId", data.getJSONObject("streetInfo").getString("id"));
                                intent2.putExtra("communityId", data.getJSONObject("communityInfo").getString("id"));
                                intent2.putExtra("villageId", data.getJSONObject("villageInfo").getString("id"));
                                intent2.putExtra("villageName", data.getJSONObject("villageInfo").getString("name"));
                                intent2.putExtra("qrCodeId", saomiaojieguo);
                                intent2.putExtra("fangjianhao",data.getString("roomNumberText"));
                                startActivityForResult(intent2, 10003);
                            }else{
                                AppTools.toastLong(jsonObject.getString("msg"));
                            }
                        } catch (JSONException e) {
                            AppTools.toastLong("解析异常");
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

}
