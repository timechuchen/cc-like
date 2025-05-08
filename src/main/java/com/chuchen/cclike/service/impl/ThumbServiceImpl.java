package com.chuchen.cclike.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chuchen.cclike.constant.ThumbConstant;
import com.chuchen.cclike.constant.UserConstant;
import com.chuchen.cclike.model.dto.thumb.DoThumbRequest;
import com.chuchen.cclike.model.entity.Blog;
import com.chuchen.cclike.model.entity.Thumb;
import com.chuchen.cclike.model.entity.User;
import com.chuchen.cclike.service.BlogService;
import com.chuchen.cclike.service.ThumbService;
import com.chuchen.cclike.mapper.ThumbMapper;
import com.chuchen.cclike.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
* @author chuchen
* @description 针对表【thumb】的数据库操作Service实现
* @createDate 2025-05-08 15:38:26
*/
@Service
@AllArgsConstructor
@Slf4j
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService{

    private TransactionTemplate transactionTemplate;

    private UserService userService;

    private BlogService blogService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if(doThumbRequest == null || doThumbRequest.getBlogId() == null){
            throw new RuntimeException("请求参数错误");
        }

        // 检验该博客是否存在
        Blog blog = blogService.getById(doThumbRequest.getBlogId());
        if(blog == null){
            throw new RuntimeException("该博客不存在");
        }

        User loginUser = userService.getLoginUser(request);

        // 加锁（这里用用户 ID 作为锁，因为同一个用户只能点赞一次）
        synchronized (loginUser.getId().toString().intern()) {
            // 因为点赞必须要是一个原子操作，所以这里用事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                // 检验该博客是否已经被该用户点赞过
                boolean exists = this.hasThumb(blogId, loginUser.getId());

                if(exists) {
                    throw new RuntimeException("用户已点赞");
                }

                // 该博客点赞数加一
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount + 1")
                        .update();

                // 保存点赞记录
                Thumb thumb = new Thumb();
                thumb.setBlogId(blogId);
                thumb.setUserId(loginUser.getId());
                boolean success = update && this.save(thumb);

                // 保存成功后，将点赞记录放入缓存
                if(success) {
                    redisTemplate.opsForHash().put(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId(), blogId.toString(), thumb.getId());
                }
                // 返回结果
                return success;
            });
        }
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }

        // 检验该博客是否存在
        Blog blog = blogService.getById(doThumbRequest.getBlogId());
        if(blog == null){
            throw new RuntimeException("该博客不存在");
        }

        User loginUser = userService.getLoginUser(request);
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {

            // 编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                Object thumbIdObj = redisTemplate.opsForHash().get(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(), blogId.toString());
                if (thumbIdObj == null) {
                    throw new RuntimeException("用户未点赞");
                }
                Long thumbId = Long.valueOf(thumbIdObj.toString());

                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();

                boolean success = update && this.removeById(thumbId);

                // 点赞记录从 Redis 删除
                if (success) {
                    redisTemplate.opsForHash().delete(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId(), blogId.toString());
                }
                return success;
            });
        }
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        return redisTemplate.opsForHash().hasKey(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, blogId.toString());
    }
}




