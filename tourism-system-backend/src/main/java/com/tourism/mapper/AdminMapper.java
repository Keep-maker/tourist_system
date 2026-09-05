package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员Mapper接口
 * 继承MyBatis-Plus BaseMapper，获得基础CRUD能力
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
    // BaseMapper已提供基础CRUD: insert/deleteById/updateById/selectById/selectList/selectPage等
    // 自定义SQL方法后续在XML映射文件中定义
}
