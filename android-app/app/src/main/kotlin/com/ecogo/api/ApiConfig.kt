package com.ecogo.api

/**
 * API 配置
 * 后端服务器配置
 */
object ApiConfig {
    /**
     * 基础 URL
     * 
     * 使用说明：
     * - 模拟器访问本机: http://10.0.2.2:8090/
     * - 真实设备访问本机: http://192.168.x.x:8090/ (替换为你的电脑IP)
     * - 生产环境: https://your-domain.com/
     */
    const val BASE_URL = "http://10.0.2.2:8090/"
    
    /**
     * API 版本
     */
    const val API_VERSION = "v1"
    
    /**
     * 完整的 API 路径
     */
    const val API_PATH = "api/$API_VERSION/"
    
    /**
     * 连接超时时间（秒）
     */
    const val CONNECT_TIMEOUT = 30L
    
    /**
     * 读取超时时间（秒）
     */
    const val READ_TIMEOUT = 30L
    
    /**
     * 写入超时时间（秒）
     */
    const val WRITE_TIMEOUT = 30L
}
