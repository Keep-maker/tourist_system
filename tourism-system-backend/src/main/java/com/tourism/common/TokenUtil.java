package com.tourism.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简易Token工具类
 * 使用内存HashMap存储Token，无需Redis，实训项目轻量化实现
 * 
 * 功能:
 * - 生成Token: 根据管理员ID生成唯一token
 * - 校验Token: 检查token是否存在且未过期
 * - 删除Token: 登出时移除token
 */
@Component
public class TokenUtil {

    /**
     * token前缀
     */
    private static final String TOKEN_PREFIX = "tk_";

    /**
     * token有效期(毫秒)，默认2小时
     */
    @Value("${app.token-expire-hours:2}")
    private int tokenExpireHours;

    /**
     * token -> 管理员ID 映射
     */
    private final Map<String, Integer> tokenAdminMap = new ConcurrentHashMap<>();

    /**
     * token -> 过期时间戳 映射
     */
    private final Map<String, Long> tokenExpireMap = new ConcurrentHashMap<>();

    /**
     * 计数器，用于生成唯一token
     */
    private final AtomicInteger counter = new AtomicInteger(0);

    /**
     * 初始化时计算过期时间(毫秒)
     */
    private long expireMillis;

    @PostConstruct
    public void init() {
        this.expireMillis = tokenExpireHours * 60 * 60 * 1000L;
    }

    /**
     * 生成Token
     * @param adminId 管理员ID
     * @return 生成的token字符串
     */
    public String generateToken(Integer adminId) {
        // 移除该管理员之前的所有token(同一设备只保留一个登录态)
        removeByAdminId(adminId);
        
        // 生成唯一token: tk_时间戳_计数器
        String token = TOKEN_PREFIX + System.currentTimeMillis() + "_" + counter.incrementAndGet();
        
        // 存储token关联的管理员ID和过期时间
        tokenAdminMap.put(token, adminId);
        tokenExpireMap.put(token, System.currentTimeMillis() + expireMillis);
        
        return token;
    }

    /**
     * 校验Token是否有效
     * @param token token字符串
     * @return true: 有效  false: 无效
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // 检查token是否存在
        if (!tokenAdminMap.containsKey(token)) {
            return false;
        }
        
        // 检查是否过期
        Long expireTime = tokenExpireMap.get(token);
        if (expireTime == null || System.currentTimeMillis() > expireTime) {
            // 过期则自动清理
            tokenAdminMap.remove(token);
            tokenExpireMap.remove(token);
            return false;
        }
        
        return true;
    }

    /**
     * 根据Token获取管理员ID
     * @param token token字符串
     * @return 管理员ID，token无效时返回null
     */
    public Integer getAdminIdByToken(String token) {
        if (!validateToken(token)) {
            return null;
        }
        return tokenAdminMap.get(token);
    }

    /**
     * 删除Token(登出)
     * @param token token字符串
     */
    public void removeToken(String token) {
        tokenAdminMap.remove(token);
        tokenExpireMap.remove(token);
    }

    /**
     * 根据管理员ID删除所有Token(强制下线)
     * @param adminId 管理员ID
     */
    public void removeByAdminId(Integer adminId) {
        tokenAdminMap.entrySet().removeIf(entry -> entry.getValue().equals(adminId));
    }
}
