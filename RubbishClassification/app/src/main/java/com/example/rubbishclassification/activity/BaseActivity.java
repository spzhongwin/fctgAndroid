package com.example.rubbishclassification.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.rubbishclassification.MyApplication;
import com.example.rubbishclassification.R;
import com.example.rubbishclassification.dialog.CustomProgressDialog;
import com.example.rubbishclassification.dialog.DialogUtils;
import com.example.rubbishclassification.tools.AppManager;
import com.example.rubbishclassification.tools.AppTools;
import com.example.rubbishclassification.tools.GsonUtil;
import com.example.rubbishclassification.tools.OkHttp3Utils;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout relativeLayoutBase;
    private RelativeLayout titleLayoutBase;
    private LinearLayout titleBack;
    private TextView titleText;
    private TextView titleCommit;
    private ImageView titleCommitIcon;
    public Dialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.layout_title);
        initTitleView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        //将activity放入Activity栈中
        AppManager.addActivity(this);

        //实例化loading;
        //loading = new DialogUtils(this);
        loading = CustomProgressDialog.createLoadingDialog(this,"加载中...");
        loading.setCancelable(true);
        loading.setCanceledOnTouchOutside(false);
    }

    /**
     * 初始化布局组件
     */
    private void initTitleView() {
        relativeLayoutBase =  findViewById(R.id.layout_base);
        titleLayoutBase = findViewById(R.id.layout_title_base);
        titleBack = findViewById(R.id.title_back);
        titleText = findViewById(R.id.title_text);
        titleCommit =  findViewById(R.id.title_commit);
        titleCommitIcon = findViewById(R.id.title_commit_icon);
        titleBack.setOnClickListener(this);
        titleCommit.setOnClickListener(this);
        titleCommitIcon.setOnClickListener(this);
    }

    /**
     * 设置布局，将title布局添加到目标布局头部
     */
    @Override
    public void setContentView(int resId) {
        View view = getLayoutInflater().inflate(resId, null);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.BELOW, R.id.layout_title_base);
        if (null != relativeLayoutBase) {
            relativeLayoutBase.addView(view, lp);
        }

    }

    /**
     * 设置布局，将title布局添加到目标布局头部
     */
    public void setContentView(int resId , Boolean isShowTitle) {
        setContentView(resId);
        if(!isShowTitle){
            titleLayoutBase.setVisibility(View.GONE);
        }else{
            titleLayoutBase.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 设置中间标题文字
     */
    public void setTitleText(CharSequence c) {
        if (titleText != null) {
            titleText.setText(c);
        }
    }

    /**
     * 设置标题是否显示
     */
    public void setTitleVisibity(boolean visible) {
        if (titleText != null) {
            if (visible)
                titleText.setVisibility(View.VISIBLE);
            else
                titleText.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 设置左边按钮是否显示
     */
    public void setTitleBackVisibity(boolean visible) {
        if (titleBack != null) {
            if (visible)
                titleBack.setVisibility(View.VISIBLE);
            else
                titleBack.setVisibility(View.GONE);
        }
    }

    /**
     * 设置右边按钮是否显示
     */
    public void setTitleCommitVisibity(boolean visible) {
        if (titleCommit != null) {
            if (visible)
                titleCommit.setVisibility(View.VISIBLE);
            else
                titleCommit.setVisibility(View.GONE);
        }
    }
    /**
     * 设置右边按钮文本
     */
    public void setTitleCommitText(String text) {
        if (titleCommit != null) {
            titleCommit.setVisibility(View.VISIBLE);
            titleCommitIcon.setVisibility(View.GONE);
            titleCommit.setText(text);
        }
    }
    /**
     * 设置右边按钮icon
     */
    public void setTitleCommitIcon(int id) {
        titleCommit.setVisibility(View.GONE);
        titleCommitIcon.setVisibility(View.VISIBLE);
        titleCommitIcon.setBackground(getResources().getDrawable(id));
    }

    /**
     * 按钮点击事件
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.title_back:
                onBackClick(v);
                break;

            case R.id.title_commit:
                onCommitClick(v);
                break;
            case R.id.title_commit_icon:
                onCommitClick(v);
                break;
            default:
                break;
        }
    }

    /**
     * 设置左边按钮点击事件回调
     */
    public void onBackClick(View v) {

    }

    /**
     * 设置右边按钮点击事件回调
     */
    public void onCommitClick(View v) {

    }

    /**
     * 网络封装
     */
    private static final int CALLBACK_SUCCESSFUL=0x01;
    private static final int CALLBACK_FAILED=0x02;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CALLBACK_SUCCESSFUL: {
                    requestSuccess(msg.arg1,(String)msg.obj);
                    break;
                }
                case CALLBACK_FAILED: {
                    requestFailure(msg.arg1,(String)msg.obj);
                    break;
                }
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    /**
     * 网络封装-get请求，urlId 区分当前Activity多个请求
     */
    public void DOGET(String url, final int urlId){
        if(!AppTools.isNetworkAvailable(MyApplication.getContext())){
            AppTools.toastLong("请检查网络");
            return;
        }
        OkHttp3Utils.doGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message message=Message.obtain();
                message.what=CALLBACK_FAILED;
                message.obj = e.toString();
                message.arg1 = urlId;
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                Message message=Message.obtain();
                message.arg1 = urlId;
                String string = response.body().string();
                message.what=CALLBACK_SUCCESSFUL;
                message.obj = string;
                handler.sendMessage(message);
            }
        });
    }

    public void requestFailure(int urlId,String result){

    }

    public void requestSuccess(int urlId,String result){

    }

}
