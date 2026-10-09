package com.xencustomizer.shorif

import android.app.PendingIntent
import android.app.SearchManager
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

class GlassClockWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_glass_clock)
            manager.updateAppWidget(id, views)
        }
    }
}

class GlassStatsWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_glass_stats)
            try {
                val stat = StatFs(Environment.getDataDirectory().path)
                val freeGb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024 * 1024)
                views.setTextViewText(R.id.tvWidgetStorage, "Storage: $freeGb GB Free")
            } catch (e: Exception) {}

            try {
                val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                    context.registerReceiver(null, filter)
                }
                val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                views.setTextViewText(R.id.tvWidgetBattery, "Battery: $level%")
            } catch (e: Exception) {}

            val cal = Calendar.getInstance()
            val day = cal.get(Calendar.DAY_OF_YEAR)
            val total = if (cal.getActualMaximum(Calendar.DAY_OF_YEAR) == 366) 366 else 365
            val percent = (day * 100) / total
            views.setTextViewText(R.id.tvWidgetYear, "This Year: $percent% ($day/$total days)")
            views.setProgressBar(R.id.progressWidgetYear, 100, percent, false)

            manager.updateAppWidget(id, views)
        }
    }
}

class GlassSearchWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_glass_search)
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, "")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.btnWidgetSearch, pendingIntent)
            manager.updateAppWidget(id, views)
        }
    }
}

class GlassifyDockWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_glassify_dock)
            manager.updateAppWidget(id, views)
        }
    }
}
