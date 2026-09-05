package com.tourism.controller;

import com.tourism.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * AI旅游助手Controller
 * 所有接口需要token鉴权
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    /**
     * AI对话(SSE流式, 逐字输出)
     * POST /api/ai/chat
     * @param body { message: 本次提问, history: [{role, content}]最近几轮对话 }
     * @return SSE流: data: {"content":"..."} ... data: [DONE]
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SuppressWarnings("unchecked")
    public void chat(@RequestBody Map<String, Object> body, HttpServletResponse response) {
        String message = (String) body.get("message");
        List<Map<String, String>> history = (List<Map<String, String>>) body.get("history");
        aiService.chatStream(message, history, response);
    }
}
