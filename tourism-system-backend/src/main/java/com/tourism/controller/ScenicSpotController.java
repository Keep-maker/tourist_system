package com.tourism.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.Result;
import com.tourism.dto.request.BatchDeleteRequest;
import com.tourism.dto.request.SpotCreateRequest;
import com.tourism.dto.request.SpotQueryRequest;
import com.tourism.dto.response.ImportResultResponse;
import com.tourism.dto.response.SpotDetailResponse;
import com.tourism.service.ScenicSpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 景点Controller
 * 包含景点CRUD、导入导出接口
 */
@RestController
@RequestMapping("/api/admin/spot")
public class ScenicSpotController {

    @Autowired
    private ScenicSpotService scenicSpotService;

    /**
     * 多条件分页查询景点
     * GET /api/admin/spot/page
     */
    @GetMapping("/page")
    public Result<Page<SpotDetailResponse>> pageQuery(SpotQueryRequest request) {
        Page<SpotDetailResponse> page = scenicSpotService.pageQuery(request);
        return Result.success(page);
    }

    /**
     * 根据ID查询景点详情
     * GET /api/admin/spot/{id}
     */
    @GetMapping("/{id}")
    public Result<SpotDetailResponse> getDetail(@PathVariable Integer id) {
        SpotDetailResponse detail = scenicSpotService.getDetailById(id);
        return Result.success(detail);
    }

    /**
     * 新增景点
     * POST /api/admin/spot
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody SpotCreateRequest request) {
        scenicSpotService.create(request);
        return Result.success("新增成功", null);
    }

    /**
     * 编辑景点
     * PUT /api/admin/spot
     */
    @PutMapping
    public Result<Void> update(@RequestBody SpotCreateRequest request) {
        scenicSpotService.update(request);
        return Result.success("修改成功", null);
    }

    /**
     * 删除单个景点
     * DELETE /api/admin/spot/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        scenicSpotService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 批量删除景点
     * POST /api/admin/spot/batch
     */
    @PostMapping("/batch")
    public Result<Void> batchDelete(@RequestBody BatchDeleteRequest request) {
        scenicSpotService.batchDelete(request.getIds());
        return Result.success("批量删除成功", null);
    }

    /**
     * 导入景点数据(xlsx/csv)
     * POST /api/admin/spot/import
     */
    @PostMapping("/import")
    public Result<ImportResultResponse> importSpots(@RequestParam("file") MultipartFile file) {
        ImportResultResponse result = scenicSpotService.importSpots(file);
        return Result.success("导入完成", result);
    }

    /**
     * 导出景点数据(xlsx)
     * GET /api/admin/spot/export
     */
    @GetMapping("/export")
    public void exportSpots(SpotQueryRequest query, HttpServletResponse response) {
        scenicSpotService.exportSpots(query, response);
    }
}
