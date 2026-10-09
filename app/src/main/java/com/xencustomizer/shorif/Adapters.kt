package com.xencustomizer.shorif

import android.appwidget.AppWidgetManager
import android.content.ComponentName
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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

data class WidgetItem(
    val title: String,
    val subtitle: String,
    val layoutResId: Int,
    val providerClass: Class<*>
)

class WidgetCatalogAdapter(
    private val context: Context,
    private val list: List<WidgetItem>
) : RecyclerView.Adapter<WidgetCatalogAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val container: FrameLayout = v.findViewById(R.id.containerPreview)
        val title: TextView = v.findViewById(R.id.tvWidgetTitle)
        val subtitle: TextView = v.findViewById(R.id.tvWidgetSubtitle)
        val btnPin: Button = v.findViewById(R.id.btnPinWidget)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_widget_card, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.title.text = item.title
        holder.subtitle.text = item.subtitle
        holder.container.removeAllViews()
        LayoutInflater.from(context).inflate(item.layoutResId, holder.container, true)

        holder.btnPin.setOnClickListener {
            val manager = AppWidgetManager.getInstance(context)
            if (manager.isRequestPinAppWidgetSupported) {
                val provider = ComponentName(context, item.providerClass)
                manager.requestPinAppWidget(provider, null, null)
                Toast.makeText(context, "Added ${item.title} to Home Screen!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please add widget from your launcher menu", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun getItemCount() = list.size
}

data class AppModel(val name: String, val icon: Drawable, val packageName: String)

class IconCatalogAdapter(
    private val context: Context,
    private val list: List<AppModel>
) : RecyclerView.Adapter<IconCatalogAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val ivIcon: ImageView = v.findViewById(R.id.ivIconPreview)
        val tvName: TextView = v.findViewById(R.id.tvIconName)
        val btnPin: Button = v.findViewById(R.id.btnPinIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_icon_theme, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.tvName.text = item.name
        holder.ivIcon.setImageDrawable(item.icon)

        holder.btnPin.setOnClickListener {
            val sm = context.getSystemService(ShortcutManager::class.java)
            if (sm != null && sm.isRequestPinShortcutSupported) {
                val bmp = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)

                val paint = Paint().apply {
                    color = Color.parseColor("#33FFFFFF")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawRoundRect(0f, 0f, 128f, 128f, 32f, 32f, paint)

                val stroke = Paint().apply {
                    color = Color.parseColor("#80FFFFFF")
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                    isAntiAlias = true
                }
                canvas.drawRoundRect(2f, 2f, 126f, 126f, 32f, 32f, stroke)

                item.icon.setBounds(24, 24, 104, 104)
                item.icon.draw(canvas)

                val launchIntent = context.packageManager.getLaunchIntentForPackage(item.packageName)
                if (launchIntent != null) {
                    val info = ShortcutInfo.Builder(context, item.packageName + System.currentTimeMillis())
                        .setIcon(Icon.createWithBitmap(bmp))
                        .setShortLabel(item.name)
                        .setIntent(launchIntent)
                        .build()
                    sm.requestPinShortcut(info, null)
                    Toast.makeText(context, "${item.name} glass icon added!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Pin shortcut not supported", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = list.size
}
