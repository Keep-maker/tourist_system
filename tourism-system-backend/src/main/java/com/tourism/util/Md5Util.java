package com.tourism.util;

import cn.hutool.crypto.digest.MD5;

/**
 * MD5加密工具类
 * 使用Hutool的MD5实现
 */
public class Md5Util {

    /**
     * 私有构造方法，禁止实例化
     */
    private Md5Util() {
    }

    /**
     * MD5加密
     * @param input 待加密字符串
     * @return MD5加密后的32位十六进制字符串
     */
    public static String encrypt(String input) {
        if (input == null) {
            return null;
        }
        return MD5.create().digestHex(input);
    }

    /**
     * MD5加密并转换为大写
     * @param input 待加密字符串
     * @return MD5加密后的32位大写十六进制字符串
     */
    public static String encryptUpper(String input) {
        if (input == null) {
            return null;
        }
        return MD5.create().digestHex16(input).toUpperCase();
    }

    /**
     * 验证密码是否匹配
     * @param input 原始密码
     * @param encrypted 已加密密码
     * @return 是否匹配
     */
    public static boolean verify(String input, String encrypted) {
        if (input == null || encrypted == null) {
            return false;
        }
        return encrypt(input).equals(encrypted);
    }
}
