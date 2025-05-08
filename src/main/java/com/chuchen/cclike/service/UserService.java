package com.chuchen.cclike.service;

import com.chuchen.cclike.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.net.http.HttpRequest;

/**
* @author chuchen
* @description 针对表【user】的数据库操作Service
* @createDate 2025-05-08 15:38:26
*/
public interface UserService extends IService<User> {

    /**
     * 获取登录用户
     * @param request 请求
     * @return User
     */
    User getLoginUser(HttpServletRequest request);
}
