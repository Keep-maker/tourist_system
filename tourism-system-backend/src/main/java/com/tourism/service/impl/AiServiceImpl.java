package com.tourism.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.entity.ScenicSpot;
import com.tourism.mapper.ScenicSpotMapper;
import com.tourism.service.AiService;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI旅游助手实现类
 * 思路(轻量RAG):
 * 1. 从数据库查出热门景点(TOP20), 拼进系统提示词, 让大模型基于"真实数据"回答
 * 2. 调用智谱GLM-4-Flash(OpenAI兼容协议, stream=true)获得SSE流
 * 3. 解析上游每个增量块, 只提取delta.content, 重新包装成SSE事件推给前端
 */
@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.api-url:https://open.bigmodel.cn/api/paas/v4/chat/completions}")
    private String apiUrl;

    @Value("${ai.model:glm-4-flash}")
    private String model;

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    /**
     * 流式对话主流程
     */
    @Override
    public void chatStream(String message, List<Map<String, String>> history, HttpServletResponse response) {
        try {
            // 0.统一设置SSE响应头(必须在写任何内容之前, 否则中文会按ISO-8859-1编码乱码)
            response.setContentType("text/event-stream;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("X-Accel-Buffering", "no");

            // 1.校验API Key是否已配置
            if (apiKey == null || apiKey.isBlank() || apiKey.contains("粘贴")) {
                sendSseEvent(response, "AI服务尚未配置API Key, 请在application.yml的ai.api-key中填入智谱APIKey后重启后端");
                sendSseDone(response);
                return;
            }

            // 2.组装对话消息: 系统提示词(含景点数据) + 历史对话 + 本次提问
            List<Map<String, String>> messages = buildMessages(message, history);

            // 3.构造OpenAI兼容协议请求体
            JSONObject reqBody = new JSONObject();
            reqBody.put("model", model);
            reqBody.put("messages", messages);
            reqBody.put("stream", true);
            reqBody.put("temperature", 0.7);
            reqBody.put("max_tokens", 1024);

            // 4.调用智谱API(JDK17 HttpClient, 流式读取响应)
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(120))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(reqBody.toJSONString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<java.io.InputStream> upstream =
                    client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            // 5.上游返回非200, 读取错误信息通知前端
            if (upstream.statusCode() != 200) {
                String errorBody = new BufferedReader(
                        new InputStreamReader(upstream.body(), StandardCharsets.UTF_8))
                        .lines().reduce("", (a, b) -> a + b);
                log.error("智谱API调用失败: status={}, body={}", upstream.statusCode(), errorBody);
                sendSseEvent(response, "AI服务调用失败(" + upstream.statusCode() + "), 请稍后再试");
                sendSseDone(response);
                return;
            }

            // 6.逐行解析上游SSE, 把delta.content实时推给前端
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(upstream.body(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                // SSE格式: "data: {...}", 只处理data开头的行
                if (!line.startsWith("data:")) {
                    continue;
                }
                String payload = line.substring(5).trim();
                // [DONE]表示流结束
                if ("[DONE]".equals(payload)) {
                    break;
                }
                JSONObject chunk = JSON.parseObject(payload);
                JSONArray choices = chunk.getJSONArray("choices");
                if (choices == null || choices.isEmpty()) {
                    continue;
                }
                JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                if (delta == null) {
                    continue;
                }
                String content = delta.getString("content");
                if (content != null && !content.isEmpty()) {
                    sendSseEvent(response, JSON.toJSONString(Map.of("content", content)));
                }
            }
            reader.close();
            sendSseDone(response);
        } catch (Exception e) {
            log.error("AI对话异常", e);
            try {
                sendSseEvent(response, "AI助手开小差了, 请稍后再试");
                sendSseDone(response);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 组装对话消息: 系统提示词(注入热门景点数据) + 历史对话 + 本次提问
     */
    private List<Map<String, String>> buildMessages(String message, List<Map<String, String>> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt()));

        // 保留前端传来的最近几轮对话(最多6条), 维持上下文
        if (history != null) {
            int from = Math.max(0, history.size() - 6);
            for (int i = from; i < history.size(); i++) {
                Map<String, String> h = history.get(i);
                String role = "assistant".equals(h.get("role")) ? "assistant" : "user";
                String content = h.get("content");
                if (content != null && !content.isBlank()) {
                    messages.add(Map.of("role", role, "content", content));
                }
            }
        }
        messages.add(Map.of("role", "user", "content", message));
        return messages;
    }

    /**
     * 系统提示词: 定义助手角色 + 注入数据库热门景点数据(轻量RAG)
     */
    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("你是\"全国旅游景点信息管理系统\"的AI旅游助手小旅，专业、友好、热爱旅行。\n\n");
        sb.append("【回答规范】\n");
        sb.append("1. 优先依据下面系统内的真实景点数据回答用户问题；数据之外的可以结合旅游常识补充，但要提醒仅供参考。\n");
        sb.append("2. 回答必须使用 Markdown 格式：\n");
        sb.append("   - 标题使用 # / ## / ### 分层；\n");
        sb.append("   - 列表使用 - 或 1. 2. 3.；\n");
        sb.append("   - 关键信息使用 **加粗** 强调；\n");
        sb.append("   - 涉及代码、SQL、配置时，用 ```language ... ``` 包裹；\n");
        sb.append("   - 表格用 | 绘制；\n");
        sb.append("   - 重点提示用 > 引用块。\n");
        sb.append("3. 语言：中文，简洁友好，适当分段，总字数控制在 400 字以内。\n\n");
        sb.append("【系统内热门景点数据(TOP20，按销量排序)】\n");

        // 按销量倒序取20个热门景点, 拼成紧凑表格文本
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(ScenicSpot::getSpotName, ScenicSpot::getSpotType, ScenicSpot::getStarLevel,
                        ScenicSpot::getScore, ScenicSpot::getTicketPrice, ScenicSpot::getAddress)
                .orderByDesc(ScenicSpot::getSalesVolume)
                .last("LIMIT 20");
        List<ScenicSpot> spots = scenicSpotMapper.selectList(wrapper);

        sb.append("| 名称 | 类型 | 星级 | 评分 | 门票 | 地址 |\n");
        sb.append("| --- | --- | --- | --- | --- | --- |\n");
        for (ScenicSpot s : spots) {
            sb.append("| ").append(s.getSpotName() != null ? s.getSpotName() : "");
            sb.append(" | ").append(s.getSpotType() != null ? s.getSpotType() : "");
            sb.append(" | ").append(s.getStarLevel() != null ? s.getStarLevel() + "★" : "");
            sb.append(" | ").append(s.getScore() != null ? s.getScore() : "");
            sb.append(" | ").append(s.getTicketPrice() != null ? s.getTicketPrice() + "元" : "");
            String addr = s.getAddress() != null ? s.getAddress() : "";
            sb.append(" | ").append(addr.length() > 20 ? addr.substring(0, 20) + "…" : addr);
            sb.append("|\n");
        }
        return sb.toString();
    }

    /**
     * 向前端写一个SSE事件: data: {json}\n\n
     */
    private void sendSseEvent(HttpServletResponse response, String json) throws Exception {
        response.getWriter().write("data: " + json + "\n\n");
        response.getWriter().flush();
    }

    /**
     * 通知前端流结束
     */
    private void sendSseDone(HttpServletResponse response) throws Exception {
        response.getWriter().write("data: [DONE]\n\n");
        response.getWriter().flush();
    }
}
