package com.example.rubbishclassification.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.example.rubbishclassification.R;
import com.example.rubbishclassification.adapter.GridAdapter;
import com.example.rubbishclassification.bean.UserBean;
import com.example.rubbishclassification.photopicker.PhotoPreviewActivity;
import com.example.rubbishclassification.photopicker.intent.PhotoPreviewIntent;
import com.example.rubbishclassification.tools.AppTools;
import com.example.rubbishclassification.tools.AppURI;
import com.example.rubbishclassification.tools.OkHttp3Utils;
import com.example.rubbishclassification.tools.UploadHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FenjianPageTwoActivity extends BaseActivity {

    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private LinearLayout linearLayout;

    private Button button;
    private GridView gridView;

    private String streetId	="";
    private String communityId	="";
    private String villageId="";
    private String remarks="";
    private String villageName="";
    private String qrCodeId="";

    private ArrayList<String> myImgList = new ArrayList<>();
    private GridAdapter gridAdapter;

    private ArrayList<String> myImgListNeedUpLoad = new ArrayList<>();

    private static final int REQUEST_CAMERA_CODE = 10;
    private static final int REQUEST_PREVIEW_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_fenjian_page_two);
        setTitleBackVisibity(true);
        setTitleText("垃圾袋督导");

        streetId = getIntent().getStringExtra("streetId");
        communityId = getIntent().getStringExtra("communityId");
        villageId = getIntent().getStringExtra("villageId");
        villageName = getIntent().getStringExtra("villageName");
        qrCodeId = getIntent().getStringExtra("qrCodeId");

        textView1 = findViewById(R.id.layout_fenjian_two_xiaoqu);
        textView2 = findViewById(R.id.layout_fenjian_two_fangjianhao);
        textView3= findViewById(R.id.layout_fenjian_two_user);
        textView4 = findViewById(R.id.layout_fenjian_two_buchong);
        textView5 = findViewById(R.id.layout_fenjian_two_buchongxinxi);
        linearLayout = findViewById(R.id.layout_fenjian_two_layout);

        textView1.setText("小区："+villageName);
        //textView2.setText(qrCodeId);
        textView3.setText("当前登录用户："+UserBean.getUserBean().getName());

        gridView = findViewById(R.id.layout_fenjian_two_grid_view);

        //弹框输入
        textView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogEdit();
            }
        });

        button = findViewById(R.id.layout_fenjian_two_commit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if(myImgListNeedUpLoad.size() == 0){
                  AppTools.toastShort("请拍照");
                  return;
              }else if(TextUtils.isEmpty(remarks)){
                  AppTools.toastShort("请补充原因");
                  return;
              }else {
                  loading.show();
                  myfinish();
              }
            }
        });

        //拍照
        int cols = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().densityDpi;
        cols = cols < 4 ? 4 : cols;
        gridView.setNumColumns(cols);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String imgs = (String) parent.getItemAtPosition(position);
                if ("paizhao".equals(imgs) ){
                    takeCamera();
                }else{
                    PhotoPreviewIntent intent = new PhotoPreviewIntent(FenjianPageTwoActivity.this);
                    intent.setCurrentItem(position);
                    intent.setPhotoPaths(myImgListNeedUpLoad);
                    startActivityForResult(intent, REQUEST_PREVIEW_CODE);
                }
            }
        });

        //添加拍照按钮
        myImgList.add("paizhao");
        gridAdapter = new GridAdapter(myImgList,FenjianPageTwoActivity.this);
        gridView.setAdapter(gridAdapter);

    }

    private File filePath;
    private void takeCamera(){
        if(AppTools.cameraIsCanUse()){
            //用于保存调用相机拍照后所生成的文件
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                        .WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "请开通相关权限，否则无法正常使用", Toast.LENGTH_SHORT).show();
                }
            } else {
                filePath = new File(Environment.getExternalStorageDirectory().getPath(), System.currentTimeMillis() + ".jpg");
                //跳转到调用系统相机
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //判断版本
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   //如果在Android7.0以上,使用FileProvider获取Uri
                    intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    Uri contentUri = FileProvider.getUriForFile(FenjianPageTwoActivity.this, FenjianPageTwoActivity.this.getApplicationContext().getPackageName() + ".provider",filePath );
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                } else {    //否则使用Uri.fromFile(file)方法获取Uri
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(filePath));
                }
                startActivityForResult(intent, REQUEST_CAMERA_CODE);
            }
        }else{
            showDialogTip("请开启相机权限",0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                // 选择照片
                case REQUEST_CAMERA_CODE:
                    loading.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //进行自己的操作
                            myThreadDo();
                        }
                    }).start();
                    break;
                // 预览
                case REQUEST_PREVIEW_CODE:
                    ArrayList<String> ListExtra = data.getStringArrayListExtra(PhotoPreviewActivity.EXTRA_RESULT);
                    if(ListExtra.size() != myImgListNeedUpLoad.size()){
                        loadAdpater(ListExtra);
                    }
                    break;
            }
        }
    }

    private void loadAdpater( ArrayList<String> paths){
        if (myImgListNeedUpLoad !=null && myImgListNeedUpLoad.size()>0){
            myImgListNeedUpLoad.clear();
        }
        myImgListNeedUpLoad.addAll(paths);
        if (myImgList !=null && myImgList.size()>0){
            myImgList.clear();
        }
        if(paths.size()<5){
            paths.add("paizhao");
        }
        myImgList.addAll(paths);

        gridAdapter.notifyDataSetChanged();
    }

    /**
     * 开启线程上传
     * */
    private void myThreadDo(){
        String uploadMyImagePath =  uploadMyImage(filePath.getPath());

        myImgListNeedUpLoad.add(uploadMyImagePath);
        if(myImgList.contains("paizhao")){
            myImgList.remove("paizhao");
        }
        myImgList.add(filePath.getPath());
        if(myImgList.size()<5){
            myImgList.add("paizhao");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading.dismiss();
                gridView.setAdapter(gridAdapter);
            }
        });
    }

    /**
     * 上传图片
     * */
    private String uploadMyImage(String filePath){
        final String myfilePath = AppTools.compressUpImage(filePath);
        Log.e("1----1----1----",myfilePath);
        String objectKey = UploadHelper.getObjectImageKey(filePath);
        return  UploadHelper.uploadAsync(objectKey, myfilePath, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest putObjectRequest, PutObjectResult putObjectResult) {
                Log.e("---","图片上传成功");
                //删除本地压缩图片
                try {
                    // 找到文件所在的路径并删除该文件
                    File file = new File(Environment.getExternalStorageDirectory(), myfilePath);
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(PutObjectRequest putObjectRequest, ClientException e, ServiceException e1) {
                //弹框提示失败
                Log.e("---","上传失败");
            }
        });
    }

    @Override
    public void onBackClick(View v) {
        super.onBackClick(v);
        showDialogTip("确定退出垃圾袋督导吗？",1);
    }

    // 物理返回键退出程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            showDialogTip("确定退出垃圾袋督导吗？",1);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 提交按钮事件
     * */
    public void myfinish(){
        String url = AppURI.setDomainUrl(AppURI.upSorting);
        url += "streetId="+streetId;
        url += "&communityId="+communityId;
        url += "&villageId="+villageId;
        try {
            url += "&remarks="+URLEncoder.encode(remarks,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        url += "&qrCodeId="+qrCodeId;
       if(myImgListNeedUpLoad.size() != 0){
           url += "&imgs=[";
           for (String map : myImgListNeedUpLoad) {
               url += "{\"url\":" + "\""+map+"\"";
               url += "},";
           }
           url = url.substring(0,url.length()-1);
           url += "]";
       }

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
                                AppTools.toastLong("提交成功");
                                Intent intent = new Intent();
                                intent.putExtra("success","1");
                                setResult(RESULT_OK,intent);
                                FenjianPageTwoActivity.this.finish();
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

    /**
     * 弹出输入框
     * */
    private void showDialogEdit(){
        LayoutInflater factory = LayoutInflater.from(FenjianPageTwoActivity.this);
        final View view = factory.inflate(R.layout.layout_dialog_edit, null);
        final EditText editText =  view.findViewById(R.id.layout_dialog_edit);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //软键盘隐藏
                        InputMethodManager imm = (InputMethodManager) FenjianPageTwoActivity.this. getSystemService(FenjianPageTwoActivity.this.INPUT_METHOD_SERVICE);
                        if (imm != null && editText != null){
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0); //强制隐藏键盘
                        }
                        dialog.cancel();
                    }
                })
                .setPositiveButton("确定",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!TextUtils.isEmpty(editText.getText().toString())){
                            remarks = editText.getText().toString();
                            linearLayout.setVisibility(View.VISIBLE);
                            textView5.setText(remarks.trim());
                        }else{
                            remarks = "";
                            textView5.setText(remarks);
                            linearLayout.setVisibility(View.INVISIBLE);
                        }
                        //软键盘隐藏
                        InputMethodManager imm = (InputMethodManager) FenjianPageTwoActivity.this. getSystemService(FenjianPageTwoActivity.this.INPUT_METHOD_SERVICE);
                        if (imm != null && editText != null){
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0); //强制隐藏键盘
                        }
                        dialog.cancel();
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击
        dialog.show();
    }

    /**
     * 相机权限未开启提示
     * */
    private void showDialogTip(String message,final int id){
        LayoutInflater factory = LayoutInflater.from(FenjianPageTwoActivity.this);
        final View view = factory.inflate(R.layout.layout_dialog, null);
        final TextView textView =  view.findViewById(R.id.layout_dialog_text);
        textView.setText(message);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if(id == 0){
                            Intent intent =new Intent(Settings.ACTION_SETTINGS);
                            FenjianPageTwoActivity.this.startActivity(intent);
                        }else{
                            Intent intent = new Intent();
                            intent.putExtra("success","0");
                            setResult(RESULT_OK,intent);
                            FenjianPageTwoActivity.this.finish();
                        }
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击
        dialog.show();
    }
}
