package com.example.rubbishclassification.activity;


import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rubbishclassification.MainActivity;
import com.example.rubbishclassification.R;
import com.example.rubbishclassification.zxing.android.CaptureActivity;
import com.example.rubbishclassification.zxing.bean.ZxingConfig;
import com.example.rubbishclassification.zxing.common.Constant;

public class ScanActivity extends BaseActivity {

    private TextView textView = null;
    private int REQUEST_CODE_SCAN = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_scan,true);
        setTitleText("songpeixin");
        setTitleBackVisibity(true);
        setTitleCommitVisibity(true);

        textView = findViewById(R.id.textView2);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScanActivity.this, CaptureActivity.class);
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
//                config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为白色
//                config.setFrameLineColor(R.color.colorAccent);//设置扫描框边框颜色 默认无色
//                config.setScanLineColor(R.color.colorAccent);//设置扫描线的颜色 默认白色
//                config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
                config.setShowFlashLight(true);
                intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                startActivityForResult(intent, REQUEST_CODE_SCAN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                textView.setText("扫描结果为：" + content);
            }
        }
    }

    public void onBackClick(View v) {
        ScanActivity.this.finish();
    }
    public void onCommitClick(View v) {
        Toast.makeText(ScanActivity.this,"点击下一步",Toast.LENGTH_SHORT).show();
    }
}
