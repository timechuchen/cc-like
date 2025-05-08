package com.chuchen.cclike.controller;

import com.chuchen.cclike.common.BaseResponse;
import com.chuchen.cclike.common.ResultUtils;
import com.chuchen.cclike.constant.UserConstant;
import com.chuchen.cclike.model.entity.User;
import com.chuchen.cclike.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chuchen
 * @date 2025/5/8 15:47
 * @description 用户控制器
 */
@RestController
@RequestMapping("user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/login")
    public BaseResponse<User> login(long userId, HttpServletRequest request) {
        User user = userService.getById(userId);
        request.getSession().setAttribute(UserConstant.LOGIN_USER, user);
        return ResultUtils.success(user);
    }

    /**
     * 获取登录用户
     * @param request 请求
     * @return BaseResponse<User>
     */
    @GetMapping("/get/login")
    public BaseResponse<User> getLoginUser(HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(UserConstant.LOGIN_USER);
        return ResultUtils.success(loginUser);
    }
}
