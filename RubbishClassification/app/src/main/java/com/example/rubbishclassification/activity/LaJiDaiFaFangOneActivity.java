package com.example.rubbishclassification.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rubbishclassification.R;
import com.example.rubbishclassification.bean.UserBean;
import com.example.rubbishclassification.tools.AppTools;
import com.example.rubbishclassification.tools.AppURI;
import com.example.rubbishclassification.tools.OkHttp3Utils;
import com.example.rubbishclassification.zxing.android.CaptureActivity;
import com.example.rubbishclassification.zxing.bean.ZxingConfig;
import com.example.rubbishclassification.zxing.common.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LaJiDaiFaFangOneActivity extends BaseActivity {

    private String[] permissions = {Manifest.permission.CAMERA};

    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;

    private EditText editText1;
    private EditText editText2;
    private EditText editText3;

    private Button button1;
    private ImageView button2;

    private String saomiaojieguo="";
    private String fangjianhao = "";
    private String clear = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_fafang_page_one);
        setTitleBackVisibity(true);
        setTitleText("垃圾袋发放");

        textView1 = findViewById(R.id.layout_fafang_one_user);
        textView2 = findViewById(R.id.layout_fafang_one_xiaoqu);
        textView3 = findViewById(R.id.layout_fafang_one_fenshu);
        textView4 = findViewById(R.id.layout_fafang_one_saomiaojieguo);

        textView1.setText("当前登录用户："+UserBean.getUserBean().getName());
        textView2.setText("小区："+UserBean.getUserBean().getVillageInfoName());

        editText1 = findViewById(R.id.layout_fafang_one_edit1);
        editText2 = findViewById(R.id.layout_fafang_one_edit2);
        editText3 = findViewById(R.id.layout_fafang_one_edit3);

        button2 = findViewById(R.id.layout_fafang_one_btn_saomiao);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        //下一步
        button1 = findViewById(R.id.layout_fafang_one_btn);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(editText1.getText().toString()) || TextUtils.isEmpty(editText2.getText().toString()) ||TextUtils.isEmpty(editText3.getText().toString())){
                    AppTools.toastShort("请输入房屋数据");
                    return;
                }
                fangjianhao = editText1.getText().toString()+"栋"+editText2.getText().toString()+"单元"+editText3.getText().toString()+"号";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // 检查该权限是否已经获取
                    int i = ContextCompat.checkSelfPermission(LaJiDaiFaFangOneActivity.this, permissions[0]);
                    // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                    if (i != PackageManager.PERMISSION_GRANTED) {
                        // 如果没有授予该权限，就去提示用户请求
                        startRequestPermission();
                    }else{
                        startCaptureActivity();
                    }
                }else{
                    startCaptureActivity();
                }

            }
        });
    }

    public void startCaptureActivity(){
        Intent intent = new Intent(LaJiDaiFaFangOneActivity.this, CaptureActivity.class);
        /*ZxingConfig是配置类
         *可以设置是否显示底部布局，闪光灯，相册，
         * 是否播放提示音  震动
         * 设置扫描框颜色等
         * 也可以不传这个参数
         * */
        ZxingConfig config = new ZxingConfig();
//                config.setPlayBeep(false);//是否播放扫描声音 默认为true
//                config.setShake(false);//是否震动  默认为true
//                config.setDecodeBarCode(false);//是否扫描条形码 默认为true
        config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为白色
        config.setFrameLineColor(R.color.colorAccent);//设置扫描框边框颜色 默认无色
        config.setScanLineColor(R.color.colorAccent);//设置扫描线的颜色 默认白色
//                config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
        config.setShowFlashLight(true);
        config.setShowAlbum(false);
        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
        intent.putExtra("fangjianhao",fangjianhao);
        startActivityForResult(intent, 10006);
    }

    // 开始提交请求权限
    private void startRequestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 321);
    }

    // 用户权限 申请 的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 321) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                    boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                    if (!b) {
                        // 用户还是想用我的 APP 的
                        // 提示用户去应用设置界面手动开启权限
                        showDialogTip();
                    } else
                        finish();
                } else {
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showDialogTip(){
        LayoutInflater factory = LayoutInflater.from(LaJiDaiFaFangOneActivity.this);
        final View view = factory.inflate(R.layout.layout_dialog, null);
        final TextView textView =  view.findViewById(R.id.layout_dialog_text);
        textView.setText("相机权限未开启，请设置开启");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("确定",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToAppSetting();
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击
        dialog.show();
    }

    private void goToAppSetting() {
        Intent intent = new Intent();

        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);

        startActivityForResult(intent, 123);
    }

    @Override
    public void onBackClick(View v) {
        super.onBackClick(v);
        LaJiDaiFaFangOneActivity.this.finish();
    }

    // 物理返回键退出程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            LaJiDaiFaFangOneActivity.this.finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 获取当前用户的一些信息
     * */
    public void getInfo(){
        loading.show();
        //ToDo -设置数据
        String url = AppURI.setDomainUrl(AppURI.upSorting);
        //url += "streetId="+streetId;

        OkHttp3Utils.doGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loading.dismiss();
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
                        loading.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            String code = jsonObject.getString("code");
                            if (code.equals("1")){
                                //ToDo -设置数据

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case 10006:
                    if (data != null) {
                        saomiaojieguo = data.getStringExtra(Constant.CODED_CONTENT);
                        //textView4.setText(saomiaojieguo);
                        //next();
                    }
                    break;
                case 10007:
                    saomiaojieguo = "";
                    fangjianhao = "";
                    editText1.setText("");
                    editText2.setText("");
                    editText3.setText("");
                    break;
            }
        }
        if (requestCode == 123) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 检查该权限是否已经获取
                int i = ContextCompat.checkSelfPermission(this, permissions[0]);
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                if (i != PackageManager.PERMISSION_GRANTED) {
                    // 提示用户应该去应用设置界面手动开启权限
                    showDialogTip();
                } else {

                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent){
        if(intent!=null){
            clear = intent.getExtras().getString("clear");
            if(!TextUtils.isEmpty(clear) && "1".equals(clear)){
                saomiaojieguo = "";
                fangjianhao = "";
                editText1.setText("");
                editText2.setText("");
                editText3.setText("");
            }
        }
    }


    /**
     * 提交
     * */
    public void next(){
        loading.show();
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
                        loading.dismiss();
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
                        loading.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            String code = jsonObject.getString("code");
                            if (code.equals("1")){
                                AppTools.toastLong("发放成功");
                                Intent intent = new Intent(LaJiDaiFaFangOneActivity.this,LaJiDaiFaFangTwoActivity.class);
                                intent.putExtra("fangjianhao",fangjianhao);
                                intent.putExtra("saomiaojieguo",saomiaojieguo);
                                startActivityForResult(intent, 10007);
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
