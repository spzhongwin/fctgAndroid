package com.example.rubbishclassification.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.example.rubbishclassification.R;
import com.example.rubbishclassification.adapter.GridAdapter;
import com.example.rubbishclassification.adapter.QuestionAdapter;
import com.example.rubbishclassification.bean.QuestionModel;
import com.example.rubbishclassification.bean.AssessmentModel;
import com.example.rubbishclassification.bean.AssessmentOperate;
import com.example.rubbishclassification.bean.UserBean;
import com.example.rubbishclassification.photopicker.PhotoPickerActivity;
import com.example.rubbishclassification.photopicker.PhotoPreviewActivity;
import com.example.rubbishclassification.photopicker.SelectModel;
import com.example.rubbishclassification.photopicker.intent.PhotoPickerIntent;
import com.example.rubbishclassification.photopicker.intent.PhotoPreviewIntent;
import com.example.rubbishclassification.tools.AppTools;
import com.example.rubbishclassification.tools.UploadHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AssessmentActivity extends BaseActivity {
    private String userAssessmentId;

    private TextView tvVillage;//小区
    private TextView tvUser;//操作人
    private TextView tvFractal;//评价总分
    private GridView glQuestionList;//问题选项按钮
    private TextView tvKoufen;//扣分项
    private TextView tvQuestion;//问题描述
    private TextView tvZuzhi;//组织管理
    private TextView tvTaizhang;//台账管理
    private TextView tvFenzhi;//分值
    private TextView tvBukoufen;//不扣分
    private SeekBar seekBar;//扣分seekBar
    private TextView tvKoufenshuLeft;//扣减分数
    private TextView tvKoufenshu;//扣减分数
    private TextView tvKoufenshuRight;//扣减分数
    private EditText editText;//输入框
    private LinearLayout linearLayout;//扣分原因
    private GridView gridView;//拍照列表

    private int curQuestionPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_assessment);
        setTitleText("小区评价");
        setTitleBackVisibity(true);
        setTitleCommitVisibity(true);

        //初始化组件
        initView();
        //获取考核id
        userAssessmentId = getIntent().getStringExtra("userAssessmentId");

        //获取数据
        AssessmentOperate.getInstance().getAssessmentQuestion(userAssessmentId, new AssessmentOperate.AssessmentQuestionCallBack() {
            @Override
            public void getAssQuestionModel(final AssessmentModel model, QuestionModel curQuestionModel) {
                //初始化数据---放到model回调中
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(model.questionList.size() == 0){
                            AppTools.toastShort("数据异常");
                            AssessmentActivity.this.finish();
                        }else{
                            initData(model);
                        }
                    }
                });
            }
            @Override
            public void errorInfo(final String errorInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AppTools.toastLong(errorInfo);
                    }
                });
            }
        });

    }

    private void initView(){
        tvVillage = findViewById(R.id.assessment_text_village);
        tvUser = findViewById(R.id.assessment_text_user);
        tvFractal = findViewById(R.id.assessment_text_fractal);
        glQuestionList = findViewById(R.id.assessment_grid_layout);
        tvKoufen = findViewById(R.id.assessment_text_koufen);
        tvQuestion = findViewById(R.id.assessment_text_question);
        tvZuzhi = findViewById(R.id.assessment_text_zuzhi);
        tvTaizhang = findViewById(R.id.assessment_text_taizhang);
        tvFenzhi = findViewById(R.id.assessment_text_fenzhi);
        tvBukoufen = findViewById(R.id.assessment_text_bukoufen);
        seekBar = findViewById(R.id.assessment_text_seek);
        tvKoufenshuLeft = findViewById(R.id.assessment_text_koufenshu_left);
        tvKoufenshu = findViewById(R.id.assessment_text_koufenshu);
        tvKoufenshuRight = findViewById(R.id.assessment_text_koufenshu_right);
        editText = findViewById(R.id.assessment_edit_koufenyuanyin);
        linearLayout = findViewById(R.id.assessment_layout_koufenxiangqing);
        gridView = findViewById(R.id.assessment_grid_view);
    }

    private QuestionAdapter questionAdapter;
    private ArrayList<QuestionModel> questionModelList;

    private void initData(AssessmentModel model){
        //设置数据
        tvVillage.setText(model.villageName);
        tvUser.setText(UserBean.getUserBean().getName());
        tvFractal.setText(model.totalFraction+"");
        //myfenshu = model.totalFraction;

        questionModelList = model.questionList;
        questionAdapter = new QuestionAdapter(AssessmentActivity.this,questionModelList);
        glQuestionList.setAdapter(questionAdapter);
        glQuestionList.setSelector(new ColorDrawable(Color.TRANSPARENT));
        glQuestionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                //当点击新的按钮时候，先将之前的按钮的isSelected属性设置为false,
                questionModelList.get(curQuestionPosition).isSelected = false;
                if(!questionModelList.get(curQuestionPosition).assessmentInfo.equals(editText.getText().toString())){
                    questionModelList.get(curQuestionPosition).isEdit = true;
                    questionModelList.get(curQuestionPosition).assessmentInfo = editText.getText().toString();
                }
                if ( questionModelList.get(curQuestionPosition).assessmentFraction != seekBarProgress) {
                    questionModelList.get(curQuestionPosition).isEdit = true;
                    questionModelList.get(curQuestionPosition).assessmentFraction = seekBarProgress;
                }
                if ( questionModelList.get(curQuestionPosition).isEdit){
                    AssessmentOperate.getInstance().upAssessmentQuestion(questionModelList.get(position).id, new AssessmentOperate.AssessmentQuestionCallBack() {
                        @Override
                        public void getAssQuestionModel(AssessmentModel model, final QuestionModel curQuestionModel) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //之后成功了之后才更新总分
                                    tvFractal.setText(AssessmentOperate.getInstance().getModel().totalFraction+"");
                                    curQuestionPosition = position;
                                    reload(curQuestionModel);
                                }
                            });
                        }

                        @Override
                        public void errorInfo(final String errorInfo) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AppTools.toastLong(errorInfo);
                                }
                            });
                        }
                    });
                }else{
                    AssessmentOperate.getInstance().setCurQuestionModel(questionModelList.get(position).id);
                    curQuestionPosition = position;
                    reload( AssessmentOperate.getInstance().curQuestionModel());
                }
            }
        });
        if(questionModelList.size() != 0){
            for (int i = 0; i <questionModelList.size() ; i++) {
                if(!questionModelList.get(i).isAnswer){
                    curQuestionPosition = i;
                    AssessmentOperate.getInstance().setCurQuestionModel(questionModelList.get(i).id);
                    break;
                }else{
                    curQuestionPosition = 0;
                    AssessmentOperate.getInstance().setCurQuestionModel(questionModelList.get(0).id);
                }
            }
            reload(AssessmentOperate.getInstance().curQuestionModel());
        }
    }

    //private int myfenshu = 100;

    private int seekBarProgress = 0;

    public void reload(final QuestionModel curQuestionModel){
        //更新选项按钮样式
        curQuestionModel.isSelected = true;
        questionAdapter.notifyDataSetChanged();
        //设置问题的各个选项
        tvQuestion.setText(curQuestionModel.info);

        tvZuzhi.setText(curQuestionModel.oneLevelName);
        tvTaizhang.setText(curQuestionModel.shortName);
        tvFenzhi.setText(curQuestionModel.fraction+"");

        //每次点击切换问题按钮之后清除所有答案列表,seekBar设置为0,输入框清空，拍照gridView 替换为新的列表
        linearLayout.removeAllViews();

        editText.clearFocus();
        if(TextUtils.isEmpty(curQuestionModel.assessmentInfo)){
            editText.setText("");
            editText.setHint("请输入备注...");
        }else{
            editText.setText(curQuestionModel.assessmentInfo);
        }
        //清空本地小图标url数组
        smallImagePaths.clear();
        if(AssessmentOperate.getInstance().curQuestionModel().imgs.size() != 0){
            smallImagePaths.addAll(AssessmentOperate.getInstance().curQuestionModel().imgs);
        }
        seekBarProgress = curQuestionModel.assessmentFraction;
        if(curQuestionModel.assessmentFraction !=0){
            seekBar.setProgress(seekBarProgress);
        }else{
            seekBar.setProgress(0);
        }

        //判断是加分项目还是扣分项目 0 默认扣分，1 加分项
        if(curQuestionModel.assessmentType == 0){
            tvKoufen.setText("扣分");
            tvKoufen.setTextColor(getResources().getColor(R.color.rad));
            tvBukoufen.setText("不扣分");
            tvBukoufen.setTextColor(getResources().getColor(R.color.myColor));
            tvKoufenshuLeft.setText("扣");
            tvKoufenshuLeft.setTextColor(getResources().getColor(R.color.rad));
            tvKoufenshu.setText(seekBarProgress+"");
            tvKoufenshu.setTextColor(getResources().getColor(R.color.rad));
            tvKoufenshuRight.setText("分");
            tvKoufenshuRight.setTextColor(getResources().getColor(R.color.rad));
        }else{
            tvKoufen.setText("加分");
            tvKoufen.setTextColor(getResources().getColor(R.color.myColor));
            tvBukoufen.setText("不加分");
            tvBukoufen.setTextColor(getResources().getColor(R.color.myColor));

            tvKoufenshuLeft.setText("加");
            tvKoufenshuLeft.setTextColor(getResources().getColor(R.color.myColor));
            tvKoufenshu.setText( seekBarProgress+"");
            tvKoufenshu.setTextColor(getResources().getColor(R.color.myColor));
            tvKoufenshuRight.setText("分");
            tvKoufenshuRight.setTextColor(getResources().getColor(R.color.myColor));
        }

        seekBar.setMax(curQuestionModel.fraction);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarProgress = progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                float step = 50/curQuestionModel.fraction;
//                seekBarProgress = Math.round(seekBar.getProgress()/step);
//                Log.e("11111",seekBar.getProgress()+"" );
//                Log.e("-----",seekBarProgress+"" );

                if(AssessmentOperate.getInstance().curQuestionModel().assessmentType == 0){
                    tvKoufenshuLeft.setText("扣");
                    tvKoufenshuLeft.setTextColor(getResources().getColor(R.color.rad));
                    tvKoufenshu.setText(seekBarProgress+"");
                    tvKoufenshu.setTextColor(getResources().getColor(R.color.rad));
                    tvKoufenshuRight.setText("分");
                    tvKoufenshuRight.setTextColor(getResources().getColor(R.color.rad));
                    if(curQuestionModel.assessmentFraction != 0){
                        if((AssessmentOperate.getInstance().getModel().totalFraction+curQuestionModel.assessmentFraction-seekBar.getProgress())<=0){
                            tvFractal.setText("0");
                        }else{
                            tvFractal.setText((AssessmentOperate.getInstance().getModel().totalFraction+curQuestionModel.assessmentFraction-seekBar.getProgress())+"");
                        }
                    }else{
                        if((AssessmentOperate.getInstance().getModel().totalFraction-seekBar.getProgress())<=0){
                            tvFractal.setText("0");
                        }else{
                            tvFractal.setText((AssessmentOperate.getInstance().getModel().totalFraction-seekBar.getProgress())+"");
                        }
                    }
                }else{
                    tvKoufenshuLeft.setText("加");
                    tvKoufenshuLeft.setTextColor(getResources().getColor(R.color.myColor));
                    tvKoufenshu.setText( seekBarProgress+"");
                    tvKoufenshu.setTextColor(getResources().getColor(R.color.myColor));
                    tvKoufenshuRight.setText("分");
                    tvKoufenshuRight.setTextColor(getResources().getColor(R.color.myColor));
                    if(curQuestionModel.assessmentFraction != 0){
                        tvFractal.setText((AssessmentOperate.getInstance().getModel().totalFraction-curQuestionModel.assessmentFraction+seekBar.getProgress())+"");
                    }else{
                        tvFractal.setText((AssessmentOperate.getInstance().getModel().totalFraction+seekBar.getProgress())+"");
                    }

                }
            }
        });

        ArrayList<Map<String,String>> arrayList = new ArrayList<>();
        for (int i = 0; i < curQuestionModel.answerJson.size() ; i++) {
            Map<String,String> map = new HashMap<>();
            map.put("name",curQuestionModel.answerJson.get(i).get("des")+"");
            arrayList.add(map);
        }

//        //添加建议扣分
        for (int i = 0; i < curQuestionModel.answerJson.size();i++) {
            String des = curQuestionModel.answerJson.get(i).get("des")+"";
//            String des1 = des.substring(0, des.indexOf("扣"));
//            String des2 =des.substring(des.indexOf("扣"));
            TextView textView = new TextView(this);
            textView.setText(des);
            textView.setGravity(Gravity.END);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
            textView.setTextColor(getResources().getColor(R.color.black));
            linearLayout.addView(textView);
        }

        //设置拍照
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
                    PhotoPreviewIntent intent = new PhotoPreviewIntent(AssessmentActivity.this);
                    intent.setCurrentItem(position);
                    intent.setPhotoPaths(AssessmentOperate.getInstance().curQuestionModel().imgs);
                    startActivityForResult(intent, REQUEST_PREVIEW_CODE);
                }
            }
        });

        //添加拍照按钮
        smallImagePaths.add("paizhao");
        gridAdapter = new GridAdapter(smallImagePaths,AssessmentActivity.this);
        gridView.setAdapter(gridAdapter);
    }

    private static final int REQUEST_CAMERA_CODE = 10;
    private static final int REQUEST_PREVIEW_CODE = 20;

    private GridAdapter gridAdapter;
    private ArrayList<String> smallImagePaths = new ArrayList<>();

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
                    if(ListExtra.size() != smallImagePaths.size()){
                        loadAdpater(ListExtra);
                    }
                    break;
            }
        }
    }

    private void loadAdpater( ArrayList<String> paths){
        if (AssessmentOperate.getInstance().curQuestionModel().imgs!=null&& AssessmentOperate.getInstance().curQuestionModel().imgs.size()>0){
            AssessmentOperate.getInstance().curQuestionModel().imgs.clear();
        }
        AssessmentOperate.getInstance().curQuestionModel().imgs.addAll(paths);

        if (smallImagePaths !=null && smallImagePaths.size()>0){
            smallImagePaths.clear();
        }
        if(paths.size()<5){
            paths.add("paizhao");
        }
        smallImagePaths.addAll(paths);
        gridAdapter.notifyDataSetChanged();
    }

    //拍照图片路径
    private File  filePath;
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
                    Uri contentUri = FileProvider.getUriForFile(AssessmentActivity.this, AssessmentActivity.this.getApplicationContext().getPackageName() + ".provider", filePath);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                } else {    //否则使用Uri.fromFile(file)方法获取Uri
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(filePath));
                }
                startActivityForResult(intent, REQUEST_CAMERA_CODE);
            }
        }else{
            showDialogTip();
        }

    }


    @Override
    public void onBackClick(View v) {
        super.onBackClick(v);
        if(myDialog == null || !myDialog.isShowing()){
            showDialog("确定要退出评价吗？",0);
        }
    }

    @Override
    public void onCommitClick(View v) {
        super.onCommitClick(v);
        //提交按钮事件
        commitQuestion(curQuestionPosition);
    }

    // 物理返回键退出程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if(myDialog == null || !myDialog.isShowing()){
                showDialog("确定要退出评价吗？",0);
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private  AlertDialog myDialog;

    private void showDialog(String string, final int oprate){
        LayoutInflater factory = LayoutInflater.from(AssessmentActivity.this);
        final View view = factory.inflate(R.layout.layout_dialog, null);
        final TextView textView =  view.findViewById(R.id.layout_dialog_text);
        textView.setText(string);
        myDialog = new AlertDialog.Builder(this)
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
                        //掉保存接口，然后返回
                        if(oprate == 0){
                            backSaveQuestion(curQuestionPosition);
                        }else{
                            Intent intent = new Intent(AssessmentActivity.this,FinishActivity.class);
                            intent.putExtra("pingjia", AssessmentOperate.getInstance().getModel().totalFraction+"");
                            intent.putExtra("xiaoqu",AssessmentOperate.getInstance().getModel().villageName );
                            intent.putExtra("userAssessmentId",userAssessmentId );
                            startActivity(intent);
                        }
                    }
                }).create();
        myDialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击
        myDialog.show();
    }

    /**
     * 相机权限未开启提示
     * */
    private void showDialogTip(){
        LayoutInflater factory = LayoutInflater.from(AssessmentActivity.this);
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
                        Intent intent =new Intent(Settings.ACTION_SETTINGS);
                        AssessmentActivity.this.startActivity(intent);
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击
        dialog.show();
    }

    private String uploadMyImage(String filePath){
        //获取图片
//        byte[] bytes = AppTools.compressImage(BitmapFactory.decodeFile(filePath));

        final String myfilePath = AppTools.compressUpImage(filePath);
        Log.e("1----1----1----",myfilePath);
        String objectKey = UploadHelper.getObjectImageKey(filePath);
        return  UploadHelper.uploadAsync(objectKey, myfilePath, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest putObjectRequest, PutObjectResult putObjectResult) {
                Log.e("---","图片上传成功");
                AssessmentOperate.getInstance().curQuestionModel().isEdit = true;
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

    /**
     * 返回按钮事件
     * */
    private void backSaveQuestion(int position){
        if(!AssessmentOperate.getInstance().curQuestionModel().assessmentInfo.equals(editText.getText().toString())){
            AssessmentOperate.getInstance().curQuestionModel().isEdit = true;
            AssessmentOperate.getInstance().curQuestionModel().assessmentInfo = editText.getText().toString();
        }
        if (AssessmentOperate.getInstance().curQuestionModel().assessmentFraction != seekBarProgress) {
            AssessmentOperate.getInstance().curQuestionModel().isEdit = true;
            AssessmentOperate.getInstance().curQuestionModel().assessmentFraction = seekBarProgress;
        }
        if (AssessmentOperate.getInstance().curQuestionModel().isEdit){
            loading.show();
            AssessmentOperate.getInstance().upAssessmentQuestion(questionModelList.get(position).id, new AssessmentOperate.AssessmentQuestionCallBack() {
                @Override
                public void getAssQuestionModel(AssessmentModel model, final QuestionModel curQuestionModel) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //上传数据成功之后返回
                            loading.dismiss();
                            AssessmentOperate.clearAssessmentQuestion();
                            AssessmentActivity.this.finish();
                        }
                    });
                }

                @Override
                public void errorInfo(final String errorInfo) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loading.dismiss();
                            AppTools.toastLong(errorInfo);
                            AssessmentActivity.this.finish();
                        }
                    });
                }
            });
        }else{
            AssessmentOperate.clearAssessmentQuestion();
            AssessmentActivity.this.finish();
        }
    }

    /**
     * 提交按钮事件
     * */
    private void commitQuestion(final int position){
        if(!questionModelList.get(position).assessmentInfo.equals(editText.getText().toString())){
            questionModelList.get(position).isEdit = true;
            questionModelList.get(position).assessmentInfo = editText.getText().toString();
        }
        if (questionModelList.get(position).assessmentFraction != seekBarProgress) {
            questionModelList.get(position).isEdit = true;
            questionModelList.get(position).assessmentFraction = seekBarProgress;
        }

        if (questionModelList.get(position).isEdit){
            //loading.show();
            AssessmentOperate.getInstance().upAssessmentQuestion(questionModelList.get(position).id, new AssessmentOperate.AssessmentQuestionCallBack() {
                @Override
                public void getAssQuestionModel(AssessmentModel model, final QuestionModel curQuestionModel) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //上传数据成功之后返回
                            //loading.dismiss();
                            tvFractal.setText(AssessmentOperate.getInstance().getModel().totalFraction+"");
                            boolean isAllOk = true;
                            for (int i = 0; i <questionModelList.size() ; i++) {
                                if(!questionModelList.get(i).isAnswer){
                                    isAllOk = false;
                                    showDialog("题目还未答完，确定要提交吗？",2);
                                    break;
                                }
                            }
                            if(isAllOk){
                                Intent intent = new Intent(AssessmentActivity.this,FinishActivity.class);
                                intent.putExtra("pingjia", AssessmentOperate.getInstance().getModel().totalFraction+"");
                                intent.putExtra("xiaoqu",AssessmentOperate.getInstance().getModel().villageName );
                                intent.putExtra("userAssessmentId",userAssessmentId );
                                startActivity(intent);
                            }
                        }
                    });
                }

                @Override
                public void errorInfo(final String errorInfo) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loading.dismiss();
                            AppTools.toastLong(errorInfo);
                        }
                    });
                }
            });
        }else{
            boolean isAllOk = true;
            for (int i = 0; i <questionModelList.size() ; i++) {
                if(!questionModelList.get(i).isAnswer){
                    isAllOk = false;
                    showDialog("题目还未答完，确定要提交吗？",2);
                    break;
                }
            }
            if(isAllOk){
                Intent intent = new Intent(AssessmentActivity.this,FinishActivity.class);
                intent.putExtra("pingjia", AssessmentOperate.getInstance().getModel().totalFraction+"");
                intent.putExtra("xiaoqu",AssessmentOperate.getInstance().getModel().villageName );
                intent.putExtra("userAssessmentId",userAssessmentId );
                startActivity(intent);
            }
        }
    }

    private void myThreadDo(){
        String uploadMyImagePath =  uploadMyImage(filePath.getPath());
        //将图片地址添加到model中的地址数组中，
        AssessmentOperate.getInstance().curQuestionModel().imgs.add(uploadMyImagePath);
        //将小图添加到本地数组中，用于图片预览
        if(smallImagePaths.contains("paizhao")){
            smallImagePaths.remove("paizhao");
        }

        smallImagePaths.add(filePath.getPath());
        if(smallImagePaths.size()<5){
            smallImagePaths.add("paizhao");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading.dismiss();
                gridView.setAdapter(gridAdapter);
            }
        });
    }
}
