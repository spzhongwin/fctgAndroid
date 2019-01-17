package com.example.rubbishclassification.bean;

import android.text.TextUtils;
import android.util.Log;

import com.example.rubbishclassification.activity.LoginActivity;
import com.example.rubbishclassification.tools.AppURI;
import com.example.rubbishclassification.tools.OkHttp3Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;



/**
 * Created by spzhong on 2019/1/5.
 * 考核操作类，
 */
public class AssessmentOperate {

    public static AssessmentOperate assessQuestion = null;

    public AssessmentModel model;
    public void setModel(AssessmentModel model){
        this.model = model;
    }
    public AssessmentModel getModel(){
        return this.model;
    }

    /**
     * 获取考核操作类实列
     * */
    public static AssessmentOperate getInstance() {
       if (assessQuestion == null){
           assessQuestion = new AssessmentOperate();
       }
       return assessQuestion;
    }

    public static void clearAssessmentQuestion() {
        assessQuestion.model = null;
        assessQuestion = null;
    }


    //获取考核的问题
    public void getAssessmentQuestion(String assessQuestionId, final AssessmentQuestionCallBack callBack){
        String url = AppURI.setDomainUrl(AppURI.getAssessmentQuestion);
        url += "userAssessmentId="+assessQuestionId;
        OkHttp3Utils.doGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.errorInfo("系统网络异常");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //对象封装
                String result = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String code = jsonObject.getString("code");
                    if (code.equals("1")){
                        AssessmentModel model = new AssessmentModel(jsonObject.getJSONObject("data"));
                        setModel(model);
                        callBack.getAssQuestionModel(model,curQuestionModel());
                    }else{
                        callBack.errorInfo(jsonObject.getString("msg"));
                    }
                } catch (JSONException e) {
                    callBack.errorInfo("解析异常");
                    e.printStackTrace();
                }
            }
        });
    }


    //切换问题的同时，调用保存的接口
    public void upAssessmentQuestion(final int nextQuestionId, final AssessmentQuestionCallBack callBack) {
        String url = AppURI.setDomainUrl(AppURI.upAssessmentQuestion);
        url += "userAssessmentId=" + this.model.id;
        //获取当前选中的问题
        url += "&questionId=" + this.curQuestionModel().id;
        //已经对应问题的答案
        url += "&fraction=" + this.curQuestionModel().assessmentFraction;
        try {
            url += "&info=" + URLEncoder.encode(this.curQuestionModel().assessmentInfo,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if(this.curQuestionModel().imgs.size() != 0){
            url += "&jsonImgs=[";
            for (String map : this.curQuestionModel().imgs) {
                url += "{\"url\":" + "\""+map+"\"";
                url += "},";
            }
            url = url.substring(0,url.length()-1);
            url += "]";
        }
        Log.e("--考核URL--",url);
        OkHttp3Utils.doGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.errorInfo("系统网络异常");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //对象封装
                String result = response.body().string();
                Log.e("--考核结果--",result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String code = jsonObject.getString("code");
                    if (code.equals("1")) {
                        AssessmentOperate.getInstance().curQuestionModel().isEdit = false;
                        AssessmentOperate.getInstance().curQuestionModel().isAnswer = true;
                        AssessmentOperate.getInstance().getModel().totalFraction = StringToInt(jsonObject.getString("data"));
                        setCurQuestionModel(nextQuestionId);
                        callBack.getAssQuestionModel(model, curQuestionModel());
                    } else {
                        callBack.errorInfo(jsonObject.getString("msg"));
                    }
                } catch (JSONException e) {
                    callBack.errorInfo("解析异常");
                    e.printStackTrace();
                }
            }
        });
    }

    public void setCurQuestionModel(int id){
        for (QuestionModel curquestion: this.model.questionList) {
            if(id == curquestion.id){
                this.model.curQuestionModel = curquestion;
                break;
            }
        }
    }

    //获取当前问题的info
    public QuestionModel curQuestionModel(){
        return this.model.curQuestionModel;
    }

    //定义接口
    public interface AssessmentQuestionCallBack{
        public void getAssQuestionModel(AssessmentModel model, QuestionModel curQuestionModel);
        public void errorInfo(String errorInfo);
    }

    private int StringToInt(String string){
        if(!TextUtils.isEmpty(string)){
            return Integer.parseInt(string);
        }
        return 0;
    }

}


