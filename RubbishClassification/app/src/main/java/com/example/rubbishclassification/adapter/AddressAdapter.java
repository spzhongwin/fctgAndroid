package com.example.rubbishclassification.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.rubbishclassification.R;

import java.util.List;
import java.util.Map;

public class AddressAdapter extends BaseAdapter {
    private Context myContext;
    private LayoutInflater layoutInflater;
    private List<Map<String,String>> myQuestionModelList;
    public AddressAdapter(Context context, List<Map<String,String>> questionModel) {
        this.myContext = context;
        this.myQuestionModelList = questionModel;
    }

    @Override
    public int getCount() {
        return myQuestionModelList.size();
    }

    @Override
    public Object getItem(int position) {
        return myQuestionModelList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            layoutInflater = LayoutInflater.from(myContext);
            convertView = layoutInflater.inflate(R.layout.layout_village_item_left,null);
            viewHolder.reqTestView = (TextView) convertView.findViewById(R.id.layout_village_item_left_text) ;
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Map<String,String> map =  myQuestionModelList.get(position);
        viewHolder.reqTestView.setText(map.get("name"));
        if(map.get("isSelected").equals("0")){
            viewHolder.reqTestView.setTextColor(myContext.getResources().getColor(R.color.black));
        }else{
            viewHolder.reqTestView.setTextColor(myContext.getResources().getColor(R.color.myColor));
        }
        return convertView;
    }

    class ViewHolder{
        //题目编号
        public TextView reqTestView;
    }

}
