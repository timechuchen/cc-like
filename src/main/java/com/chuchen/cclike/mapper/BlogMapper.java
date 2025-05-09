package com.chuchen.cclike.mapper;

import com.chuchen.cclike.model.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
* @author chuchen
* @description 针对表【blog】的数据库操作Mapper
* @createDate 2025-05-08 15:38:26
* @Entity com.chuchen.cclike.model.entity.Blog
*/
public interface BlogMapper extends BaseMapper<Blog> {

    void batchUpdateThumbCount(@Param("countMap") Map<Long, Long> countMap);
}


