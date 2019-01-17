package com.example.rubbishclassification.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.rubbishclassification.R;
import com.example.rubbishclassification.pullrecyclerview.BaseRecyclerAdapter;
import com.example.rubbishclassification.pullrecyclerview.BaseViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CommonListAdapter  extends BaseRecyclerAdapter {

    public CommonListAdapter(Context context, int layoutResId, List<Map<String,String>> data) {
        super(context, layoutResId, data);
    }

    @Override
    protected void converted(BaseViewHolder holder, Object item, int position) {
        Map<String,String> data = (Map<String, String>) item;
        View view = holder.getView(R.id.jilu_view);
        if(position == 0){
            view.setVisibility(View.INVISIBLE);
        }else{
            view.setVisibility(View.VISIBLE);
        }
        holder.setText(R.id.jilu_time, stampToDate(data.get("time")));
        holder.setText(R.id.jilu_fangjian, data.get("fanjian"));
        holder.setText(R.id.jilu_geshu, data.get("geshu"));
        holder.setText(R.id.jilu_leixing, data.get("leixing"));
    }
    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }

}