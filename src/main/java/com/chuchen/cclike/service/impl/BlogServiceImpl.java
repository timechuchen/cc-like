package com.chuchen.cclike.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chuchen.cclike.model.entity.Blog;
import com.chuchen.cclike.model.entity.Thumb;
import com.chuchen.cclike.model.entity.User;
import com.chuchen.cclike.model.vo.BlogVO;
import com.chuchen.cclike.service.BlogService;
import com.chuchen.cclike.mapper.BlogMapper;
import com.chuchen.cclike.service.ThumbService;
import com.chuchen.cclike.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author chuchen
* @description 针对表【blog】的数据库操作Service实现
* @createDate 2025-05-08 15:38:26
*/
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService{

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private ThumbService thumbService;

    @Override
    public BlogVO getBlogVOById(long blogId, HttpServletRequest request) {
        Blog blog = this.getById(blogId);
        User loginUser = userService.getLoginUser(request);
        return this.getBlogVO(blog, loginUser);
    }

    @Override
    public List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Map<Long, Boolean> blogIdHasThumbMap = new HashMap<>();

        // 如果是登陆过的用户
        if (ObjUtil.isNotEmpty(loginUser)) {
            // 根据博客集合重新构建博客id集合
            Set<Long> blogIdSet = blogList.stream().map(Blog::getId).collect(Collectors.toSet());
            // 获取点赞
            List<Thumb> thumbList = thumbService.lambdaQuery()
                    .eq(Thumb::getUserId, loginUser.getId())
                    .in(Thumb::getBlogId, blogIdSet)
                    .list();

            // 构建博客id和点赞的映射（是否被当前用户点赞的映射）
            thumbList.forEach(blogThumb -> blogIdHasThumbMap.put(blogThumb.getBlogId(), true));
        }

        return blogList.stream()
                .map(blog -> {
                    BlogVO blogVO = BeanUtil.copyProperties(blog, BlogVO.class);
                    blogVO.setHasThumb(blogIdHasThumbMap.get(blog.getId()));
                    return blogVO;
                })
                .toList();
    }

    private BlogVO getBlogVO(Blog blog, User loginUser) {
        BlogVO blogVO = new BlogVO();
        BeanUtil.copyProperties(blog, blogVO);

        if (loginUser == null) {
            return blogVO;
        }

        Thumb thumb = thumbService.lambdaQuery()
                .eq(Thumb::getUserId, loginUser.getId())
                .eq(Thumb::getBlogId, blog.getId())
                .one();
        blogVO.setHasThumb(thumb != null);

        return blogVO;
    }

}




