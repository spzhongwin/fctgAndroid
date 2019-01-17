package com.example.rubbishclassification.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.rubbishclassification.R;
import com.example.rubbishclassification.adapter.CommonListAdapter;
import com.example.rubbishclassification.pullrecyclerview.BaseRecyclerAdapter;
import com.example.rubbishclassification.pullrecyclerview.BaseViewHolder;
import com.example.rubbishclassification.pullrecyclerview.PullRecyclerView;
import com.example.rubbishclassification.pullrecyclerview.layoutmanager.XLinearLayoutManager;
import com.example.rubbishclassification.tools.AppTools;
import com.example.rubbishclassification.tools.AppURI;
import com.example.rubbishclassification.tools.OkHttp3Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class JiLuActivity extends BaseActivity {

    private PullRecyclerView mPullRecyclerView;
    private List<Map<String,String>> myList = new ArrayList<>();
    private CommonListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_lajidaifafangjilu);
        setTitleBackVisibity(true);
        setTitleText("垃圾袋发放记录");

        mPullRecyclerView = findViewById(R.id.layout_jilu_pull);
        mPullRecyclerView.setLayoutManager(new XLinearLayoutManager(this));
        mAdapter = new CommonListAdapter(this, R.layout.layout_lajidaifafangjilu_item, myList);
        mPullRecyclerView.setAdapter(mAdapter);
        // 设置下拉刷新的旋转圆圈的颜色（根据自己的需求设置）
        mPullRecyclerView.setColorSchemeResources(R.color.colorAccent);
        // 触发PullRecyclerView的下拉刷新，会展示下拉旋转圆圈
        mPullRecyclerView.postRefreshing();
        mPullRecyclerView.enableLoadMore(false);
        mPullRecyclerView.enableLoadDoneTip(true,R.string.tip);

        mPullRecyclerView.setOnRecyclerRefreshListener(new PullRecyclerView.OnRecyclerRefreshListener() {
            @Override
            public void onPullRefresh() {
                // 下拉刷新事件被触发
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Thread.sleep(1000);
                            getJiluList(System.currentTimeMillis()+"",true);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            }

            @Override
            public void onLoadMore() {
                // 上拉加载更多事件被触发
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Thread.sleep(1000);
                            getJiluList(myList.get(myList.size()-1).get("time"),false);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });

    }

    @Override
    public void onBackClick(View v) {
        super.onBackClick(v);
        JiLuActivity.this.finish();
    }

    // 物理返回键退出程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            JiLuActivity.this.finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }



    public void getJiluList(final String time, final boolean isRefresh){
        String url = AppURI.setDomainUrl(AppURI.getPropertySendList);
        url += "timeStamp="+time;
        OkHttp3Utils.doGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            String code = jsonObject.getString("code");
                            if (code.equals("1")){
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                if(isRefresh){
                                    myList.clear();
                                    mPullRecyclerView.stopRefresh();
                                }else{
                                    mPullRecyclerView.stopLoadMore();
                                }
                                if(jsonArray.length() == 0){
                                    mPullRecyclerView.setEmptyView(R.layout.layout_no_data);
                                } else if(jsonArray.length() < 20){
                                    mPullRecyclerView.enableLoadMore(false);
                                    mPullRecyclerView.enableLoadDoneTip(true,R.string.tip);
                                }else{
                                    mPullRecyclerView.enableLoadMore(true);
                                }
                                for (int i=0;i<jsonArray.length();i++) {
                                    JSONObject jsonObject1 = (JSONObject)jsonArray.get(i);
                                    Map<String,String> map = new HashMap<>();
                                    map.put("time",jsonObject1.getString("createTime"));
                                    map.put("fanjian",jsonObject1.getString("roomNumberText"));
                                    map.put("geshu",jsonObject1.getInt("bagNumber")+"");
                                    map.put("leixing",jsonObject1.getString("bagTypeString"));
                                    myList.add(map);
                                }
                                mAdapter.notifyDataSetChanged();
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
