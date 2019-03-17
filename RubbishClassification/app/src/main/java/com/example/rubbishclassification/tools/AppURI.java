package com.example.rubbishclassification.tools;

import com.example.rubbishclassification.bean.UserBean;

public class AppURI {
    //域名  //http://112.74.187.228
    public static String domain = "http://112.74.187.228";
    //evn  生产环境-ftcg;测试环境-testftcg
    public static String evn = "/testftcg";

    /**
     * 登录接口
     * */
    public static String login = domain+evn+"/user/sign";

    /**
     * 自动登录接口
     * */
    public static String autoSign = domain+evn+"/user/autoSign";

    /**
     * 考核-查询进行中
     * */
    public static String getIsHaveAssessment = domain+evn+"/assessment/getIsHaveAssessment";

    /**
     * 街道列表获取
     * */
    public static String getStreets = domain+evn+"/config/getStreets";

    /**
     * 社区列表获取
     * */
    public static String getCommunitys = domain+evn+"/config/getCommunitys";

    /**
     * 小区列表获取
     * */
    public static String getVillages = domain+evn+"/config/getVillages";

    /**
     * 小区搜索
     * */
    public static String searchVillage = domain+evn+"/config/searchVillage";

    /**
     * 考核创建
     * */
    public static String createAssessment = domain+evn+"/assessment/createAssessment";

    /**
     * 考核-获取考核的问题
     * */
    public static String getAssessmentQuestion = domain+evn+"/assessment/getAssessmentQuestion";
    /**
     * 考核-提交考核的问题
     * */
    public static String upAssessmentQuestion = domain+evn+"/assessment/upAssessmentQuestion";
    /**
     * 考核-考核的问题完成
     * */
    public static String finshAssessment = domain+evn+"/assessment/finshAssessment";

    /**
     *  登出
     * */
    public static String signOut = domain+evn+"/user/signOut";

    /**
     * 分拣
     * */
    public static String upSorting = domain+evn+"/sorting/upSorting";

    /**
     * 督察，扫描结果查询小区信息
     * */
    public static String sweepcCodeSorting = domain+evn+"/sorting/sweepcCodeSorting";
    /**
     * 物业人员发放袋子
     * */
    public static String propertySendBags = domain+evn+"/config/propertySendBags";

    /**
     * 物业人员发放记录
     * */
    public static String getPropertySendList = domain+evn+"/config/getPropertySendList";

    public static String setDomainUrl(String url){
        url += "?token="+ UserBean.getUserBean().getToken()+"&userId="+UserBean.getUserBean().getId()+"&";
        return url;
    }

}
