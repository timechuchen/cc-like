package com.chuchen.cclike.service;

import com.chuchen.cclike.model.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chuchen.cclike.model.vo.BlogVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author chuchen
* @description 针对表【blog】的数据库操作Service
* @createDate 2025-05-08 15:38:26
*/
public interface BlogService extends IService<Blog> {

    /**
     * 根据 ID 获取博客
     * @param blogId 博客 id
     * @param request 请求
     * @return BlogVO
     */
    BlogVO getBlogVOById(long blogId, HttpServletRequest request);

    /**
     * 获取博客列表（这不是本项目的核，所以这里只是将所有的博客里表直接查询出来封装给前端就行）
     * @param blogList 博客列表
     * @param request 请求
     * @return List<BlogVO>
     */
    List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request);

}
