package com.abhishek.tmdb

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import info.movito.themoviedbapi.model.Artwork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ScreenshotAdapter(context: Context?, art: List<Artwork>) :
    RecyclerView.Adapter<ScreenshotAdapter.ScreenshotHolder>(), CoroutineScope by CoroutineScope(
    Dispatchers.IO
) {
    private var mArtwork = art

    inner class ScreenshotHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var ssImageview: ImageView = view.findViewById(R.id.screenshot_images)

        fun load(artPath: String) {
            Glide.with(itemView)
                .load("https://www.themoviedb.org/t/p/original$artPath")
                .placeholder(R.drawable.ic_broken_image)
                .override(1600, 800)
                .into(ssImageview)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScreenshotAdapter.ScreenshotHolder {


        return ScreenshotHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.tmdb_screenshot, parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mArtwork.size
    }

    override fun onBindViewHolder(holder: ScreenshotHolder, position: Int) {
        holder.load(mArtwork[position].filePath)
    }
}