package com.ecogo.app.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 网络客户端单例
 */
object RetrofitClient {

    // 后端服务器地址
    private const val BASE_URL = "http://47.129.124.55:8090/api/v1/"  // EcoGo服务器

    // 其他环境地址
    // private const val BASE_URL = "http://10.0.2.2:8090/api/v1/"  // 本地开发（模拟器）
    // private const val BASE_URL = "http://localhost:8090/api/v1/"  // 本地开发（真机）

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                // 可添加 Token 认证头
                // .header("Authorization", "Bearer ${TokenManager.getToken()}")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    /**
     * 行程API服务实例
     */
    val tripApiService: TripApiService = retrofit.create(TripApiService::class.java)
}
