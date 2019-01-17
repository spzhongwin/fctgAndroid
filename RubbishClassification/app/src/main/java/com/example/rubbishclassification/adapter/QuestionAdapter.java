package com.example.rubbishclassification.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.rubbishclassification.R;
import com.example.rubbishclassification.bean.QuestionModel;

import java.util.ArrayList;
import java.util.List;

public class QuestionAdapter extends BaseAdapter {
    private Context myContext;
    private LayoutInflater layoutInflater;
    private ArrayList<QuestionModel> myQuestionModelList;
    public QuestionAdapter(Context context, ArrayList<QuestionModel> questionModel) {
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
            convertView = layoutInflater.inflate(R.layout.grid_item,null);
            viewHolder.reqTestView = (TextView) convertView.findViewById(R.id.grid_item_text) ;
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        QuestionModel questionModel = myQuestionModelList.get(position);
        viewHolder.reqTestView.setText("第"+questionModel.req+"题");
        if(questionModel.isSelected){
            viewHolder.reqTestView.setBackgroundResource(R.drawable.button_question_jinxingzhong);
            viewHolder.reqTestView.setTextColor(ContextCompat.getColor(myContext, R.color.white));
        }else if(questionModel.isAnswer){
            viewHolder.reqTestView.setBackgroundResource(R.drawable.button_question_yida);
            viewHolder.reqTestView.setTextColor(ContextCompat.getColor(myContext, R.color.white));
        }else{
            viewHolder.reqTestView.setBackgroundResource(R.drawable.button_question_weida);
            viewHolder.reqTestView.setTextColor(ContextCompat.getColor(myContext, R.color.black));
        }
        return convertView;
    }

    class ViewHolder{
        //题目编号
        public TextView reqTestView;
    }

}
