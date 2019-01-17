package com.example.rubbishclassification.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rubbishclassification.MyApplication;
import com.example.rubbishclassification.R;
import com.example.rubbishclassification.adapter.ViewpagerAdapter;
import com.example.rubbishclassification.bean.UserBean;
import com.example.rubbishclassification.tools.AppTools;
import com.example.rubbishclassification.tools.AppURI;
import com.example.rubbishclassification.tools.OkHttp3Utils;
import com.example.rubbishclassification.zxing.android.CaptureActivity;
import com.example.rubbishclassification.zxing.bean.ZxingConfig;
import com.example.rubbishclassification.zxing.common.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MyMainActivity extends BaseActivity {
    Handler mHandler = new Handler();
    //private ViewPager viewPager;
    private List<String> list;
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private ImageView imageView1;
    private ImageView imageView2;
    private List<Map<String, String>> assessmentList;
    private AlertDialog dialog;
    // 物理返回键退出程序
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        setTitleCommitVisibity(true);
        setTitleCommitIcon(R.mipmap.loginout);
        initViewPager();

        //判断角色显示不同的UI和不同的进入页面
        //1 是 分拣人员，2是物业人员，默认分拣
        if ("2".equals(UserBean.getUserBean().getRole())) {
            textView1.setText("发放袋子");
            textView2.setText("发放记录");
            imageView1.setImageResource(R.mipmap.layout_main_rubbishbag);
            imageView2.setImageResource(R.mipmap.layout_main_record);
        } else {
            textView1.setText("小区评价");
            textView2.setText("分拣");
            imageView1.setImageResource(R.mipmap.layout_main_appraise);
            imageView2.setImageResource(R.mipmap.layout_main_classify);
        }
        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("2".equals(UserBean.getUserBean().getRole())) {
                    //进入发放袋子
                    startActivity(new Intent(MyMainActivity.this, LaJiDaiFaFangOneActivity.class));
                } else {
                    //小区评价
                    String url = AppURI.getIsHaveAssessment + "?token=" + UserBean.getUserBean().getToken() + "&userId=" + UserBean.getUserBean().getId();
                    loading.show();
                    DOGET(url, 1);
                }
            }
        });

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("2".equals(UserBean.getUserBean().getRole())) {
                    //发放记录
                    startActivity(new Intent(MyMainActivity.this, JiLuActivity.class));
                } else {
                    //分拣
                    //startActivity(new Intent(MyMainActivity.this,FenjianPageOneActivity.class));
                    Intent intent = new Intent(MyMainActivity.this, CaptureActivity.class);
                    /*ZxingConfig是配置类
                     *可以设置是否显示底部布局，闪光灯，相册，
                     * 是否播放提示音  震动
                     * 设置扫描框颜色等
                     * 也可以不传这个参数
                     * */
                    ZxingConfig config = new ZxingConfig();
                    config.setPlayBeep(false);//是否播放扫描声音 默认为true
//                config.setShake(false);//是否震动  默认为true
//                config.setDecodeBarCode(false);//是否扫描条形码 默认为true
                    config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为白色
                    config.setFrameLineColor(R.color.colorAccent);//设置扫描框边框颜色 默认无色
                    config.setScanLineColor(R.color.colorAccent);//设置扫描线的颜色 默认白色
//                config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
                    config.setShowFlashLight(false);
                    config.setShowAlbum(false);
                    intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                    intent.putExtra("from","1");
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    public void requestSuccess(int urlId, String result) {
        super.requestSuccess(urlId, result);
        switch (urlId) {
            case 1: {
                Log.e("---", result);
                loading.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String code = jsonObject.getString("code");
                    if ("1".equals(code)) {
                        assessmentList.clear();
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            Map<String, String> nameMap = new HashMap<String, String>();
                            nameMap.put("userAssessmentId", jsonObject1.getString("id"));
                            JSONObject jsonObject2 = new JSONObject(jsonObject1.getString("village"));
                            nameMap.put("villageName", jsonObject2.getString("name"));
                            assessmentList.add(nameMap);
                        }
                        Map<String, String> addMyMap = new HashMap<String, String>();
                        addMyMap.put("userAssessmentId", "999999");
                        addMyMap.put("villageName", "选择其他小区");
                        assessmentList.add(addMyMap);
                        showListViewDialog();
                    } else if ("0".equals(code)) {
                        //选择小区
                        Intent intent = new Intent(MyMainActivity.this, SelectVillageActivity.class);
                        startActivity(intent);
                    } else {
                        requestFailure(1, jsonObject.getString("msg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    requestFailure(1, "数据解析异常");
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void requestFailure(int urlId, String result) {
        super.requestFailure(urlId, result);
        switch (urlId) {
            case 1: {
                Log.e("---", result);
                loading.dismiss();
                AppTools.toastShort(result);
                break;
            }
            default:
                break;
        }
    }

    private void initViewPager() {
        //viewPager = findViewById(R.id.main_view_pager);
        textView1 = findViewById(R.id.main_text1);
        textView2 = findViewById(R.id.main_text2);
        imageView1 = findViewById(R.id.main_img1);
        imageView2 = findViewById(R.id.main_img2);

        textView3 = findViewById(R.id.my_main_text_user);
        textView3.setText(UserBean.getUserBean().getName());

        assessmentList = new ArrayList<>();

        list = new ArrayList<>();

        //添加轮播点
        //final LinearLayout pointGroup =  (LinearLayout) MyMainActivity.this.findViewById(R.id.main_point_group);

//        for (int i = 0; i < list.size(); i++) {
//            // 制作底部小圆点
//            ImageView pointImage = new ImageView(MyMainActivity.this);
//            pointImage.setImageResource(R.drawable.shape_point_selector);
//
//            // 设置小圆点的布局参数
//            int PointSize = getResources().getDimensionPixelSize(R.dimen.point_size);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(PointSize, PointSize);
//
//            if (i > 0) {
//                params.leftMargin = getResources().getDimensionPixelSize(R.dimen.point_margin);
//                pointImage.setSelected(false);
//            } else {
//                pointImage.setSelected(true);
//            }
//            pointImage.setLayoutParams(params);
//            // 添加到容器里
//            pointGroup.addView(pointImage);
//        }

//        ViewpagerAdapter adapter = new ViewpagerAdapter(this,list);
//        viewPager.setAdapter(adapter);
//        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int i, float v, int i1) {
//
//            }
//
//            int lastPosition;
//            @Override
//            public void onPageSelected(int position) {
//                //伪无限循环，滑到最后一张图片又从新进入第一张图片
//                position = position % list.size();
//                // 设置当前页面选中
//                pointGroup.getChildAt(position).setSelected(true);
//                // 设置前一页不选中
//                pointGroup.getChildAt(lastPosition).setSelected(false);
//
//                // 把当前的索引赋值给前一个索引变量, 方便下一次再切换.
//                lastPosition = position;
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int i) {
//
//            }
//        });
//        if(list.size() == 1){
//            pointGroup.setVisibility(View.INVISIBLE);
//        }else{
//            pointGroup.setVisibility(View.VISIBLE);
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    int currentPosition = viewPager.getCurrentItem();
//
//                    if(currentPosition == viewPager.getAdapter().getCount() - 1){
//                        // 最后一页
//                        viewPager.setCurrentItem(0);
//                    }else{
//                        viewPager.setCurrentItem(currentPosition + 1);
//                    }
//                    // 一直给自己发消息
//                    mHandler.postDelayed(this,3000);
//                }
//            },3000);
//        }
    }

    private void showListViewDialog() {
        LayoutInflater factory = LayoutInflater.from(MyMainActivity.this);
        final View view = factory.inflate(R.layout.layout_listview, null);
        final ListView listView = (ListView) view.findViewById(R.id.layout_listview_list);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        SimpleAdapter adapter = new SimpleAdapter(MyMainActivity.this,
                assessmentList, R.layout.layout_listview_item,
                new String[]{"villageName"},
                new int[]{R.id.layout_listview_item_text});
        listView.setAdapter(adapter);

        dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击
        dialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//响应listview中的item的点击事件

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                dialog.cancel();
                if ("999999".equals(assessmentList.get(arg2).get("userAssessmentId"))) {
                    //创建新小区，跳转到选择小区页面
                    Intent intent = new Intent(MyMainActivity.this, SelectVillageActivity.class);
                    startActivity(intent);
                } else {
                    //把用户的考核id 放到userBean中

                    Intent intent = new Intent(MyMainActivity.this, AssessmentActivity.class);
                    intent.putExtra("userAssessmentId", assessmentList.get(arg2).get("userAssessmentId"));
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onCommitClick(View v) {
        super.onCommitClick(v);
        LayoutInflater factory = LayoutInflater.from(MyMainActivity.this);
        final View view = factory.inflate(R.layout.layout_dialog, null);
        final TextView textView = view.findViewById(R.id.layout_dialog_text);
        textView.setText("是否退出该账户");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //loading.show();
                        //loginOut();
                        MyApplication.setUserToken("");
                        UserBean.setNil();
                        startActivity(new Intent(MyMainActivity.this, LoginActivity.class));
                        MyMainActivity.this.finish();
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击
        dialog.show();
    }

    public void loginOut() {
        String url = AppURI.signOut;
        url += "?token=" + MyApplication.getUserToken();
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
                            if (code.equals("1")) {
                                MyApplication.setUserToken("");
                                MyMainActivity.this.finish();
                            } else {
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 3000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
