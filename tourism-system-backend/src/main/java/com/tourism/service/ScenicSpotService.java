package com.tourism.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.dto.request.BatchDeleteRequest;
import com.tourism.dto.request.SpotCreateRequest;
import com.tourism.dto.request.SpotQueryRequest;
import com.tourism.dto.response.ImportResultResponse;
import com.tourism.dto.response.SpotDetailResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 景点Service接口
 */
public interface ScenicSpotService {

    /**
     * 多条件分页查询景点
     * @param request 查询请求
     * @return 分页结果
     */
    Page<SpotDetailResponse> pageQuery(SpotQueryRequest request);

    /**
     * 根据ID查询景点详情(带省份、城市信息)
     * @param id 景点ID
     * @return 景点详情
     */
    SpotDetailResponse getDetailById(Integer id);

    /**
     * 新增景点
     * @param request 新增请求
     */
    void create(SpotCreateRequest request);

    /**
     * 编辑景点
     * @param request 编辑请求
     */
    void update(SpotCreateRequest request);

    /**
     * 删除单个景点
     * @param id 景点ID
     */
    void delete(Integer id);

    /**
     * 批量删除景点
     * @param ids ID列表
     */
    void batchDelete(List<Integer> ids);

    /**
     * 导入景点数据(xlsx/csv)
     * @param file 上传的Excel文件
     * @return 导入结果统计
     */
    ImportResultResponse importSpots(MultipartFile file);

    /**
     * 导出景点数据(xlsx)
     * @param query 筛选条件
     * @param response HttpServletResponse
     */
    void exportSpots(SpotQueryRequest query, HttpServletResponse response);
}
