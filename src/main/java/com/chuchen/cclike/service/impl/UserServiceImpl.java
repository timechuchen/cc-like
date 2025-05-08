package com.chuchen.cclike.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chuchen.cclike.constant.UserConstant;
import com.chuchen.cclike.model.entity.User;
import com.chuchen.cclike.service.UserService;
import com.chuchen.cclike.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
* @author chuchen
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-05-08 15:38:26
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    @Override
    public User getLoginUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(UserConstant.LOGIN_USER);
    }

}




