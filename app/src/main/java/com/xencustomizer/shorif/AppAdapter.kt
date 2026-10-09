package com.xencustomizer.shorif

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

data class AppModel(val name: String, val icon: Drawable, val packageName: String)

class AppAdapter(private val context: Context, private val list: List<AppModel>) :
    RecyclerView.Adapter<AppAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val ivIcon: ImageView = v.findViewById(R.id.ivAppIcon)
        val tvName: TextView = v.findViewById(R.id.tvAppName)
        val btnApply: Button = v.findViewById(R.id.btnApplyTheme)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_theme, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.tvName.text = item.name
        holder.ivIcon.setImageDrawable(item.icon)

        holder.btnApply.setOnClickListener {
            pinGlassShortcut(item)
        }
    }

    override fun getItemCount() = list.size

    private fun pinGlassShortcut(app: AppModel) {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported) {
            val bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                color = Color.parseColor("#33FFFFFF")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawRoundRect(0f, 0f, 128f, 128f, 28f, 28f, paint)

            val stroke = Paint().apply {
                color = Color.parseColor("#80FFFFFF")
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }
            canvas.drawRoundRect(2f, 2f, 126f, 126f, 28f, 28f, stroke)

            app.icon.setBounds(24, 24, 104, 104)
            app.icon.draw(canvas)

            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                val pinShortcutInfo = ShortcutInfo.Builder(context, app.packageName + System.currentTimeMillis())
                    .setIcon(Icon.createWithBitmap(bitmap))
                    .setShortLabel(app.name)
                    .setIntent(launchIntent)
                    .build()
                shortcutManager.requestPinShortcut(pinShortcutInfo, null)
                Toast.makeText(context, "${app.name} glass icon added to Home Screen!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Pin shortcuts not supported on your launcher", Toast.LENGTH_SHORT).show()
        }
    }
}
