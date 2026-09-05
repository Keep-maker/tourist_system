package com.tourism.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.dto.request.CityCreateRequest;
import com.tourism.dto.request.CityQueryRequest;
import com.tourism.dto.response.CitySimpleResponse;
import com.tourism.entity.ScenicCity;

import java.util.List;

/**
 * 景点城市Service接口
 */
public interface ScenicCityService {

    /**
     * 分页条件查询城市
     * @param request 查询请求
     * @return 分页结果
     */
    Page<ScenicCity> pageQuery(CityQueryRequest request);

    /**
     * 获取全部省份列表(去重)
     * @return 省份列表
     */
    List<String> listAllProvinces();

    /**
     * 根据省份获取城市列表
     * @param province 省份
     * @return 城市列表
     */
    List<CitySimpleResponse> listCitiesByProvince(String province);

    /**
     * 根据ID查询城市
     * @param id 城市ID
     * @return 城市信息
     */
    ScenicCity getById(Integer id);

    /**
     * 新增城市
     * @param request 新增请求
     */
    void create(CityCreateRequest request);

    /**
     * 编辑城市
     * @param request 编辑请求
     */
    void update(CityCreateRequest request);

    /**
     * 删除城市(业务校验:存在景点则禁止删除)
     * @param id 城市ID
     */
    void delete(Integer id);
}
