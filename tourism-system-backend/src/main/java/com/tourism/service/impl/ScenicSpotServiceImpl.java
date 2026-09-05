package com.tourism.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.BusinessException;
import com.tourism.dto.request.SpotCreateRequest;
import com.tourism.dto.request.SpotExcelDTO;
import com.tourism.dto.request.SpotQueryRequest;
import com.tourism.dto.response.ImportResultResponse;
import com.tourism.dto.response.SpotDetailResponse;
import com.tourism.entity.ScenicCity;
import com.tourism.entity.ScenicSpot;
import com.tourism.mapper.ScenicCityMapper;
import com.tourism.mapper.ScenicSpotMapper;
import com.tourism.service.ScenicSpotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 景点Service实现类
 */
@Slf4j
@Service
public class ScenicSpotServiceImpl implements ScenicSpotService {

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    @Autowired
    private ScenicCityMapper scenicCityMapper;

    @Override
    public Page<SpotDetailResponse> pageQuery(SpotQueryRequest request) {
        Page<ScenicSpot> page = new Page<>(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();

        // 多条件筛选
        // 省份筛选: 查出该省所有城市ID, 按 city_id IN (...) 过滤
        if (StringUtils.hasText(request.getProvince())) {
            List<ScenicCity> cities = scenicCityMapper.selectList(
                    new LambdaQueryWrapper<ScenicCity>()
                            .eq(ScenicCity::getProvince, request.getProvince())
            );
            List<Integer> cityIds = cities.stream().map(ScenicCity::getId).collect(Collectors.toList());
            if (cityIds.isEmpty()) {
                // 该省份无城市, 直接返回空页
                page.setRecords(new ArrayList<>());
                return new Page<>(page.getCurrent(), page.getSize(), 0);
            }
            if (request.getCityId() != null && !cityIds.contains(request.getCityId())) {
                // 同时指定了cityId但不在该省内, 直接返回空
                page.setRecords(new ArrayList<>());
                return new Page<>(page.getCurrent(), page.getSize(), 0);
            }
            if (request.getCityId() == null) {
                wrapper.in(ScenicSpot::getCityId, cityIds);
            }
            // 如果 cityId 也指定了, 后续 eq 条件会加上, 不冲突
        }

        if (request.getCityId() != null) {
            wrapper.eq(ScenicSpot::getCityId, request.getCityId());
        }
        if (StringUtils.hasText(request.getSpotType())) {
            wrapper.eq(ScenicSpot::getSpotType, request.getSpotType());
        }
        if (StringUtils.hasText(request.getSpotName())) {
            wrapper.like(ScenicSpot::getSpotName, request.getSpotName());
        }
        // 评分区间
        if (request.getMinScore() != null) {
            wrapper.ge(ScenicSpot::getScore, request.getMinScore());
        }
        if (request.getMaxScore() != null) {
            wrapper.le(ScenicSpot::getScore, request.getMaxScore());
        }
        // 门票价格区间
        if (request.getMinPrice() != null) {
            wrapper.ge(ScenicSpot::getTicketPrice, request.getMinPrice());
        }
        if (request.getMaxPrice() != null) {
            wrapper.le(ScenicSpot::getTicketPrice, request.getMaxPrice());
        }
        wrapper.orderByDesc(ScenicSpot::getId);

        Page<ScenicSpot> spotPage = scenicSpotMapper.selectPage(page, wrapper);

        // 转换为详情响应(带省份、城市信息)
        Page<SpotDetailResponse> resultPage = new Page<>(spotPage.getCurrent(), spotPage.getSize(), spotPage.getTotal());
        List<SpotDetailResponse> records = spotPage.getRecords().stream()
                .map(this::convertToDetailResponse)
                .collect(Collectors.toList());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public SpotDetailResponse getDetailById(Integer id) {
        ScenicSpot spot = scenicSpotMapper.selectById(id);
        if (spot == null) {
            throw new BusinessException("景点不存在");
        }
        return convertToDetailResponse(spot);
    }

    @Override
    public void create(SpotCreateRequest request) {
        // 检查城市是否存在
        ScenicCity city = scenicCityMapper.selectById(request.getCityId());
        if (city == null) {
            throw new BusinessException("所属城市不存在");
        }
        ScenicSpot spot = new ScenicSpot();
        copyRequestToEntity(request, spot);
        scenicSpotMapper.insert(spot);
    }

    @Override
    public void update(SpotCreateRequest request) {
        ScenicSpot spot = scenicSpotMapper.selectById(request.getId());
        if (spot == null) {
            throw new BusinessException("景点不存在");
        }
        // 如果修改了城市，检查新城市是否存在
        if (!spot.getCityId().equals(request.getCityId())) {
            ScenicCity city = scenicCityMapper.selectById(request.getCityId());
            if (city == null) {
                throw new BusinessException("所属城市不存在");
            }
        }
        copyRequestToEntity(request, spot);
        scenicSpotMapper.updateById(spot);
    }

    /**
     * 将请求参数复制到实体
     */
    private void copyRequestToEntity(SpotCreateRequest request, ScenicSpot spot) {
        spot.setCityId(request.getCityId());
        spot.setDistrict(request.getDistrict());
        spot.setSpotName(request.getSpotName());
        spot.setSpotType(request.getSpotType());
        spot.setScore(request.getScore());
        spot.setTicketPrice(request.getTicketPrice());
        spot.setSalesVolume(request.getSalesVolume());
        spot.setSpotIntro(request.getSpotIntro());
        spot.setAddress(request.getAddress());
        spot.setStarLevel(request.getStarLevel());
    }

    @Override
    public void delete(Integer id) {
        ScenicSpot spot = scenicSpotMapper.selectById(id);
        if (spot == null) {
            throw new BusinessException("景点不存在");
        }
        scenicSpotMapper.deleteById(id);
    }

    @Override
    public void batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的景点");
        }
        scenicSpotMapper.deleteBatchIds(ids);
    }

    /**
     * 导入景点数据(xlsx/csv)
     * 字段映射:
     * - name → spot_name
     * - score(0-1区间) → score(×5换算到0-5,兼容星级显示)
     * - level → spot_type(景区等级字符串) + star_level(提取数字,5A→5)
     * - address → address
     * - comments → spot_intro
     * - price → ticket_price
     * - sales → sales_volume
     * - region(省·市·区) → 拆分: 省+市→city_id, 第3段→district
     * - 经纬度: 新数据集无,留空(热力地图靠province聚合)
     * 脏数据过滤: 景点名称为空→丢弃; 门票价格<0→丢弃
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultResponse importSpots(MultipartFile file) {
        int total = 0;
        int success = 0;
        int fail = 0;

        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new BusinessException("文件名无效");
        }
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        if (!extension.equals("xlsx") && !extension.equals("xls") && !extension.equals("csv")) {
            throw new BusinessException("仅支持 xlsx、xls、csv 格式文件");
        }

        try (InputStream inputStream = file.getInputStream()) {
            List<SpotExcelDTO> excelDataList = EasyExcel.read(inputStream)
                    .head(SpotExcelDTO.class)
                    .sheet()
                    .doReadSync();

            total = excelDataList.size();

            for (SpotExcelDTO data : excelDataList) {
                try {
                    if (!isValidData(data)) {
                        fail++;
                        continue;
                    }

                    // 拆分region(格式: 省·市·区) 得到省市和区县
                    String[] parts = splitRegion(data.getRegion());
                    String province = parts[0];
                    String city = parts[1];
                    String district = parts[2];

                    // 根据省份+城市匹配city_id，不存在则新增城市
                    Integer cityId = getOrCreateCity(province, city);

                    ScenicSpot spot = new ScenicSpot();
                    spot.setCityId(cityId);
                    spot.setDistrict(district);
                    spot.setSpotName(data.getName().trim());

                    // 景区等级存为spot_type; 数据集用 \N 表示无等级
                    String level = StringUtils.hasText(data.getLevel()) ? data.getLevel().trim() : "未评级";
                    if (level.equals("\\N") || level.equalsIgnoreCase("NULL") || level.equalsIgnoreCase("NaN")) {
                        level = "未评级";
                    }
                    spot.setSpotType(level);

                    // score 0-1 换算到 0-5,兼容el-rate星级显示和评分轴
                    BigDecimal score = data.getScore() != null
                            ? data.getScore().multiply(new BigDecimal("5")).setScale(1, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    spot.setScore(score);

                    spot.setTicketPrice(data.getPrice() != null ? data.getPrice() : BigDecimal.ZERO);
                    spot.setSalesVolume(data.getSales() != null ? data.getSales() : BigDecimal.ZERO);
                    spot.setSpotIntro(StringUtils.hasText(data.getComments()) ? data.getComments().trim() : "");
                    spot.setAddress(StringUtils.hasText(data.getAddress()) ? data.getAddress().trim() : "");
                    spot.setStarLevel(parseStarLevel(level));
                    scenicSpotMapper.insert(spot);
                    success++;

                } catch (Exception e) {
                    log.warn("处理行数据异常: {}", e.getMessage());
                    fail++;
                }
            }

        } catch (Exception e) {
            log.error("导入文件解析失败", e);
            throw new BusinessException("文件解析失败: " + e.getMessage());
        }

        return new ImportResultResponse(total, success, fail);
    }

    /**
     * 拆分region(格式: 省·市·区) 返回[省, 市, 区]
     * 不足3段时: 没有市用省代替,没有区留空
     */
    private String[] splitRegion(String region) {
        String[] result = new String[]{"", "", ""};
        if (StringUtils.hasText(region)) {
            String[] parts = region.split("·");
            if (parts.length >= 1) result[0] = parts[0].trim();
            if (parts.length >= 2) result[1] = parts[1].trim();
            else result[1] = result[0];
            if (parts.length >= 3) result[2] = parts[2].trim();
        }
        return result;
    }

    /**
     * 从景区等级字符串提取数字(5A景区→5,4A景区→4)
     * 提取不到返回3(默认)
     */
    private Integer parseStarLevel(String level) {
        if (!StringUtils.hasText(level)) {
            return 3;
        }
        for (char ch : level.toCharArray()) {
            if (Character.isDigit(ch)) {
                return Integer.parseInt(String.valueOf(ch));
            }
        }
        return 3;
    }

    /**
     * 导出景点数据(xlsx)
     */
    @Override
    public void exportSpots(SpotQueryRequest query, HttpServletResponse response) {
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            if (query.getCityId() != null) {
                wrapper.eq(ScenicSpot::getCityId, query.getCityId());
            }
            if (StringUtils.hasText(query.getSpotType())) {
                wrapper.eq(ScenicSpot::getSpotType, query.getSpotType());
            }
            if (StringUtils.hasText(query.getSpotName())) {
                wrapper.like(ScenicSpot::getSpotName, query.getSpotName());
            }
            if (query.getMinScore() != null) {
                wrapper.ge(ScenicSpot::getScore, query.getMinScore());
            }
            if (query.getMaxScore() != null) {
                wrapper.le(ScenicSpot::getScore, query.getMaxScore());
            }
            if (query.getMinPrice() != null) {
                wrapper.ge(ScenicSpot::getTicketPrice, query.getMinPrice());
            }
            if (query.getMaxPrice() != null) {
                wrapper.le(ScenicSpot::getTicketPrice, query.getMaxPrice());
            }
        }
        wrapper.orderByDesc(ScenicSpot::getId);
        List<ScenicSpot> spotList = scenicSpotMapper.selectList(wrapper);

        // 转换为导出DTO(字段映射回新数据集格式)
        List<SpotExcelDTO> exportList = new ArrayList<>();
        for (ScenicSpot spot : spotList) {
            SpotExcelDTO dto = new SpotExcelDTO();
            dto.setNumber(spot.getId());
            dto.setName(spot.getSpotName());
            dto.setScore(spot.getScore());
            dto.setLevel(spot.getSpotType());  // 景区等级
            dto.setAddress(spot.getAddress());
            dto.setComments(spot.getSpotIntro());
            dto.setPrice(spot.getTicketPrice());
            dto.setSales(spot.getSalesVolume());
            // province 和 region 从关联城市查并拼接
            if (spot.getCityId() != null) {
                ScenicCity city = scenicCityMapper.selectById(spot.getCityId());
                if (city != null) {
                    dto.setProvince(city.getProvince());
                    dto.setRegion(city.getProvince() + "·" + city.getCity() + "·" + spot.getDistrict());
                }
            }
            exportList.add(dto);
        }

        try {
            String fileName = URLEncoder.encode("景点数据导出", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            try (OutputStream out = response.getOutputStream()) {
                EasyExcel.write(out, SpotExcelDTO.class)
                        .sheet("景点数据")
                        .doWrite(exportList);
            }
        } catch (Exception e) {
            log.error("导出失败", e);
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }

    /**
     * 校验数据是否有效
     * 新数据集无经纬度字段,只校验名称和价格
     * (原3.5~5.0评分区间校验已删除: 新数据score是0-1区间,导入时×5换算)
     */
    private boolean isValidData(SpotExcelDTO data) {
        if (data.getName() == null || data.getName().trim().isEmpty()) {
            return false;
        }
        if (data.getPrice() != null && data.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        return true;
    }

    /**
     * 根据省份+城市获取或创建城市记录
     */
    private Integer getOrCreateCity(String province, String city) {
        if (!StringUtils.hasText(province) || !StringUtils.hasText(city)) {
            throw new BusinessException("省份和城市不能为空");
        }
        ScenicCity existingCity = scenicCityMapper.selectOne(
                new LambdaQueryWrapper<ScenicCity>()
                        .eq(ScenicCity::getProvince, province.trim())
                        .eq(ScenicCity::getCity, city.trim())
        );
        if (existingCity != null) {
            return existingCity.getId();
        }
        ScenicCity newCity = new ScenicCity();
        newCity.setProvince(province.trim());
        newCity.setCity(city.trim());
        scenicCityMapper.insert(newCity);
        return newCity.getId();
    }

    /**
     * 转换景点实体为详情响应(带省份、城市信息)
     */
    private SpotDetailResponse convertToDetailResponse(ScenicSpot spot) {
        SpotDetailResponse response = new SpotDetailResponse();
        response.setId(spot.getId());
        response.setCityId(spot.getCityId());
        response.setDistrict(spot.getDistrict());
        response.setSpotName(spot.getSpotName());
        response.setSpotType(spot.getSpotType());
        response.setScore(spot.getScore());
        response.setTicketPrice(spot.getTicketPrice());
        response.setSalesVolume(spot.getSalesVolume());
        response.setSpotIntro(spot.getSpotIntro());
        response.setAddress(spot.getAddress());
        response.setStarLevel(spot.getStarLevel());
        // 查询关联的城市信息
        if (spot.getCityId() != null) {
            ScenicCity city = scenicCityMapper.selectById(spot.getCityId());
            if (city != null) {
                response.setProvince(city.getProvince());
                response.setCity(city.getCity());
            }
        }
        return response;
    }
}
