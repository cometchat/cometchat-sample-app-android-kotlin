package com.inscripts.cometchatpulse.Utils

import android.content.Context
import android.text.format.DateFormat
import com.inscripts.cometchatpulse.R
import java.text.SimpleDateFormat
import java.util.*

class DateUtil {

    companion object {


        fun getLastSeenDate(timeStamp: Long, context: Context): String {

            val lastSeenTime = java.text.SimpleDateFormat("HH:mm a").format(java.util.Date(timeStamp))
            val lastSeenDate = java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date(timeStamp))

            val currentTimeStamp = System.currentTimeMillis()

            val diffTimeStamp = (currentTimeStamp - timeStamp) / 1000

            return if (diffTimeStamp < 24 * 60 * 60) {

                context.getString(R.string.today_last_seen) + " " + lastSeenTime

            } else if (diffTimeStamp < 48 * 60 * 60) {

                context.getString(R.string.yesterday_last_seen) + " " + lastSeenTime
            } else {
                "Last seen at $lastSeenDate on $lastSeenTime"
            }

        }

        fun getDateId(var0: Long): String {
            val var2 = Calendar.getInstance(Locale.ENGLISH)
            var2.timeInMillis = var0
            return DateFormat.format("ddMMyyyy", var2).toString()
        }

        fun getCustomizeDate(time: Long): String {
            val monthNames = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

            var date = ""
            val cal = Calendar.getInstance()
            cal.timeInMillis = time
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)

            date = day.toString() + " " + monthNames[month] + ", " + year

            return date
        }


        fun convertTimeStampToDurationTime(var0: Long): String {
            val var2 = var0 / 1000L
            val var4 = var2 / 60L % 60L
            val var6 = var2 / 60L / 60L % 24L
            return if (var6 == 0L) String.format(Locale.getDefault(), "%02d:%02d", var4, var2 % 60L) else String.format(Locale.getDefault(), "%02d:%02d:%02d", var6, var4, var2 % 60L)
        }

        fun getMessageTime(time: String, dateFormat: String): String {
            val format = SimpleDateFormat(dateFormat)
            val timestamp = java.sql.Timestamp.valueOf(time)
            return format.format(timestamp)
        }
    }


}