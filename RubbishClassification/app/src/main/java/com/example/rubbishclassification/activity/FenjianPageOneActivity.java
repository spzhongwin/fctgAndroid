package com.example.rubbishclassification.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.rubbishclassification.R;
import com.example.rubbishclassification.bean.UserBean;
import com.example.rubbishclassification.photopicker.PhotoPreviewActivity;
import com.example.rubbishclassification.tools.AppTools;
import com.example.rubbishclassification.tools.AppURI;
import com.example.rubbishclassification.tools.OkHttp3Utils;
import com.example.rubbishclassification.zxing.android.CaptureActivity;
import com.example.rubbishclassification.zxing.bean.ZxingConfig;
import com.example.rubbishclassification.zxing.common.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FenjianPageOneActivity extends BaseActivity {

    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private RelativeLayout button1;
    private ImageView button2;
    private Button button3;
    private String streetId	="";
    private String communityId	="";
    private String villageId="";
    private String villageName="";
    private String saomiaojieguo="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_fenjian_page_one);
        setTitleBackVisibity(true);
        setTitleText("垃圾袋分拣");

        textView1 = findViewById(R.id.layout_fenjian_one_uesr);
        textView2 = findViewById(R.id.layout_fenjian_one_text_xiaoqu);
        textView3 = findViewById(R.id.layout_fenjian_one_saomiaojieguo);
        textView1.setText(UserBean.getUserBean().getName());

        //选择小区
        button1 = findViewById(R.id.layout_fenjian_one_btn_select);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FenjianPageOneActivity.this,SelectVillageActivity.class);
                intent.putExtra("from","1");
                startActivityForResult(intent,10001);
            }
        });

        //扫描二维码
        button2 = findViewById(R.id.layout_fenjian_one_btn_saomiao);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FenjianPageOneActivity.this, CaptureActivity.class);
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
                config.setShowFlashLight(false);
                config.setShowAlbum(false);
                intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                startActivityForResult(intent, 10002);
            }
        });

        //下一步
        button3 = findViewById(R.id.layout_fenjian_one_btn_next);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(textView2.getText().toString())){
                    AppTools.toastLong("请先选择小区");
                    return;
                }
                if(TextUtils.isEmpty(textView3.getText().toString())){
                    //AppTools.toastLong("请先扫描二维码");
                    //return;
                }
                Intent intent = new Intent(FenjianPageOneActivity.this,FenjianPageTwoActivity.class);
                intent.putExtra("streetId",streetId);
                intent.putExtra("communityId",communityId);
                intent.putExtra("villageId",villageId);
                intent.putExtra("villageName",villageName);
                intent.putExtra("fanjianhao",saomiaojieguo);
                startActivityForResult(intent, 10003);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                // 选择小区
                case 10001:
                    if(data != null){
                        streetId = data.getStringExtra("streetId");
                        communityId = data.getStringExtra("communityId");
                        villageId = data.getStringExtra("villageId");
                        villageName = data.getStringExtra("villageName");
                        textView2.setText(villageName);
                    }
                    break;
                case 10002:
                    if (data != null) {
                        saomiaojieguo = data.getStringExtra(Constant.CODED_CONTENT);
                        textView3.setText(saomiaojieguo);
                    }
                    break;
                case 10003:
                    if (data != null) {
                        String result = data.getStringExtra("success");
                        if("1".equals(result)){
                            saomiaojieguo = "";
                            textView3.setText(saomiaojieguo);
                        }else{
                            saomiaojieguo = "";
                            textView3.setText(saomiaojieguo);
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onBackClick(View v) {
        super.onBackClick(v);
        FenjianPageOneActivity.this.finish();
    }

    // 物理返回键退出程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            FenjianPageOneActivity.this.finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

}
