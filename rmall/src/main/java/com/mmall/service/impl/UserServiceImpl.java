package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    /**
     1.用户登陆userlogin
     */
    @Override
    public ServerResponse<User> login(String username, String password){
        //用户是否存在
        int resultCount = userMapper.checkUserName(username);
        if(resultCount==0) {
            return ServerResponse.createByErrorMessage("User is not exist.");
        }
        //todo 密码登陆MD5
        //用户名和密码是否正确
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        //比较的是加密后的password(db中存储的是加密后的password)
        User user = userMapper.selectLogin(username, md5Password);
        if(user == null){ //密码错误
            return ServerResponse.createByErrorMessage("Password is wrong.");

        }
        //将返回值的密码设置为空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("Login success!", user);

    }

    /**
     2.用户注册userregister
     */
    @Override
    public ServerResponse<String> register(User user){
//        //用户名是否已存在
//        int resultCount = userMapper.checkUserName(user.getUsername());
//        if(resultCount>0) {
//            return ServerResponse.createByErrorMessage("User has already existed.");
//        }
//        //邮箱是否已存在
//        resultCount = userMapper.checkEmail(user.getEmail());
//        if(resultCount>0){
//            return ServerResponse.createByErrorMessage("Email has already existed.");
//        }
        ServerResponse validResopnse = this.checkValid(user.getUsername(), Const.USERNAME);
        if(!validResopnse.isSuccess()){
            return validResopnse;
        }
        validResopnse = this.checkValid(user.getEmail(), Const.EMAIL);
        if(!validResopnse.isSuccess()){
            return validResopnse;
        }
        //分为普通用户和管理员
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("Registeration faliled.");
        }
        return ServerResponse.createBySuccessMessage("Registeration success!");
    }

    /**
     3.校验用户名和密码是否已存在（在前台username input之后，实施调用此接口）
     validate whether username and password are exist
     */
    @Override
    public ServerResponse<String> checkValid(String str, String type){
        if(StringUtils.isNotBlank(type)){
            //传进来的type不为空或者不是只有空格,开始校验
            if(Const.USERNAME.equals(type)){
                //用户名是否已存在
                int resultCount = userMapper.checkUserName(str);
                if(resultCount>0) {
                    return ServerResponse.createByErrorMessage("User has already existed.");
                }
            }
            if(Const.EMAIL.equals(type)){
                //邮箱是否已存在
                int resultCount = userMapper.checkEmail(str);
                if(resultCount>0){
                    return ServerResponse.createByErrorMessage("Email has already existed.");
                }
            }

        }else{
            ServerResponse.createByErrorMessage("Parameter is wrong.");
        }
        return ServerResponse.createBySuccessMessage("Validation Success.");
    }

    /**
     4.忘记密码，提示密码提示问题
     The question of finding back password
     */
    @Override
    public ServerResponse<String> selectQuestion(String username){
        ServerResponse checkValidResponse = this.checkValid(username, Const.USERNAME);
        if(checkValidResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("Username is not exist, please register firstly.");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("The question of finding back password is empty!");
    }

    /**
     5.校验问题答案是否正确
     validate whether answer of question is right
     */
    //将token放入string中
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer){
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if(resultCount>0){
            //说明问题及问题的答案是这个用户的，并且是正确的
            String forgetToken = UUID.randomUUID().toString();
            //将forgetToken放入本地cache中并设置有效期
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("Answer is wrong.");
    }

    /**
     6.忘记密码+重置密码
     ForgetPassword + Reset password
     */
    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("Token is empty");
        }
        //若token为空，依然可以拿到key=token_，因此username不应为空
        ServerResponse validResopnse = this.checkValid(username, Const.USERNAME);
        if(validResopnse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("Username is not exist");
        }
        //从cache中获取token
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        //对cache中的token进行校验
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token expired. 无效或者过期");
        }
        if(StringUtils.equals(forgetToken,token)){
            String md5Password  = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,md5Password);

            if(rowCount > 0){
                return ServerResponse.createBySuccessMessage("Reset password successfully.修改密码成功");
            }
        }else{
            return ServerResponse.createByErrorMessage("token错误,请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("Reset password falied. 修改密码失败");
    }

    /**
     7.登陆状态+重置密码
     Login + Reset password
     */
    @Override
    public ServerResponse<String> loginResetPassword(String passwordOld, String passwordNew, User user){
        //放置横向越权:校验这个用户的旧密码一定要是这个用户的
        //因为会查询一个count（1），如果不指定id，那么返回结果会是true--count>0
        int resultCount = userMapper.checkPassword(user.getId(), MD5Util.MD5EncodeUtf8(passwordOld));
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("Old password is wrong. 旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0){
            return ServerResponse.createBySuccessMessage("Reset password successfully. 密码更新成功");
        }
        return ServerResponse.createByErrorMessage("Reset password failed. 密码更新失败");
    }

    /**
     8.更新个人用户信息
     Update User Information
     */
    @Override
    public ServerResponse<User> updateUserInfo(User user){
        //username不能被更新
        //email需要被校验：新的email是不是已经存在
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if(resultCount>0){
            //email已被其他用户使用过
            return ServerResponse.createByErrorMessage("This email has been used by other users.此邮箱已被其他用户使用过");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        //updateTime已在数据库中的now（）自动更新成功
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount>0){
            ServerResponse.createBySuccess("Update success.", updateUser);
        }
        return  ServerResponse.createByErrorMessage("Update failed.");
    }

    /**
     9.获取用户详细信息,如果没有登陆会强制登陆
     Receive User's specific Information
     */
    @Override
    public ServerResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }
}
