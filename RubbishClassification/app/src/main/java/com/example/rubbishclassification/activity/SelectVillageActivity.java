package com.example.rubbishclassification.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


import com.example.rubbishclassification.R;
import com.example.rubbishclassification.adapter.AddressAdapter;
import com.example.rubbishclassification.adapter.ViewpagerAdapter;
import com.example.rubbishclassification.tools.AppTools;
import com.example.rubbishclassification.tools.AppURI;
import com.example.rubbishclassification.tools.OkHttp3Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SelectVillageActivity extends BaseActivity {
    //private ViewPager viewPager;
    private List<String> list;
    Handler mHandler = new Handler();

    private EditText editText;
    private ImageView imageView;

    private ListView listViewStreet;
    private ListView listViewCommunity;
    private ListView listViewVillage;

    private AddressAdapter myAdapterStreet;
    private AddressAdapter myAdapterCommunity;
    private AddressAdapter myAdapterVillage;

    private List<Map<String,String>> listStreet = new ArrayList();
    private List<Map<String,String>> listCommunity = new ArrayList();
    private List<Map<String,String>> listVillage = new ArrayList();

    private int curListStreetPosition = 0;
    private int curListCommunityPosition = 0;
    private int curListVillagePosition = 0;

    private String myStreetId = "";
    private String myCommunityId = "";
    private String myVillageId = "";
    private String myVillageName = "";

    private List<Map<String,String>> searchVillageList = new ArrayList<>();

    AlertDialog myDialog;

    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private View textView5;
    private TextView textView6;
    private View textView7;
    private TextView textView8;
    private View textView9;

    //判断是从那个页面过来的。用来处理选择完小区的跳转，
    private String from = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_village);
        setTitleBackVisibity(true);
        setTitleText("选择小区");
        initViewPager();

        from = getIntent().getStringExtra("from");

        //发起网络请求街道列表
        getStreetList();

        listViewStreet.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        myAdapterStreet = new AddressAdapter(SelectVillageActivity.this,listStreet);
        listViewStreet.setAdapter(myAdapterStreet);

        listViewStreet.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //每一项点击事件
                textView4.setSelected(true);
                textView5.setSelected(true);
                textView6.setSelected(false);
                textView7.setSelected(false);
                textView8.setSelected(false);
                textView9.setSelected(false);
                myStreetId = listStreet.get(position).get("streetId");
                textView1.setText(listStreet.get(position).get("name")+">");
                textView3.setVisibility(View.GONE);
                textView2.setVisibility(View.VISIBLE);
                textView2.setText("请选择社区");
                textView2.setTextColor(getResources().getColor(R.color.myColor));

                if(listStreet.size() != 0){
                    listStreet.get(curListStreetPosition).put("isSelected","0");
                    listStreet.get(position).put("isSelected","1");
                }
                curListStreetPosition = position;
                myAdapterStreet.notifyDataSetChanged();

                listCommunity.clear();
                curListCommunityPosition = 0;
                myAdapterCommunity.notifyDataSetChanged();
                listVillage.clear();
                curListVillagePosition = 0;
                myAdapterVillage.notifyDataSetChanged();
                getCommunityList(myStreetId);
            }
        });

        myAdapterCommunity = new AddressAdapter(SelectVillageActivity.this,listCommunity);
        listViewCommunity.setAdapter(myAdapterCommunity);
        listViewCommunity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listViewCommunity.getChildAt(position-listViewCommunity.getFirstVisiblePosition()).setSelected(true);
                //每一项点击事件
                textView4.setSelected(false);
                textView5.setSelected(false);
                textView6.setSelected(true);
                textView7.setSelected(true);
                textView8.setSelected(false);
                textView9.setSelected(false);
                myCommunityId = listCommunity.get(position).get("communityId");
                textView2.setText( listCommunity.get(position).get("name")+">");
                textView2.setTextColor(getResources().getColor(R.color.black));
                textView3.setVisibility(View.VISIBLE);
                textView3.setText("请选择小区");
                textView3.setTextColor(getResources().getColor(R.color.myColor));

                if(listCommunity.size() != 0){
                    listCommunity.get(curListCommunityPosition).put("isSelected","0");
                    listCommunity.get(position).put("isSelected","1");
                }
                curListCommunityPosition = position;
                myAdapterCommunity.notifyDataSetChanged();

                listVillage.clear();
                myAdapterVillage.notifyDataSetChanged();
                getVillageList(myStreetId,myCommunityId);
            }
        });

        listViewVillage.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        myAdapterVillage = new AddressAdapter(SelectVillageActivity.this,listVillage);

        listViewVillage.setAdapter(myAdapterVillage);
        listViewVillage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listViewVillage.getChildAt(position-listViewVillage.getFirstVisiblePosition()).setSelected(true);
                //每一项点击事件 保存 streetId  communityId villageId
                textView4.setSelected(false);
                textView5.setSelected(false);
                textView6.setSelected(false);
                textView7.setSelected(false);
                textView8.setSelected(true);
                textView9.setSelected(true);
                myVillageId = listVillage.get(position).get("villageId");
                myVillageName =  listVillage.get(position).get("name");
                textView3.setText(listVillage.get(position).get("name"));
                textView3.setTextColor(getResources().getColor(R.color.black));

                if(listVillage.size() != 0){
                    listVillage.get(curListVillagePosition).put("isSelected","0");
                    listVillage.get(position).put("isSelected","1");
                }
                curListVillagePosition = position;
                myAdapterVillage.notifyDataSetChanged();

                if(myDialog == null || !myDialog.isShowing()){
                    showDialog();
                }

            }
        });

        editText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,KeyEvent event)  {
                if (actionId==EditorInfo.IME_ACTION_SEARCH ||(event!=null&&event.getKeyCode()== KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager) SelectVillageActivity.this. getSystemService(SelectVillageActivity.this.INPUT_METHOD_SERVICE);
                    if (imm != null && editText != null){
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0); //强制隐藏键盘
                    }
                    searchVillage();
                    return true;
                }
                return false;
            }
        });

    }

    private void getStreetList(){
        OkHttp3Utils.doGet(AppURI.getStreets, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AppTools.toastShort("请求异常");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            JSONObject jsonObject = new JSONObject(result);
                            String code = jsonObject.getString("code");
                            if("1".equals(code)){
                                listStreet.clear();
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                for(int i=0;i < jsonArray.length();i++){
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    Map<String, String> nameMap = new HashMap<String, String>();
                                    nameMap.put("streetId",jsonObject1.getString("id"));
                                    nameMap.put("name",jsonObject1.getString("name"));
                                    nameMap.put("isSelected","0");
                                    listStreet.add(nameMap);
                                }
                                myAdapterStreet.notifyDataSetChanged();
                                if(listStreet.size()!=0){
                                    textView1.setText(listStreet.get(0).get("name")+">");
                                    textView2.setVisibility(View.VISIBLE);
                                    textView4.setSelected(true);
                                    textView5.setSelected(true);
                                }
                            }else{
                                AppTools.toastShort(jsonObject.getString("msg"));
                            }
                        }catch (Exception e){
                            AppTools.toastShort("解析异常");
                        }
                    }
                });
            }
        });
    }

    private void getCommunityList(String streetId){
        OkHttp3Utils.doGet(AppURI.getCommunitys+"?streetId="+streetId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AppTools.toastShort("请求异常");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            JSONObject jsonObject = new JSONObject(result);
                            String code = jsonObject.getString("code");
                            if("1".equals(code)){
                                listCommunity.clear();
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                for(int i=0;i < jsonArray.length();i++){
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    Map<String, String> nameMap = new HashMap<String, String>();
                                    nameMap.put("communityId",jsonObject1.getString("id"));
                                    nameMap.put("name",jsonObject1.getString("name"));
                                    nameMap.put("isSelected","0");
                                    listCommunity.add(nameMap);
                                }
                                myAdapterCommunity.notifyDataSetChanged();
                            }else{
                                AppTools.toastShort(jsonObject.getString("msg"));
                            }
                        }catch (Exception e){
                            AppTools.toastShort("解析异常");
                        }
                    }
                });
            }
        });
    }

    private void getVillageList(String streetId,String communityId){
        OkHttp3Utils.doGet(AppURI.getVillages+"?streetId="+streetId+"&communityId="+communityId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AppTools.toastShort("请求异常");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            JSONObject jsonObject = new JSONObject(result);
                            String code = jsonObject.getString("code");
                            if("1".equals(code)){
                                listVillage.clear();
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                for(int i=0;i < jsonArray.length();i++){
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    Map<String, String> nameMap = new HashMap<String, String>();
                                    nameMap.put("villageId",jsonObject1.getString("id"));
                                    nameMap.put("name",jsonObject1.getString("name"));
                                    nameMap.put("isSelected","0");
                                    listVillage.add(nameMap);
                                }
                                myAdapterVillage.notifyDataSetChanged();
                            }else{
                                AppTools.toastShort(jsonObject.getString("msg"));
                            }
                        }catch (Exception e){
                            AppTools.toastShort("解析异常");
                        }
                    }
                });
            }
        });
    }

    private void searchVillage(){
        String name = editText.getText().toString();
        if(TextUtils.isEmpty(name)){
            AppTools.toastShort("请输入要搜索的小区");
            return;
        }
        loading.show();
        try {
            name = URLEncoder.encode(name,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        OkHttp3Utils.doGet(AppURI.searchVillage+"?name="+name, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loading.dismiss();
                        AppTools.toastShort("请求异常");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loading.dismiss();
                        try{
                            JSONObject jsonObject = new JSONObject(result);
                            String code = jsonObject.getString("code");
                            if("1".equals(code)){
                                searchVillageList.clear();
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                for(int i=0;i < jsonArray.length();i++){
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    Map<String, String> nameMap = new HashMap<String, String>();
                                    nameMap.put("streetId",jsonObject1.getString("streetId"));
                                    nameMap.put("communityId",jsonObject1.getString("communityId"));
                                    nameMap.put("villageId",jsonObject1.getString("id"));
                                    nameMap.put("villageName",jsonObject1.getString("name"));
                                    searchVillageList.add(nameMap);
                                }
                                if(searchVillageList.size() == 0){
                                    AppTools.toastShort("未搜索到该小区");
                                }else{
                                    showListViewDialog();
                                }

                            }else{
                                AppTools.toastShort(jsonObject.getString("msg"));
                            }
                        }catch (Exception e){
                            AppTools.toastShort("解析异常");
                        }
                    }
                });
            }
        });
    }

    private void createAssessment(){
        String url = AppURI.setDomainUrl(AppURI.createAssessment);
        url+= "streetId="+myStreetId;
        url+= "&communityId="+myCommunityId;
        url+= "&villageId="+myVillageId;
        Log.e("---url---",url);
        OkHttp3Utils.doGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AppTools.toastShort("请求异常");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            JSONObject jsonObject = new JSONObject(result);
                            String code = jsonObject.getString("code");
                            if("1".equals(code)){
                                searchVillageList.clear();
                                Intent intent = new Intent(SelectVillageActivity.this, AssessmentActivity.class);
                                intent.putExtra("userAssessmentId",jsonObject.getString("data"));
                                myDialog.cancel();
                                startActivity(intent);
                                SelectVillageActivity.this.finish();
                            }else{
                                AppTools.toastShort(jsonObject.getString("msg"));
                            }
                        }catch (Exception e){
                            AppTools.toastShort("解析异常");
                        }
                    }
                });
            }
        });
    }

    private void showListViewDialog(){
        LayoutInflater factory = LayoutInflater.from(SelectVillageActivity.this);
        final View view = factory.inflate(R.layout.layout_listview_search, null);
        final ListView listView= (ListView) view.findViewById(R.id.layout_listview_search_list);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        SimpleAdapter adapter = new SimpleAdapter(SelectVillageActivity.this,
                searchVillageList, R.layout.layout_listview_item,
                new String[] { "villageName" },
                new int[] { R.id.layout_listview_item_text});
        listView.setAdapter(adapter);

        final AlertDialog dialog = new AlertDialog.Builder(this)
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
                myStreetId = searchVillageList.get(arg2).get("streetId");
                myCommunityId = searchVillageList.get(arg2).get("communityId");
                myVillageId = searchVillageList.get(arg2).get("villageId");
                myVillageName = searchVillageList.get(arg2).get("villageName");
                dialog.cancel();
                if(myDialog == null || !myDialog.isShowing()){
                    showDialog();
                }

            }
        });
    }

    private void showDialog(){
        String message = "";
        if("1".equals(from)){
            message = "是否确认选择该小区进行督导";
        }else{
            message = "是否确认选择该小区进行评分";
        }
        LayoutInflater factory = LayoutInflater.from(SelectVillageActivity.this);
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
                        if("1".equals(from)){
                            Intent intent = new Intent();
                            intent.putExtra("streetId",myStreetId);
                            intent.putExtra("communityId",myCommunityId);
                            intent.putExtra("villageId",myVillageId);
                            intent.putExtra("villageName",myVillageName);
                            setResult(RESULT_OK,intent);
                            SelectVillageActivity.this.finish();
                        }else{
                            createAssessment();
                        }

                    }
                }).create();
        myDialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击
        myDialog.show();
    }

    private void initViewPager(){
        //viewPager = findViewById(R.id.village_view_pager);

        editText = findViewById(R.id.village_edit);
        imageView = findViewById(R.id.village_search);
        listViewStreet = findViewById(R.id.village_list1);
        listViewCommunity = findViewById(R.id.village_list2);
        listViewVillage = findViewById(R.id.village_list3);

        textView1 = findViewById(R.id.village_text_ccc1);
        textView2 = findViewById(R.id.village_text_ccc2);
        textView3 = findViewById(R.id.village_text_ccc3);
        textView4 = findViewById(R.id.village_text_street);
        textView5 = findViewById(R.id.village_text_street_view);
        textView6 = findViewById(R.id.village_text_community);
        textView7 = findViewById(R.id.village_text_community_view);
        textView8 = findViewById(R.id.village_text_village);
        textView9 = findViewById(R.id.village_text_village_view);



        list = new ArrayList<>();

        //添加轮播点
        //final LinearLayout pointGroup =  (LinearLayout) SelectVillageActivity.this.findViewById(R.id.village_point_group);

//        for (int i = 0; i < list.size(); i++) {
//            // 制作底部小圆点
//            ImageView pointImage = new ImageView(SelectVillageActivity.this);
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
//
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

    @Override
    public void onBackClick(View v) {
        super.onBackClick(v);
        SelectVillageActivity.this.finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
