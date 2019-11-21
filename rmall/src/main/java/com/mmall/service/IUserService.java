package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

public interface IUserService {
    /**
     1.用户登陆userlogin
     */
    ServerResponse<User> login(String username, String password);
    /**
     2.用户注册userregister
     */
    ServerResponse<String> register(User user);
    /**
     3.校验用户名和密码是否已存在（在前台username input之后，实施调用此接口）
     validate whether username and password are exist
     */
    ServerResponse<String> checkValid(String str, String type);
    /**
     4.忘记密码，提示密码提示问题
     The question of finding back password
     */
    ServerResponse<String> selectQuestion(String username);
    /**
     5.校验问题答案是否正确
     validate whether answer of question is right
     */
    ServerResponse<String> checkAnswer(String username, String question, String answer);
    /**
     6.忘记密码+重置密码
     ForgetPassword + Reset password
     */
    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);
    /**
     7.登陆状态+重置密码
     Login + Reset password
     */
    ServerResponse<String> loginResetPassword(String passwordOld, String passwordNew, User user);
    /**
     8.更新个人用户信息
     Update User Information
     */
    ServerResponse<User> updateUserInfo(User user);
    /**
     9.获取用户详细信息,如果没有登陆会强制登陆
     Receive User's specific Information
     */
    ServerResponse<User> getInformation(Integer userId);

}
