package com.example.rubbishclassification.activity;


import com.example.rubbishclassification.R;
import com.example.rubbishclassification.adapter.PhotoPreViewAdapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class MyPhotoPreViewActivity extends BaseActivity {

    public static final String EXTRA_PHOTOS = "extra_photos";
    public static final String EXTRA_CURRENT_ITEM = "extra_current_item";
    public static final String EXTRA_RESULT = "preview_result";
    private ViewPager viewPager;

    private ArrayList<String> paths = new ArrayList<>();
    private int currentItem = 0;
    private PhotoPreViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_photo);
        setTitleBackVisibity(true);
        setTitleCommitVisibity(true);
        setTitleCommitText("删除");

        ArrayList<String> pathArr = getIntent().getStringArrayListExtra(EXTRA_PHOTOS);
        if(pathArr != null){
            paths.addAll(pathArr);
        }
        currentItem = getIntent().getIntExtra(EXTRA_CURRENT_ITEM, 0);

        //设置标题显示
        setTitleText((currentItem+1)+"/"+paths.size());

        viewPager = (ViewPager) findViewById(R.id.layout_photo_view_pager);

        adapter = new PhotoPreViewAdapter(this,paths);
        viewPager.setCurrentItem(currentItem);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                setTitleText((position+1)+"/"+paths.size());
                currentItem = position;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    @Override
    public void onBackClick(View v) {
        super.onBackClick(v);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT, paths);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onCommitClick(View v) {
        super.onCommitClick(v);
        if(myDialog == null || !myDialog.isShowing()){
            showDialog();
        }
    }
    private AlertDialog myDialog;

    private void showDialog(){
        String message = "是否确认删除照片";
        LayoutInflater factory = LayoutInflater.from(MyPhotoPreViewActivity.this);
        final View view = factory.inflate(R.layout.layout_dialog, null);
        final TextView textView =  view.findViewById(R.id.layout_dialog_text);
        textView.setText(message);
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
                        if(paths.size()>1){
                            paths.remove(currentItem);
                            adapter.notifyDataSetChanged();
                            dialog.cancel();
                        }else{
                            paths.remove(currentItem);
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_RESULT, paths);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                }).create();
        myDialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击
        myDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_RESULT, paths);
            setResult(RESULT_OK, intent);
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
