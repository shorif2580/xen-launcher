package com.xencustomizer.shorif

import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var tabWallpaperView: View
    private lateinit var tabWidgetsView: View
    private lateinit var tabGlassifyView: View
    private lateinit var tabIconsView: View

    private lateinit var ivWallpaperPreview: ImageView
    private lateinit var sbBlur: SeekBar
    private var originalBitmap: Bitmap? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                applyWallpaperEffects()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabWallpaperView = findViewById(R.id.tabWallpaperView)
        tabWidgetsView = findViewById(R.id.tabWidgetsView)
        tabGlassifyView = findViewById(R.id.tabGlassifyView)
        tabIconsView = findViewById(R.id.tabIconsView)

        ivWallpaperPreview = findViewById(R.id.ivWallpaperPreview)
        sbBlur = findViewById(R.id.sbBlur)

        setupNavigation()
        setupWallpaperTab()
        setupWidgetsTab()
        setupIconsTab()
    }

    private fun setupNavigation() {
        val btnWallpaper = findViewById<Button>(R.id.btnNavWallpaper)
        val btnWidgets = findViewById<Button>(R.id.btnNavWidgets)
        val btnGlassify = findViewById<Button>(R.id.btnNavGlassify)
        val btnIcons = findViewById<Button>(R.id.btnNavIcons)

        val navs = listOf(btnWallpaper, btnWidgets, btnGlassify, btnIcons)
        fun setTab(active: View, activeBtn: Button) {
            tabWallpaperView.visibility = View.GONE
            tabWidgetsView.visibility = View.GONE
            tabGlassifyView.visibility = View.GONE
            tabIconsView.visibility = View.GONE
            active.visibility = View.VISIBLE

            navs.forEach { it.setTextColor(Color.WHITE) }
            activeBtn.setTextColor(Color.parseColor("#38BDF8"))
        }

        btnWallpaper.setOnClickListener { setTab(tabWallpaperView, btnWallpaper) }
        btnWidgets.setOnClickListener { setTab(tabWidgetsView, btnWidgets) }
        btnGlassify.setOnClickListener { setTab(tabGlassifyView, btnGlassify) }
        btnIcons.setOnClickListener { setTab(tabIconsView, btnIcons) }
    }

    private fun setupWallpaperTab() {
        findViewById<Button>(R.id.btnSelectPhoto).setOnClickListener {
            pickImage.launch("image/*")
        }

        sbBlur.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                applyWallpaperEffects()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        findViewById<Button>(R.id.btnSetWallpaper).setOnClickListener {
            originalBitmap?.let { bmp ->
                try {
                    val wm = WallpaperManager.getInstance(this)
                    val result = getProcessedBitmap(bmp, sbBlur.progress)
                    wm.setBitmap(result)
                    Toast.makeText(this, "Wallpaper applied successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error setting wallpaper: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyWallpaperEffects() {
        originalBitmap?.let { bmp ->
            val processed = getProcessedBitmap(bmp, sbBlur.progress)
            ivWallpaperPreview.setImageBitmap(processed)
        }
    }

    private fun getProcessedBitmap(src: Bitmap, blurRadius: Int): Bitmap {
        val scaled = Bitmap.createScaledBitmap(src, (src.width / 4).coerceAtLeast(10), (src.height / 4).coerceAtLeast(10), true)
        val result = Bitmap.createScaledBitmap(scaled, src.width, src.height, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            color = Color.parseColor("#33000000")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, result.width.toFloat(), result.height.toFloat(), paint)
        return result
    }

    private fun setupWidgetsTab() {
        findViewById<Button>(R.id.btnAddClockWidget).setOnClickListener {
            requestPinWidget(GlassClockWidget::class.java)
        }
        findViewById<Button>(R.id.btnAddStatsWidget).setOnClickListener {
            requestPinWidget(GlassStatsWidget::class.java)
        }
        findViewById<Button>(R.id.btnAddSearchWidget).setOnClickListener {
            requestPinWidget(GlassSearchWidget::class.java)
        }
        findViewById<Button>(R.id.btnAddDockWidget).setOnClickListener {
            requestPinWidget(GlassifyDockWidget::class.java)
        }
    }

    private fun requestPinWidget(cls: Class<*>) {
        val manager = AppWidgetManager.getInstance(this)
        if (manager.isRequestPinAppWidgetSupported) {
            val provider = ComponentName(this, cls)
            manager.requestPinAppWidget(provider, null, null)
            Toast.makeText(this, "Pinned widget to your Home Screen!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Add widget manually from your home screen widget menu", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupIconsTab() {
        val rv = findViewById<RecyclerView>(R.id.rvApps)
        rv.layoutManager = LinearLayoutManager(this)

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val apps = pm.queryIntentActivities(intent, 0)
        val appList = mutableListOf<AppModel>()

        for (resolve in apps) {
            if (resolve.activityInfo.packageName != packageName) {
                appList.add(
                    AppModel(
                        name = resolve.loadLabel(pm).toString(),
                        icon = resolve.loadIcon(pm),
                        packageName = resolve.activityInfo.packageName
                    )
                )
            }
        }
        appList.sortBy { it.name.lowercase() }
        rv.adapter = AppAdapter(this, appList)
    }
}
