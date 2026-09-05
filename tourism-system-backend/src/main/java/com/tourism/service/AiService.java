package com.tourism.service;

import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

/**
 * AI旅游助手Service
 * 基于智谱GLM-4-Flash大模型(OpenAI兼容协议), 结合系统景点数据回答用户问题
 */
public interface AiService {

    /**
     * 流式对话(SSE): 结合数据库热门景点数据回答, 逐字推送给前端
     * @param message 用户本次提问
     * @param history 历史对话 [{role:"user"/"assistant", content:"..."}](前端保留最近几轮)
     * @param response HTTP响应对象(用于向客户端写SSE流)
     */
    void chatStream(String message, List<Map<String, String>> history, HttpServletResponse response);
}
