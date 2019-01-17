package com.example.rubbishclassification.bean;

import java.io.Serializable;

public class UserBean implements Serializable {
    private String id;
    private String name;
    private String token;
    private String role;
    private String phone;
    private String villageInfoName;

    public String getVillageInfoName() {
        return villageInfoName;
    }

    public void setVillageInfoName(String villageInfoName) {
        this.villageInfoName = villageInfoName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public static UserBean userBeanObj = null;
    public static UserBean setUserBean(UserBean userBean){
        if (userBeanObj == null){
            userBeanObj = userBean;
        }
        return userBeanObj;
    }
    public static UserBean getUserBean() {
        return userBeanObj;
    }
    public static void setNil(){
        userBeanObj = null;
    }
}