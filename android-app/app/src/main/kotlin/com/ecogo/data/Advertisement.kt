package com.ecogo.data

import com.google.gson.annotations.SerializedName

data class Advertisement(
    val id: String,
    val name: String,
    val description: String,
    val status: String,
    val startDate: String,
    val endDate: String,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("linkUrl")
    val linkUrl: String,
    val position: String,
    val impressions: Int,
    val clicks: Int,
    val clickRate: Double
)

data class AdResponse(
    val code: Int,
    val message: String,
    val data: List<Advertisement>?
)