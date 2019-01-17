package com.example.rubbishclassification.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.rubbishclassification.MyApplication;
import com.example.rubbishclassification.R;
import com.example.rubbishclassification.bean.AssessmentModel;
import com.example.rubbishclassification.bean.UserBean;
import com.example.rubbishclassification.tools.AppTools;
import com.example.rubbishclassification.tools.AppURI;
import com.example.rubbishclassification.tools.OkHttp3Utils;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FinishActivity extends BaseActivity {

    private TextView textView1;
    private TextView textView2;
    private Button button;
    private String userAssessmentId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_finish);
        setTitleBackVisibity(true);
        setTitleText("评价结果");

        String pingjia = getIntent().getStringExtra("pingjia");
        String xiaoqu = getIntent().getStringExtra("xiaoqu");
        userAssessmentId = getIntent().getStringExtra("userAssessmentId");

        textView1 = findViewById(R.id.layout_finish_zongfen);
        textView2 = findViewById(R.id.layout_finish_xiaoqu);

        textView1.setText(pingjia);
        textView2.setText(xiaoqu);

        button = findViewById(R.id.layout_finish_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.show();
                myfinish();
            }
        });

    }

    @Override
    public void onBackClick(View v) {
        super.onBackClick(v);
        FinishActivity.this.finish();
    }

    // 物理返回键退出程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            FinishActivity.this.finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void myfinish(){
        String url = AppURI.setDomainUrl(AppURI.finshAssessment);
        url += "userAssessmentId="+userAssessmentId;
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
                                startActivity(new Intent(FinishActivity.this,MyMainActivity.class));
                                AppTools.toastLong("考核完成");
                                FinishActivity.this.finish();
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
