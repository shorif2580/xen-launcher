package com.xencustomizer.shorif

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.widget.RemoteViews
import java.util.Calendar

class LargeClockWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_large_clock)
            manager.updateAppWidget(id, views)
        }
    }
}

class StatsCardWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_stats_card)
            try {
                val stat = StatFs(Environment.getDataDirectory().path)
                val freeGb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024 * 1024)
                views.setTextViewText(R.id.tvWStorage, "Storage: $freeGb GB Free")
            } catch (e: Exception) {}

            try {
                val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryStatus = context.registerReceiver(null, filter)
                val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                views.setTextViewText(R.id.tvWBattery, "Battery: $level%")
            } catch (e: Exception) {}

            val cal = Calendar.getInstance()
            val day = cal.get(Calendar.DAY_OF_YEAR)
            val total = if (cal.getActualMaximum(Calendar.DAY_OF_YEAR) == 366) 366 else 365
            val percent = (day * 100) / total
            views.setTextViewText(R.id.tvWYear, "This Year: $percent% ($day/$total days)")
            views.setProgressBar(R.id.pbWYear, 100, percent, false)

            manager.updateAppWidget(id, views)
        }
    }
}

class DockWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_dock)
            manager.updateAppWidget(id, views)
        }
    }
}

class CalendarWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_calendar)
            manager.updateAppWidget(id, views)
        }
    }
}
