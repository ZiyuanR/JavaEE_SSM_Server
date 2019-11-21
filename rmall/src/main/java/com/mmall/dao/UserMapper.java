package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    //用户名是否已存在
    int checkUserName(String username);
    //用户名和密码是否正确
    //多个参数需要使用@param，而且mapper中#{}中的名字和param中的保持一致
    User selectLogin(@Param("username") String username, @Param("password")String password);
    //注册--email是否已存在
    int checkEmail(String email);
    //获取找回密码提示问题
    String selectQuestionByUsername(String username);
    //获取找回密码提示问题的答案
    int checkAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);
    //忘记密码时重置密码
    int updatePasswordByUsername(@Param("username") String username, @Param("passwordNew") String passwordNew);
    //登陆状态时重置密码
    //检查旧密码是否存在
    int checkPassword(@Param("userId") Integer userId, @Param("password") String password);
    //检查邮箱是否存在
    int checkEmailByUserId(@Param("email") String email, @Param("userId") Integer userId);
    //取得用户信息
//    User selectUserInfo(String username);
}