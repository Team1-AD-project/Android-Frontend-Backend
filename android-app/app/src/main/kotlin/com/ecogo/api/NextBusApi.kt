package com.ecogo.api

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.converter.gson.GsonConverterFactory




// ====== 1) Retrofit Service ======

interface NextBusApi {
    @GET("ShuttleService")
    suspend fun getShuttleService(
        @Query("busstopname") busStopCode: String
    ): ShuttleServiceResponse

    @GET("ServiceDescription")
    suspend fun getServiceDescription(): ServiceDescriptionResponse
}

// ====== 2) Response Models（只建我们用到的字段） ======

data class ShuttleServiceResponse(
    val ShuttleServiceResult: ShuttleServiceResult?
)

data class ShuttleServiceResult(
    val name: String?, // stop name
    val shuttles: List<Shuttle>?
)

data class Shuttle(
    val name: String?, // route name, e.g. D1/K/R1
    val _etas: List<Eta>?
)

data class Eta(
    val eta: Int?,      // minutes
    val plate: String?  // bus plate
)

data class ServiceDescriptionResponse(
    val ServiceDescriptionResult: ServiceDescriptionResult?
)

data class ServiceDescriptionResult(
    val ServiceDescription: List<RouteServiceDescription>?
)

data class RouteServiceDescription(
    val Route: String?,              // e.g. "D1"
    val RouteDescription: String?,   // e.g. "SOC > BIZ > IT > UT > CLB > BIZ > SOC"
    val RouteLongName: String?
)



// ====== 3) Client ======

object NextBusApiClient {


    private const val USERNAME = "NUSnextbus"
    private const val PASSWORD = "13dL?zY,3feWR^\"T"

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("accept", "application/json")
            .addHeader("Authorization", Credentials.basic(USERNAME, PASSWORD))
            .build()
        chain.proceed(request)
    }

    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val api: NextBusApi = Retrofit.Builder()
        .baseUrl("https://nnextbus.nus.edu.sg/")
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NextBusApi::class.java)
}
