package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.BusinessException;
import com.tourism.dto.request.CityCreateRequest;
import com.tourism.dto.request.CityQueryRequest;
import com.tourism.dto.response.CitySimpleResponse;
import com.tourism.entity.ScenicCity;
import com.tourism.entity.ScenicSpot;
import com.tourism.mapper.ScenicCityMapper;
import com.tourism.mapper.ScenicSpotMapper;
import com.tourism.service.ScenicCityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 景点城市Service实现类
 */
@Service
public class ScenicCityServiceImpl implements ScenicCityService {

    @Autowired
    private ScenicCityMapper scenicCityMapper;

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    @Override
    public Page<ScenicCity> pageQuery(CityQueryRequest request) {
        Page<ScenicCity> page = new Page<>(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<ScenicCity> wrapper = new LambdaQueryWrapper<>();
        // 省份模糊查询
        if (StringUtils.hasText(request.getProvince())) {
            wrapper.like(ScenicCity::getProvince, request.getProvince());
        }
        // 城市模糊查询
        if (StringUtils.hasText(request.getCity())) {
            wrapper.like(ScenicCity::getCity, request.getCity());
        }
        wrapper.orderByDesc(ScenicCity::getId);
        return scenicCityMapper.selectPage(page, wrapper);
    }

    @Override
    public List<String> listAllProvinces() {
        // 查询所有不重复的省份
        List<String> provinces = scenicCityMapper.selectList(new LambdaQueryWrapper<ScenicCity>()
                        .select(ScenicCity::getProvince)
                        .isNotNull(ScenicCity::getProvince)
                ).stream()
                .map(ScenicCity::getProvince)
                .distinct()
                .collect(Collectors.toList());

        // 按行政区划代码(GB/T 2260)顺序排列: 华北-东北-华东-中南-西南-西北-港澳台
        List<String> adCodeOrder = Arrays.asList(
                "北京", "天津", "河北", "山西", "内蒙古",                 // 华北 11-15
                "辽宁", "吉林", "黑龙江",                                 // 东北 21-23
                "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东",   // 华东 31-37
                "河南", "湖北", "湖南", "广东", "广西", "海南",           // 中南 41-46
                "重庆", "四川", "贵州", "云南", "西藏",                   // 西南 50-54
                "陕西", "甘肃", "青海", "宁夏", "新疆",                   // 西北 61-65
                "台湾", "香港", "澳门"                                    // 港澳台 71/81/82
        );
        provinces.sort(Comparator.comparingInt(adCodeOrder::indexOf));
        return provinces;
    }

    @Override
    public List<CitySimpleResponse> listCitiesByProvince(String province) {
        return scenicCityMapper.selectList(new LambdaQueryWrapper<ScenicCity>()
                        .eq(StringUtils.hasText(province), ScenicCity::getProvince, province)
                        .orderByAsc(ScenicCity::getCity)
                ).stream()
                .map(c -> new CitySimpleResponse(c.getId(), c.getProvince(), c.getCity()))
                .collect(Collectors.toList());
    }

    @Override
    public ScenicCity getById(Integer id) {
        ScenicCity city = scenicCityMapper.selectById(id);
        if (city == null) {
            throw new BusinessException("城市不存在");
        }
        return city;
    }

    @Override
    public void create(CityCreateRequest request) {
        // 检查同一省份下城市名是否重复
        Long count = scenicCityMapper.selectCount(
                new LambdaQueryWrapper<ScenicCity>()
                        .eq(ScenicCity::getProvince, request.getProvince())
                        .eq(ScenicCity::getCity, request.getCity())
        );
        if (count > 0) {
            throw new BusinessException("该省份下已存在同名城市");
        }
        ScenicCity city = new ScenicCity();
        city.setProvince(request.getProvince());
        city.setCity(request.getCity());
        scenicCityMapper.insert(city);
    }

    @Override
    public void update(CityCreateRequest request) {
        ScenicCity city = scenicCityMapper.selectById(request.getId());
        if (city == null) {
            throw new BusinessException("城市不存在");
        }
        // 检查同一省份下城市名是否重复(排除自己)
        Long count = scenicCityMapper.selectCount(
                new LambdaQueryWrapper<ScenicCity>()
                        .eq(ScenicCity::getProvince, request.getProvince())
                        .eq(ScenicCity::getCity, request.getCity())
                        .ne(ScenicCity::getId, request.getId())
        );
        if (count > 0) {
            throw new BusinessException("该省份下已存在同名城市");
        }
        city.setProvince(request.getProvince());
        city.setCity(request.getCity());
        scenicCityMapper.updateById(city);
    }

    @Override
    public void delete(Integer id) {
        ScenicCity city = scenicCityMapper.selectById(id);
        if (city == null) {
            throw new BusinessException("城市不存在");
        }
        // 业务校验: 检查该城市下是否存在景点
        Long spotCount = scenicSpotMapper.selectCount(
                new LambdaQueryWrapper<ScenicSpot>()
                        .eq(ScenicSpot::getCityId, id)
        );
        if (spotCount > 0) {
            throw new BusinessException("该城市下存在景点，禁止删除");
        }
        scenicCityMapper.deleteById(id);
    }
}
