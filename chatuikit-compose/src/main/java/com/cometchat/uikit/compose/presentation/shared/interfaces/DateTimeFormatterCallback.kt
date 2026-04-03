package com.cometchat.uikit.compose.presentation.shared.interfaces

interface DateTimeFormatterCallback {
    fun time(timestamp: Long): String? {
        return null
    }

    fun today(timestamp: Long): String? {
        return null
    }

    fun yesterday(timestamp: Long): String? {
        return null
    }

    fun lastWeek(timestamp: Long): String? {
        return null
    }

    fun otherDays(timestamp: Long): String? {
        return null
    }

    fun minute(timestamp: Long): String? {
        return null
    }

    fun minutes(diffInMinutesFromNow: Long, timestamp: Long): String? {
        return null
    }

    fun hour(timestamp: Long): String? {
        return null
    }

    fun hours(diffInHourFromNow: Long, timestamp: Long): String? {
        return null
    }
}
