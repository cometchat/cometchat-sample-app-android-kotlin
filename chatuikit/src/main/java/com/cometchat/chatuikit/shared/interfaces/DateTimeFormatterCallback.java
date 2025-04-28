package com.cometchat.chatuikit.shared.interfaces;

public interface DateTimeFormatterCallback {
    default String time(long timestamp) {return null;}

    default String today(long timestamp) {return null;}

    default String yesterday(long timestamp) {return null;}

    default String lastWeek(long timestamp) {return null;}

    default String otherDays(long timestamp) {return null;}

    default String minute(long timestamp) {return null;}

    default String minutes(long diffInMinutesFromNow, long timestamp) {return null;}

    default String hour(long timestamp) {return null;}

    default String hours(long diffInHourFromNow, long timestamp) {return null;}

}
