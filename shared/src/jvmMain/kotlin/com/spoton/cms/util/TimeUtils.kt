package com.spoton.cms.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

actual fun getCurrentTimeMillis(): Long {
    return System.currentTimeMillis()
}

actual fun formatTimestamp(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(timestamp))
}
