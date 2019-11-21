package com.mmall.common;

public class Const {
    public static final String CURRENT_USER = "current_User";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    //通过内部接口类将const分组（普通用户和管理员）--功能类似于枚举，但是又不像枚举这么繁琐
    public interface Role{
        int ROLE_CUSTOMER = 0; //customer
        int ROLE_ADMIN = 1; //Admin
    }
}
