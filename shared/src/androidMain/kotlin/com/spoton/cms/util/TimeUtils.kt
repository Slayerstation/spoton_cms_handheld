package com.spoton.cms.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun getCurrentTimeMillis(): Long {
    return System.currentTimeMillis()
}

actual fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
