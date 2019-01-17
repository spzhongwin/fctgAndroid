package com.example.rubbishclassification.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.rubbishclassification.R;
import com.example.rubbishclassification.bean.UserBean;
import com.example.rubbishclassification.tools.AppTools;
import com.example.rubbishclassification.tools.AppURI;
import com.example.rubbishclassification.tools.OkHttp3Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LaJiDaiFaFangTwoActivity extends BaseActivity {

    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private TextView textView6;

    private Button button1;

    String fangjianhao = "";
    String saomiaojieguo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_fafang_page_two);
        setTitleBackVisibity(true);
        setTitleText("垃圾袋发放");

        textView1 = findViewById(R.id.layout_fafang_two_user);
        textView2 = findViewById(R.id.layout_fafang_two_xiaoqu);
        textView3 = findViewById(R.id.layout_fafang_two_fangjianhao);
        textView4 = findViewById(R.id.layout_fafang_two_fenshu);
        textView5 = findViewById(R.id.layout_fafang_two_yiyong);
        textView6 = findViewById(R.id.layout_fafang_two_shenling);


        fangjianhao = getIntent().getStringExtra("fangjianhao");
        //saomiaojieguo = getIntent().getStringExtra("saomiaojieguo");

        textView1.setText("当前登录用户："+UserBean.getUserBean().getName());
        textView2.setText("小区："+UserBean.getUserBean().getVillageInfoName());
        textView3.setText("房间号："+fangjianhao);


        //下一步
        button1 = findViewById(R.id.layout_fafang_two_btn);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setResult(RESULT_OK);
                Intent intent = new Intent(LaJiDaiFaFangTwoActivity.this,LaJiDaiFaFangOneActivity.class);
                intent.putExtra("clear","1");
                overridePendingTransition(R.anim.left_in,R.anim.right_out);
                startActivity(intent);
                LaJiDaiFaFangTwoActivity.this.finish();
            }
        });
    }

    @Override
    public void onBackClick(View v) {
        super.onBackClick(v);
        //setResult(RESULT_OK);
        Intent intent = new Intent(LaJiDaiFaFangTwoActivity.this,LaJiDaiFaFangOneActivity.class);
        intent.putExtra("clear","1");
        overridePendingTransition(R.anim.left_in,R.anim.right_out);
        startActivity(intent);
        LaJiDaiFaFangTwoActivity.this.finish();
    }

    // 物理返回键退出程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            //setResult(RESULT_OK);
            Intent intent = new Intent(LaJiDaiFaFangTwoActivity.this,LaJiDaiFaFangOneActivity.class);
            intent.putExtra("clear","1");
            overridePendingTransition(R.anim.left_in,R.anim.right_out);
            startActivity(intent);
            LaJiDaiFaFangTwoActivity.this.finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 获取当前用户的一些信息
     * */
    public void next(){
        loading.show();
        //ToDo -设置数据
        String url = AppURI.setDomainUrl(AppURI.upSorting);
        url += "qrCodeId="+saomiaojieguo;
        url += "&roomNumberText="+fangjianhao;
        url += "&bagNumber="+"100";

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

}
