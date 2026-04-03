package com.cometchat.uikit.kotlin.shared.interfaces

interface DateTimeFormatterCallback {
    fun time(timestamp: Long): String? = null
    fun today(timestamp: Long): String? = null
    fun yesterday(timestamp: Long): String? = null
    fun lastWeek(timestamp: Long): String? = null
    fun otherDays(timestamp: Long): String? = null
    fun minute(timestamp: Long): String? = null
    fun minutes(diffInMinutesFromNow: Long, timestamp: Long): String? = null
    fun hour(timestamp: Long): String? = null
    fun hours(diffInHourFromNow: Long, timestamp: Long): String? = null
}