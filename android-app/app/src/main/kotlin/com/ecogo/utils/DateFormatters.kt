package com.ecogo.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatters {

    private val outFmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

    // 支持：2026-03-06T17:14:29.577 / 2026-03-06T17:14:29 / 2026-03-06T17:14:29.5
    private val isoLocalDateTimeFmt =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]", Locale.ENGLISH)

    private val isoLocalDateFmt =
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)

    fun formatExpiry(raw: String?): String {
        if (raw.isNullOrBlank()) return ""

        // 1) 带时区：2026-03-06T17:14:29.577Z / +08:00
        try {
            val dt = OffsetDateTime.parse(raw)
            return dt.toLocalDate().format(outFmt)
        } catch (_: Exception) {}

        // 2) 不带时区：2026-03-06T17:14:29.577
        try {
            val dt = LocalDateTime.parse(raw, isoLocalDateTimeFmt)
            return dt.toLocalDate().format(outFmt)
        } catch (_: Exception) {}

        // 3) 纯日期：2026-03-06
        try {
            val d = LocalDate.parse(raw, isoLocalDateFmt)
            return d.format(outFmt)
        } catch (_: Exception) {}

        // 4) 斜杠：2026/03/31
        try {
            val inFmt = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ENGLISH)
            val d = LocalDate.parse(raw, inFmt)
            return d.format(outFmt)
        } catch (_: Exception) {}

        return raw
    }
}
