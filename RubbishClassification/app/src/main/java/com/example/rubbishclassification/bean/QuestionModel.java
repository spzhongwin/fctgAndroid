package com.example.rubbishclassification.bean;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class QuestionModel  implements Comparable<QuestionModel>{

    public int id;              //问题的ID
    public String oneLevelName; //一级指标的名字
    public String shortName;    //二级指标的简称
    public String info;         //二级指标的详细描述
    public int fraction;        //当前问题的分数
    public String req;          //当前问题的序列
    public int assessmentType;   // 0是基本指标（默认的，是减分项目），1是鼓励指标（加分项）
    public ArrayList<HashMap> answerJson;//答案列表
    //回答的问题
    public boolean isAnswer;                 //该问题是否已经回答过了
    public int assessmentFraction=0;         //分拣员做的答案信息-考核的分数
    public String assessmentInfo="";         //分拣员做的答案信息-考核的备注描述
    public ArrayList<String> imgs =  new ArrayList<>();    //分拣员做的答案信息-图片列表
    // 业务属性
    public boolean isEdit = false;
    public boolean isSelected = false;                 //该问题是否被选中


    //构造函数
    public QuestionModel(JSONObject oneJson){
        this.id = StringToInt(analysisJson(oneJson,"id"));
        this.oneLevelName = analysisJson(oneJson,"oneLevelName");
        this.shortName = analysisJson(oneJson,"shortName");
        this.info = analysisJson(oneJson,"info");
        this.fraction = StringToInt(analysisJson(oneJson,"fraction"));
        this.req = analysisJson(oneJson,"req");
        this.assessmentType = StringToInt(analysisJson(oneJson,"assessmentType"));
        this.isAnswer = analysisJsonBoolean(oneJson,"isAnswer");
        this.answerJson = new ArrayList<>();
        //问题答案
        try {
            JSONArray answerJsonList = oneJson.getJSONArray("answerJson");
            for (int i = 0; i< answerJsonList.length(); i++){
                JSONObject theOneJson = answerJsonList.getJSONObject(i);
                HashMap map = new HashMap();
                map.put("des",theOneJson.getString("des"));
                this.answerJson.add(map);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //回答过的问题
        try {
            JSONObject answerJson = oneJson.getJSONObject("answerInfo");
            this.assessmentFraction = StringToInt(analysisJson(answerJson,"fraction"));
            this.assessmentInfo = analysisJson(answerJson,"info");
            try{
                JSONArray arrayImg = answerJson.getJSONArray("imgs");
                for (int i = 0; i< arrayImg.length(); i++){
                    JSONObject imgOneJson = arrayImg.getJSONObject(i);
                    //HashMap map = new HashMap();
                    //map.put("url",imgOneJson.getString("url"));
                    this.imgs.add(imgOneJson.getString("url"));
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
            //this.isAnswer = true;
        } catch (JSONException e) {
            e.printStackTrace();
            //this.isAnswer = false;
        }
    }

    private int StringToInt(String string){
        if(!TextUtils.isEmpty(string)){
            return Integer.parseInt(string);
        }
        return 0;
    }

    //解析json
    private String analysisJson(JSONObject json, String key){
        try {
            return json.getString(key);
        } catch (JSONException e) {
            try {
                return ""+json.getInt(key);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return "";
    }

    private Boolean analysisJsonBoolean(JSONObject jsonObject,String key){
        try {
            return jsonObject.getBoolean(key);
        }catch (JSONException e2){
            e2.printStackTrace();
        }
        return false;
    }

    @Override
    public int compareTo(@NonNull QuestionModel questionModel) {
        int i = StringToInt(this.req)-StringToInt(questionModel.req);
        return i;
    }
}
