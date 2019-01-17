package com.example.rubbishclassification.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.example.rubbishclassification.MainActivity;
import com.example.rubbishclassification.R;
import com.example.rubbishclassification.dialog.DialogUtils;
import com.example.rubbishclassification.photopicker.PhotoPickerActivity;
import com.example.rubbishclassification.photopicker.PhotoPreviewActivity;
import com.example.rubbishclassification.photopicker.SelectModel;
import com.example.rubbishclassification.photopicker.intent.PhotoPickerIntent;
import com.example.rubbishclassification.photopicker.intent.PhotoPreviewIntent;
import com.example.rubbishclassification.tools.AppTools;
import com.example.rubbishclassification.tools.UploadHelper;

import org.json.JSONArray;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class UploadImageActivity extends BaseActivity {

    private static final int REQUEST_CAMERA_CODE = 10;
    private static final int REQUEST_PREVIEW_CODE = 20;
    private ArrayList<String> imagePaths = new ArrayList<>();

    private int number = 0;

    private GridView gridView;
    private GridAdapter gridAdapter;
    private TextView tv_click;
    private EditText textView;
    private String TAG =MainActivity.class.getSimpleName();

    private DialogUtils loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_uploading,false);
        setTitleText("上传图片");
        setTitleBackVisibity(true);
        setTitleCommitVisibity(true);
        gridView = (GridView) findViewById(R.id.gridView);
        tv_click = (TextView) findViewById(R.id.find_comment_submit);
        textView= (EditText)findViewById(R.id.et_context);

        // 设置loading
        loading = new DialogUtils(this);

        int cols = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().densityDpi;
        cols = cols < 3 ? 3 : cols;
        gridView.setNumColumns(cols);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                String imgs = (String) parent.getItemAtPosition(position);
                if ("paizhao".equals(imgs) ){
                    PhotoPickerIntent intent = new PhotoPickerIntent(UploadImageActivity.this);
                    intent.setSelectModel(SelectModel.MULTI);
                    intent.setShowCarema(true); // 是否显示拍照
                    intent.setMaxTotal(6); // 最多选择照片数量，默认为6
                    intent.setSelectedPaths(imagePaths); // 已选中的照片地址， 用于回显选中状态
                    startActivityForResult(intent, REQUEST_CAMERA_CODE);
                }else{
                    Toast.makeText(UploadImageActivity.this,"1"+position,Toast.LENGTH_SHORT).show();
                    PhotoPreviewIntent intent = new PhotoPreviewIntent(UploadImageActivity.this);
                    intent.setCurrentItem(position);
                    intent.setPhotoPaths(imagePaths);
                    startActivityForResult(intent, REQUEST_PREVIEW_CODE);
                }
            }
        });
        imagePaths.add("paizhao");
        gridAdapter = new GridAdapter(imagePaths);
        gridView.setAdapter(gridAdapter);
        tv_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String uploaduel = UploadHelper.uploadImage("/storage/emulated/0/XRichText/1534936652406-");
                //Log.e( "testurl:",uploaduel);
               if(imagePaths.size() > 1){
                   loading.show();
                   uploadMyImage();
               }else{
                   AppTools.toastShort("请先拍照");
               }
            }
        });
    }

    //递归上传图片
    private void uploadMyImage(){
        if (number == imagePaths.size() - 1) {
            //关闭dialog
            loading.dismiss();
            AppTools.toastShort("上传成功");

            return;
        }
        //获取图片
        byte[] bytes = AppTools.compressImage(BitmapFactory.decodeFile(imagePaths.get(number)));
        String objectKey = UploadHelper.getObjectImageKey(imagePaths.get(number));
//        UploadHelper.uploadAsync(objectKey, bytes, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
//            @Override
//            public void onSuccess(PutObjectRequest putObjectRequest, PutObjectResult putObjectResult) {
//                if(number < imagePaths.size() - 1){
//                    number++;
//                    uploadMyImage();
//                }
//            }
//
//            @Override
//            public void onFailure(PutObjectRequest putObjectRequest, ClientException e, ServiceException e1) {
//                //关闭进度条
//                //弹框提示失败
//                loading.dismiss();
//                AppTools.toastShort("上传失败");
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                // 选择照片
                case REQUEST_CAMERA_CODE:
                    ArrayList<String> list = data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT);
                    Log.d(TAG, "数量："+list.size());
                    loadAdpater(list);
                    break;
                // 预览
                case REQUEST_PREVIEW_CODE:
                    ArrayList<String> ListExtra = data.getStringArrayListExtra(PhotoPreviewActivity.EXTRA_RESULT);
                    loadAdpater(ListExtra);
                    break;
            }
        }
    }

    private void loadAdpater(ArrayList<String> paths){
        if (imagePaths!=null&& imagePaths.size()>0){
            imagePaths.clear();
        }
        if (paths.contains("paizhao")){
            paths.remove("paizhao");
        }
        paths.add("paizhao");
        imagePaths.addAll(paths);
        gridAdapter  = new GridAdapter(imagePaths);
        gridView.setAdapter(gridAdapter);
        try{
            JSONArray obj = new JSONArray(imagePaths);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private class GridAdapter extends BaseAdapter {
        private ArrayList<String> listUrls;
        private LayoutInflater inflater;
        public GridAdapter(ArrayList<String> listUrls) {
            this.listUrls = listUrls;
            if(listUrls.size() == 7){
                listUrls.remove(listUrls.size()-1);
            }
            inflater = LayoutInflater.from(UploadImageActivity.this);
        }

        public int getCount(){
            return  listUrls.size();
        }
        @Override
        public String getItem(int position) {
            return listUrls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item, parent,false);
                holder.image = (ImageView) convertView.findViewById(R.id.imageView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            final String path=listUrls.get(position);
            if (path.equals("paizhao")){
                holder.image.setImageResource(R.mipmap.find_add_img);
            }else {
                Glide.with(UploadImageActivity.this)
                        .load(path)
                        .placeholder(R.mipmap.default_error)
                        .error(R.mipmap.default_error)
                        .centerCrop()
                        .crossFade()
                        .into(holder.image);
            }
            return convertView;
        }
        class ViewHolder {
            ImageView image;
        }
    }

    public void onBackClick(View v) {
        UploadImageActivity.this.finish();
    }
    public void onCommitClick(View v) {
        Toast.makeText(UploadImageActivity.this,"点击下一步",Toast.LENGTH_SHORT).show();
    }
}
