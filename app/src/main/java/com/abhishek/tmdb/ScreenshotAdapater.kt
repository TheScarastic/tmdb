package com.abhishek.tmdb

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ScreenshotAdapater(context: Context?, art: ArrayList<String>) :
    RecyclerView.Adapter<ScreenshotAdapater.MyviewHolder>(), CoroutineScope by CoroutineScope(
    Dispatchers.IO
) {
    private var backdrops: ArrayList<String> = art

    inner class MyviewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var ssImageview: ImageView = view.findViewById(R.id.screenshot_images)

        fun load(artPath: String) {
            Glide.with(itemView)
                .load("https://www.themoviedb.org/t/p/original$artPath")
                .override(1600, 800)
                .into(ssImageview)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScreenshotAdapater.MyviewHolder {


        return MyviewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.tmdb_screenshot, parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return backdrops.size
    }

    override fun onBindViewHolder(holder: MyviewHolder, position: Int) {
        holder.load(backdrops[position])
    }
}