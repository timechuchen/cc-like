package com.chuchen.cclike.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chuchen.cclike.constant.RedisLuaScriptConstant;
import com.chuchen.cclike.constant.ThumbConstant;
import com.chuchen.cclike.mapper.ThumbMapper;
import com.chuchen.cclike.model.dto.thumb.DoThumbRequest;
import com.chuchen.cclike.model.entity.Blog;
import com.chuchen.cclike.model.entity.Thumb;
import com.chuchen.cclike.model.entity.User;
import com.chuchen.cclike.model.enums.LuaStatusEnum;
import com.chuchen.cclike.service.BlogService;
import com.chuchen.cclike.service.ThumbService;
import com.chuchen.cclike.service.UserService;
import com.chuchen.cclike.util.RedisKeyUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Objects;

/**
* @author chuchen
* @description 针对表【thumb】的数据库操作Service实现
* @createDate 2025-05-08 15:38:26
*/
@Service("thumbServiceRedis")
@RequiredArgsConstructor
@Slf4j
public class ThumbServiceRedisImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService{

    private final UserService userService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if(doThumbRequest == null || doThumbRequest.getBlogId() == null){
            throw new RuntimeException("请求参数错误");
        }

        User loginUser = userService.getLoginUser(request);
        long blogId = doThumbRequest.getBlogId();

        String timeSlice = getTimeSlice();
        // Redis Key
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());

        // 执行 Lua 脚本
        Long result = redisTemplate.execute(
                RedisLuaScriptConstant.THUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUser.getId(),
                blogId
        );

        if(Objects.equals(LuaStatusEnum.FAIL.getValue(), result)){
            throw new RuntimeException("用户已点赞");
        }

        return Objects.equals(LuaStatusEnum.SUCCESS.getValue(), result);
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }

        User loginUser = userService.getLoginUser(request);
        long blogId = doThumbRequest.getBlogId();

        String timeSlice = getTimeSlice();
        // Redis Key
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());

        // 执行 Lua 脚本
        Long result = redisTemplate.execute(
                RedisLuaScriptConstant.UNTHUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUser.getId(),
                blogId
        );

        if(Objects.equals(LuaStatusEnum.FAIL.getValue(), result)){
            throw new RuntimeException("用户未点赞");
        }

        return Objects.equals(LuaStatusEnum.SUCCESS.getValue(), result);
    }

    private String getTimeSlice() {
        DateTime nowDate = new DateTime();
        // 获取到当前时间前最接近的整数秒，比如 12:01:00 -> 12:00
        return DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10) * 10;
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserThumbKey(userId), blogId.toString());
    }
}




