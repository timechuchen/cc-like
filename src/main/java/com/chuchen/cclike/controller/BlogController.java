package com.chuchen.cclike.controller;

import com.chuchen.cclike.common.BaseResponse;
import com.chuchen.cclike.common.ResultUtils;
import com.chuchen.cclike.model.entity.Blog;
import com.chuchen.cclike.model.vo.BlogVO;
import com.chuchen.cclike.service.BlogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author chuchen
 * @date 2025/5/8 16:17
 * @description 博客控制器
 */
@RestController
@RequestMapping("blog")
public class BlogController {

    @Resource
    private BlogService blogService;

    @GetMapping("/get")
    public BaseResponse<BlogVO> get(long blogId, HttpServletRequest request) {
        BlogVO blogVO = blogService.getBlogVOById(blogId, request);
        return ResultUtils.success(blogVO);
    }

    @GetMapping("/list")
    public BaseResponse<List<BlogVO>> list(HttpServletRequest request) {
        List<Blog> blogList = blogService.list();
        List<BlogVO> blogVOList = blogService.getBlogVOList(blogList, request);
        return ResultUtils.success(blogVOList);
    }
}