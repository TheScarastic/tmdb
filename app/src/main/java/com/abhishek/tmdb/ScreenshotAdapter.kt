package com.abhishek.tmdb

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abhishek.tmdb.databinding.TmdbScreenshotBinding
import com.bumptech.glide.Glide
import info.movito.themoviedbapi.model.Artwork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ScreenshotAdapter(art: List<Artwork>) :
    RecyclerView.Adapter<ScreenshotAdapter.ScreenshotHolder>(), CoroutineScope by CoroutineScope(
    Dispatchers.IO
) {
    private var mArtwork = art

    class ScreenshotHolder(private val binding: TmdbScreenshotBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun load(artPath: String) {
            Glide.with(itemView)
                .load("https://www.themoviedb.org/t/p/original$artPath")
                .placeholder(R.drawable.ic_broken_image)
                .override(1600, 800)
                .into(binding.screenshotImages)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScreenshotHolder {

        val inflater = LayoutInflater.from(parent.context)
        val binding = TmdbScreenshotBinding.inflate(inflater, parent, false)
        return ScreenshotHolder(binding)
    }

    override fun getItemCount(): Int {
        return mArtwork.size
    }

    override fun onBindViewHolder(holder: ScreenshotHolder, position: Int) {
        holder.load(mArtwork[position].filePath)
    }
}