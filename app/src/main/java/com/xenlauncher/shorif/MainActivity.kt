package com.xenlauncher.shorif

import android.app.SearchManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var ivWallpaper: ImageView
    private lateinit var tvBattery: TextView
    private lateinit var tvStorage: TextView
    private lateinit var tvYearProgress: TextView
    private lateinit var progressBarYear: ProgressBar
    private lateinit var rvAppDrawer: RecyclerView

    private val pickWallpaperLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            ivWallpaper.setImageURI(it)
            applyBlurEffect()
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            tvBattery.text = "$level%"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ivWallpaper = findViewById(R.id.ivWallpaper)
        tvBattery = findViewById(R.id.tvBattery)
        tvStorage = findViewById(R.id.tvStorage)
        tvYearProgress = findViewById(R.id.tvYearProgress)
        progressBarYear = findViewById(R.id.progressBarYear)
        rvAppDrawer = findViewById(R.id.rvAppDrawer)

        findViewById<android.view.View>(R.id.layoutSearchBar).setOnClickListener {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, "")
            }
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnPickWallpaper).setOnClickListener {
            pickWallpaperLauncher.launch("image/*")
        }

        setupStorageInfo()
        setupYearProgress()
        loadInstalledApps()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(batteryReceiver)
    }

    private fun applyBlurEffect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blur = RenderEffect.createBlurEffect(30f, 30f, Shader.TileMode.CLAMP)
            ivWallpaper.setRenderEffect(blur)
        }
    }

    private fun setupStorageInfo() {
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val bytesAvailable = stat.availableBlocksLong * stat.blockSizeLong
            val gigaAvailable = bytesAvailable / (1024 * 1024 * 1024)
            tvStorage.text = "$gigaAvailable GB Free"
        } catch (e: Exception) {
            tvStorage.text = "Available"
        }
    }

    private fun setupYearProgress() {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_YEAR)
        val totalDays = if (cal.getActualMaximum(Calendar.DAY_OF_YEAR) == 366) 366 else 365
        val percent = (day * 100) / totalDays
        progressBarYear.progress = percent
        tvYearProgress.text = "This Year: $percent% passed ($day/$totalDays days)"
    }

    private fun loadInstalledApps() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val apps = pm.queryIntentActivities(intent, 0)
        val appList = mutableListOf<AppInfo>()

        for (resolveInfo in apps) {
            if (resolveInfo.activityInfo.packageName != packageName) {
                appList.add(
                    AppInfo(
                        name = resolveInfo.loadLabel(pm).toString(),
                        icon = resolveInfo.loadIcon(pm),
                        packageName = resolveInfo.activityInfo.packageName
                    )
                )
            }
        }
        appList.sortBy { it.name.lowercase() }

        rvAppDrawer.layoutManager = GridLayoutManager(this, 4)
        rvAppDrawer.adapter = AppAdapter(this, appList)
    }
}
