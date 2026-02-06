package com.ecogo.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeFormat {

    // 后端返回：2026-02-06T14:26:36.736+00:00
    private val inputFormat = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        Locale.getDefault()
    ).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // 前端显示：2026-02-06 22:26
    private val outputFormat = SimpleDateFormat(
        "yyyy-MM-dd HH:mm",
        Locale.getDefault()
    ).apply {
        timeZone = TimeZone.getTimeZone("Asia/Singapore")
    }

    fun toSgTime(isoOffset: String?): String {
        if (isoOffset.isNullOrBlank()) return "-"
        return try {
            val date = inputFormat.parse(isoOffset)
            if (date != null) outputFormat.format(date) else isoOffset
        } catch (e: Exception) {
            isoOffset
        }
    }
}
