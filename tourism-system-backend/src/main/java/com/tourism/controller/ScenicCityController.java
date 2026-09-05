package com.tourism.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.Result;
import com.tourism.dto.request.CityCreateRequest;
import com.tourism.dto.request.CityQueryRequest;
import com.tourism.dto.response.CitySimpleResponse;
import com.tourism.entity.ScenicCity;
import com.tourism.service.ScenicCityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 景点城市Controller
 */
@RestController
@RequestMapping("/api/city")
public class ScenicCityController {

    @Autowired
    private ScenicCityService scenicCityService;

    /**
     * 分页条件查询城市
     * GET /api/city/page
     */
    @GetMapping("/page")
    public Result<Page<ScenicCity>> pageQuery(CityQueryRequest request) {
        Page<ScenicCity> page = scenicCityService.pageQuery(request);
        return Result.success(page);
    }

    /**
     * 获取全部省份列表
     * GET /api/city/provinces
     */
    @GetMapping("/provinces")
    public Result<List<String>> listAllProvinces() {
        List<String> provinces = scenicCityService.listAllProvinces();
        return Result.success(provinces);
    }

    /**
     * 根据省份获取城市列表
     * GET /api/city/listByProvince
     */
    @GetMapping("/listByProvince")
    public Result<List<CitySimpleResponse>> listByProvince(
            @RequestParam(required = false) String province) {
        List<CitySimpleResponse> cities = scenicCityService.listCitiesByProvince(province);
        return Result.success(cities);
    }

    /**
     * 根据ID查询城市
     * GET /api/city/{id}
     */
    @GetMapping("/{id}")
    public Result<ScenicCity> getById(@PathVariable Integer id) {
        ScenicCity city = scenicCityService.getById(id);
        return Result.success(city);
    }

    /**
     * 新增城市
     * POST /api/city
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CityCreateRequest request) {
        scenicCityService.create(request);
        return Result.success("新增成功", null);
    }

    /**
     * 编辑城市
     * PUT /api/city
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody CityCreateRequest request) {
        scenicCityService.update(request);
        return Result.success("修改成功", null);
    }

    /**
     * 删除城市(业务校验:存在景点则禁止删除)
     * DELETE /api/city/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        scenicCityService.delete(id);
        return Result.success("删除成功", null);
    }
}
