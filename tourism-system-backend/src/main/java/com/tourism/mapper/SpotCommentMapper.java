package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.SpotComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 景点评论Mapper接口
 * 继承MyBatis-Plus BaseMapper, 获得基础CRUD能力
 */
@Mapper
public interface SpotCommentMapper extends BaseMapper<SpotComment> {
    // BaseMapper已提供基础CRUD
}
