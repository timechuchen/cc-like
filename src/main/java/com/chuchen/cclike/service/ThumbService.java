package com.chuchen.cclike.service;

import com.chuchen.cclike.model.dto.thumb.DoThumbRequest;
import com.chuchen.cclike.model.entity.Thumb;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author chuchen
* @description 针对表【thumb】的数据库操作Service
* @createDate 2025-05-08 15:38:26
*/
public interface ThumbService extends IService<Thumb> {

    /**
     * 点赞
     * @param doThumbRequest 点赞的请求（博客 ID）
     * @param request 请求
     * @return {@link Boolean }
     */
    Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);

    /**
     * 取消点赞
     * @param doThumbRequest 取消点赞的请求（博客 ID）
     * @param request 请求
     * @return {@link Boolean }
     */
    Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);

    /**
     * 判断用户是否点赞
     * @param blogId 博客 ID
     * @param userId 用户 ID
     * @return {@link Boolean }
     */
    Boolean hasThumb(Long blogId, Long userId);
}
