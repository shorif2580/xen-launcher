package com.xencustomizer.shorif

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var viewTabWidgets: View
    private lateinit var viewTabWallpaper: View
    private lateinit var viewTabIcons: View

    private lateinit var btnNavWidgets: Button
    private lateinit var btnNavWallpaper: Button
    private lateinit var btnNavIcons: Button

    private lateinit var ivStudioPreview: ImageView
    private lateinit var sbBlur: SeekBar
    private lateinit var sbDim: SeekBar
    private lateinit var cbClockOverlay: CheckBox
    private var baseBitmap: Bitmap? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                baseBitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                renderWallpaperPreview()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewTabWidgets = findViewById(R.id.viewTabWidgets)
        viewTabWallpaper = findViewById(R.id.viewTabWallpaper)
        viewTabIcons = findViewById(R.id.viewTabIcons)

        btnNavWidgets = findViewById(R.id.btnNavWidgets)
        btnNavWallpaper = findViewById(R.id.btnNavWallpaper)
        btnNavIcons = findViewById(R.id.btnNavIcons)

        ivStudioPreview = findViewById(R.id.ivStudioPreview)
        sbBlur = findViewById(R.id.sbBlur)
        sbDim = findViewById(R.id.sbDim)
        cbClockOverlay = findViewById(R.id.cbClockOverlay)

        setupNavigation()
        setupWidgetsCatalog()
        setupWallpaperStudio()
        setupIconsCatalog()
    }

    private fun setupNavigation() {
        fun selectTab(activeView: View, activeBtn: Button) {
            viewTabWidgets.visibility = View.GONE
            viewTabWallpaper.visibility = View.GONE
            viewTabIcons.visibility = View.GONE
            activeView.visibility = View.VISIBLE

            btnNavWidgets.setTextColor(Color.parseColor("#80FFFFFF"))
            btnNavWallpaper.setTextColor(Color.parseColor("#80FFFFFF"))
            btnNavIcons.setTextColor(Color.parseColor("#80FFFFFF"))
            activeBtn.setTextColor(Color.parseColor("#38BDF8"))
        }

        btnNavWidgets.setOnClickListener { selectTab(viewTabWidgets, btnNavWidgets) }
        btnNavWallpaper.setOnClickListener { selectTab(viewTabWallpaper, btnNavWallpaper) }
        btnNavIcons.setOnClickListener { selectTab(viewTabIcons, btnNavIcons) }
    }

    private fun setupWidgetsCatalog() {
        val rv = findViewById<RecyclerView>(R.id.rvWidgetsList)
        rv.layoutManager = LinearLayoutManager(this)

        val widgetList = listOf(
            WidgetItem("Large Bold Typography Clock", "4x2 • Minimal Glass", R.layout.widget_large_clock, LargeClockWidget::class.java),
            WidgetItem("Battery, RAM & Storage Ring", "4x2 • Live Device Stats", R.layout.widget_stats_card, StatsCardWidget::class.java),
            WidgetItem("Frosted Glass Dock Plate", "4x1 • App Container Dock", R.layout.widget_dock, DockWidget::class.java),
            WidgetItem("Monthly Calendar Glass Card", "4x2 • Full Month View", R.layout.widget_calendar, CalendarWidget::class.java)
        )
        rv.adapter = WidgetCatalogAdapter(this, widgetList)
    }

    private fun setupWallpaperStudio() {
        findViewById<Button>(R.id.btnPickImage).setOnClickListener {
            pickImage.launch("image/*")
        }

        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                renderWallpaperPreview()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        }
        sbBlur.setOnSeekBarChangeListener(listener)
        sbDim.setOnSeekBarChangeListener(listener)
        cbClockOverlay.setOnCheckedChangeListener { _, _ -> renderWallpaperPreview() }

        findViewById<Button>(R.id.btnApplyWallpaper).setOnClickListener {
            baseBitmap?.let { bmp ->
                try {
                    val finalWallpaper = generateCustomWallpaper(bmp, sbBlur.progress, sbDim.progress, cbClockOverlay.isChecked)
                    val wm = WallpaperManager.getInstance(this)
                    wm.setBitmap(finalWallpaper)
                    Toast.makeText(this, "Wallpaper set to Home & Lock screen!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, "Please choose an image first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderWallpaperPreview() {
        baseBitmap?.let { bmp ->
            val preview = generateCustomWallpaper(bmp, sbBlur.progress, sbDim.progress, cbClockOverlay.isChecked)
            ivStudioPreview.setImageBitmap(preview)
        }
    }

    private fun generateCustomWallpaper(src: Bitmap, blur: Int, dimPercent: Int, withClock: Boolean): Bitmap {
        val factor = (blur.coerceAtLeast(1) * 2).coerceAtMost(30)
        val w = (src.width / factor).coerceAtLeast(10)
        val h = (src.height / factor).coerceAtLeast(10)
        val small = Bitmap.createScaledBitmap(src, w, h, true)
        val result = Bitmap.createScaledBitmap(small, src.width, src.height, true)

        val canvas = Canvas(result)

        // ডার্কেন ওভারলে
        val alpha = ((dimPercent / 100f) * 255).toInt().coerceIn(0, 255)
        val dimPaint = Paint().apply {
            color = Color.argb(alpha, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, result.width.toFloat(), result.height.toFloat(), dimPaint)

        // ওয়ালপেপারের উপর বড় ঘড়ি বসানো
        if (withClock) {
            val clockPaint = Paint().apply {
                color = Color.WHITE
                textSize = result.width * 0.18f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                setShadowLayer(16f, 0f, 4f, Color.parseColor("#99000000"))
                isAntiAlias = true
            }
            val timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val clockY = result.height * 0.22f
            canvas.drawText(timeText, result.width / 2f, clockY, clockPaint)

            val datePaint = Paint().apply {
                color = Color.parseColor("#E6FFFFFF")
                textSize = result.width * 0.05f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                setShadowLayer(8f, 0f, 2f, Color.parseColor("#99000000"))
                isAntiAlias = true
            }
            val dateText = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
            canvas.drawText(dateText, result.width / 2f, clockY + (result.width * 0.08f), datePaint)
        }
        return result
    }

    private fun setupIconsCatalog() {
        val rv = findViewById<RecyclerView>(R.id.rvIconsList)
        rv.layoutManager = LinearLayoutManager(this)

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val apps = pm.queryIntentActivities(intent, 0)
        val list = mutableListOf<AppModel>()

        for (item in apps) {
            if (item.activityInfo.packageName != packageName) {
                list.add(
                    AppModel(
                        name = item.loadLabel(pm).toString(),
                        icon = item.loadIcon(pm),
                        packageName = item.activityInfo.packageName
                    )
                )
            }
        }
        list.sortBy { it.name.lowercase() }
        rv.adapter = IconCatalogAdapter(this, list)
    }
}
