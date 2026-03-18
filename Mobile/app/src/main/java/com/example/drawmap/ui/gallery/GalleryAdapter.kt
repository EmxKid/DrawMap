package com.example.drawmap.ui.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.drawmap.R
import com.example.drawmap.data.model.GalleryItem

/**
 * Adapter для отображения элементов галереи
 */
class GalleryAdapter(
    private val onItemClick: (GalleryItem) -> Unit,
    private val onImageClick: (GalleryItem) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    private var items: List<GalleryItem> = emptyList()

    class GalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
        val tvTitle: TextView = view.findViewById(R.id.tvCardTitle)
        val tvSubtitle: TextView = view.findViewById(R.id.tvCardSubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_card, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvSubtitle.text = item.subtitle
        
        // Устанавливаем placeholder пока загружается изображение
        holder.ivPreview.setImageResource(R.drawable.ic_launcher_background)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        holder.ivPreview.setOnClickListener {
            onImageClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    /**
     * Обновить список элементов
     */
    fun submitList(newItems: List<GalleryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    /**
     * Получить элемент по позиции
     */
    fun getItem(position: Int): GalleryItem? {
        return items.getOrNull(position)
    }
}
