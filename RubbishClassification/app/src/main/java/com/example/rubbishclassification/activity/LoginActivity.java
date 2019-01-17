package com.example.rubbishclassification.activity;


import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.rubbishclassification.MyApplication;
import com.example.rubbishclassification.R;
import com.example.rubbishclassification.bean.UserBean;
import com.example.rubbishclassification.tools.AppTools;
import com.example.rubbishclassification.tools.AppURI;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;



public class LoginActivity extends BaseActivity {

    private EditText editText_name;
    private EditText editText_pass;
    private Button button;

    private String from;//用来标识是否由页面超时进来的，主要操作是登录成功关闭登录页面

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);
        setTitleVisibity(false);

        from = getIntent().getStringExtra("from");

        editText_name = findViewById(R.id.login_edit_name);
        editText_pass = findViewById(R.id.login_edit_pass);
        button = findViewById(R.id.login_button);

        //        账号：分拣员123
        //        密码：md5(md5(18888888888))
        //
        //        账号：物业人员123
        //        密码：md5(md5(18888888888))
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editText_name.getText().toString();
                String password = editText_pass.getText().toString();
                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(password)){
                    AppTools.toastShort("账号密码不能为空");
                    return;
                }
                password = AppTools.getMD5String(AppTools.getMD5String(password));
                try {
                    name = URLEncoder.encode(name, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                final String login_url = AppURI.login + "?name=" + name + "&password=" + password;
                DOGET(login_url,1);
            }
        });
    }


    /**
     * 网络请求的回调函数---成功
     * */
    @Override
    public void requestSuccess(int urlId, String result) {
        super.requestFailure(urlId, result);
        switch (urlId){
            case 1: {
                Log.e("---",result);
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

                        AppTools.toastShort("登录成功");
                        Intent intent = new Intent(LoginActivity.this, MyMainActivity.class);
                        startActivity(intent);
                        LoginActivity.this.finish();
                    }else if("0".equals(code)){
                        AppTools.toastLong(jsonObject.getString("msg"));
                    }
                }catch (Exception e){
                    Log.e("-----","解析异常");
                    return;
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * 网络请求的回调函数---失败
     * */
    @Override
    public void requestFailure(int urlId, String result) {
        super.requestFailure(urlId, result);
        switch (urlId){
            case 1: {
                AppTools.toastShort("登录失败");
                break;
            }
            default:
                break;
        }
    }
}
