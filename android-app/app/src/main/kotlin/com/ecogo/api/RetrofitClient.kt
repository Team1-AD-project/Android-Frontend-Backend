package com.ecogo.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 客户端单例
 */
object RetrofitClient {
    
    /**
     * OkHttp 客户端
     * 优化：仅在 Debug 模式启用日志，且使用 BASIC 级别提升性能
     */
    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
        
        // 添加日志拦截器（开发环境）
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Changed to BODY to see full response/request details for debugging
        }
        builder.addInterceptor(loggingInterceptor)

        // 添加认证拦截器
        builder.addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
            
            // 自动注入 Token
            val token = com.ecogo.auth.TokenManager.getToken()
            if (!token.isNullOrEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            
            val request = requestBuilder.build()

            // Execute request
            val response = chain.proceed(request)
            
            // Check for 401 Unauthorized
            if (response.code == 401) {
                // Token is invalid/expired
                // Clear token and logout
                com.ecogo.auth.TokenManager.logout()
            }
            
            response
        }
        
        builder.build()
    }
    
    /**
     * Retrofit 实例
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * API 服务实例
     */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
