package com.example.rubbishclassification.bean;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class AssessmentModel {

    public int id;              //此次考核的ID
    public int totalFraction;   //考核的总得分
    public String createTime;   //创建时间
    public int state;           //考核的状态，(0是进行中，1是审核中，2是审核打回，3是审核通过，-1是删除)
    public int villageId;       //小区的ID
    public String villageName;     //小区的名字
    public ArrayList<QuestionModel> questionList;//考核的问题及答案
    public QuestionModel curQuestionModel; //当前选中的问题，默认是从第一个开始
    // 构造函数
    public AssessmentModel(JSONObject responseJson) throws JSONException {
        this.questionList = new ArrayList<QuestionModel>();
        this.id = StringToInt(analysisJson(responseJson,"id"));
        this.totalFraction = StringToInt(analysisJson(responseJson,"totalFraction"));
        this.createTime = analysisJson(responseJson,"createTime");
        this.state = StringToInt(analysisJson(responseJson,"state"));
        this.villageId = StringToInt(analysisJson(responseJson.getJSONObject("village"),"id"));
        this.villageName = analysisJson(responseJson.getJSONObject("village"),"name");
        JSONArray questionJsonList = responseJson.getJSONArray("questionList");
        for (int i = 0; i < questionJsonList.length(); i++) {
            JSONObject oneJson = questionJsonList.getJSONObject(i);
            QuestionModel anserModel = new QuestionModel(oneJson);
            this.questionList.add(anserModel);
        }
        Collections.sort(this.questionList);
        //设置当前的问题
        setCurQuestionModel(0);
    }

    //设置默认选中的第一个
    //设置默认选中的第一个
    public void setCurQuestionModel(int questionId){
        if (this.questionList.size() == 0)
            return;
        if (questionId == 0){
            for (QuestionModel infoModel : this.questionList) {
                if (infoModel.isAnswer == false) {
                    this.curQuestionModel = infoModel;
                    break;
                }
            }
        }else {
            for (QuestionModel infoModel : this.questionList) {
                if (infoModel.id == questionId){
                    this.curQuestionModel = infoModel;
                    break;
                }
            }
        }
        // 如果没有找到，就默认选中当前的一个
        if (this.curQuestionModel == null){
            this.curQuestionModel = this.questionList.get(0);
        }
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

    private int StringToInt(String string){
        if(!TextUtils.isEmpty(string)){
            return Integer.parseInt(string);
        }
        return 0;
    }
}