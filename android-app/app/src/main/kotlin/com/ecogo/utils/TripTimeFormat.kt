package com.ecogo.utils

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object TripTimeFormat {

    // 你想显示成什么样可以改这里
    // 例：2026-02-10 12:26
    private val outFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())

    private val sgZone = ZoneId.of("Asia/Singapore")

    /**
     * 兼容两种后端时间：
     * 1) 2026-02-06T14:26:36.736+00:00 (带 offset)
     * 2) 2026-02-10T04:26:43.034       (不带 offset)
     */
    fun toSgTime(raw: String?): String {
        if (raw.isNullOrBlank()) return "--"

        return try {
            if (raw.contains("+") || raw.endsWith("Z")) {
                // 带时区 offset：转成新加坡时间显示
                OffsetDateTime.parse(raw)
                    .atZoneSameInstant(sgZone)
                    .format(outFmt)
            } else {
                // 不带 offset：按 LocalDateTime 解析（默认就当它是后端给的“本地时间”）
                LocalDateTime.parse(raw)
                    .format(outFmt)
            }
        } catch (e: Exception) {
            raw // 兜底：解析失败就原样显示
        }
    }

    fun rangeText(start: String?, end: String?): String {
        val s = toSgTime(start)
        val e = if (end.isNullOrBlank()) "Now" else toSgTime(end)
        return "$s ~ $e"
    }
}
