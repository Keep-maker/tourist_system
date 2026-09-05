package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户行为Mapper接口
 * 继承MyBatis-Plus BaseMapper, 获得基础CRUD能力
 */
@Mapper
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {
    // BaseMapper已提供基础CRUD
    // 自定义聚合SQL在XML映射文件中定义(如统计景点共现次数)
}
