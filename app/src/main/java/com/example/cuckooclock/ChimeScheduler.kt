package com.example.cuckooclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ChimeScheduler {

    const val EXTRA_IS_HALF_HOUR = "is_half_hour"
    const val EXTRA_HOUR_COUNT = "hour_count"

    fun scheduleNextChime(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val now = Calendar.getInstance()
        val minute = now.get(Calendar.MINUTE)
        val next = Calendar.getInstance()

        // Determine next chime time
        if (minute < 30) {
            // next chime at :30
            next.set(Calendar.MINUTE, 30)
            next.set(Calendar.SECOND, 0)
            next.set(Calendar.MILLISECOND, 0)
        } else {
            // next chime at :00 (next hour)
            next.add(Calendar.HOUR_OF_DAY, 1)
            next.set(Calendar.MINUTE, 0)
            next.set(Calendar.SECOND, 0)
            next.set(Calendar.MILLISECOND, 0)
        }

        val isHalf = next.get(Calendar.MINUTE) == 30
        val hourCount = if (isHalf) 0 else {
            val h = next.get(Calendar.HOUR)
            if (h == 0) 12 else h
        }

        val intent = Intent(context, ChimeReceiver::class.java).apply {
            putExtra(EXTRA_IS_HALF_HOUR, isHalf)
            putExtra(EXTRA_HOUR_COUNT, hourCount)
        }

        val pi = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    next.timeInMillis,
                    pi
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    next.timeInMillis,
                    pi
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                next.timeInMillis,
                pi
            )
        }
    }

    fun cancelChimes(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ChimeReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pi?.let { alarmManager.cancel(it) }
    }
}
