package com.videoapi.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.videoapi.app.R
import com.videoapi.app.data.local.VideoEntity

class VideoAdapter(
    private var items: List<VideoEntity>,
    private val onClick: (VideoEntity) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val statusText: TextView = view.findViewById(R.id.statusText)
        val timeText: TextView = view.findViewById(R.id.timeText)
        val viewsText: TextView = view.findViewById(R.id.viewsText)
        val titleText: TextView = view.findViewById(R.id.titleText)
        val urlText: TextView = view.findViewById(R.id.urlText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        if (item.isLive) {
            holder.statusText.text = "live feed / بث مباشر"
            holder.statusText.setBackgroundColor(holder.itemView.context.getColor(R.color.live_red))
        } else {
            holder.statusText.text = "Recorded broadcast / بث مسجل"
            holder.statusText.setBackgroundColor(holder.itemView.context.getColor(R.color.recorded_gray))
        }
        holder.timeText.text = "تم ارسال هذا البث في توقيت ${item.timestamp}"
        holder.viewsText.text = "عدد المشاهدات ${item.views}"
        holder.titleText.text = item.title
        holder.urlText.text = item.url
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<VideoEntity>) {
        items = newItems
        notifyDataSetChanged()
    }
}
