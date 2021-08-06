package com.abhishek.tmdb

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abhishek.tmdb.databinding.TmdbItemBinding
import com.bumptech.glide.Glide
import info.movito.themoviedbapi.model.tv.TvSeries

class TmdbTvAdapter(private var tvDb: ArrayList<TvSeries>, context: Context) :
    RecyclerView.Adapter<TmdbTvAdapter.TvHolder>() {
    private val mContext = context

    class TvHolder(private val binding: TmdbItemBinding, context: Context) :
        RecyclerView.ViewHolder(binding.root) {

        private val ratingText: String = context.getString(R.string.rating)

        fun load(tvDb: TvSeries) {
            binding.title.text = tvDb.name
            binding.rating.text = (ratingText + tvDb.voteAverage.toString())
            Glide.with(itemView)
                .load("https://www.themoviedb.org/t/p/w600_and_h900_bestv2" + tvDb.posterPath)
                .placeholder(R.drawable.ic_broken_image)
                .override(450, 500)
                .into(binding.thumbnail)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TvHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TmdbItemBinding.inflate(inflater, parent, false)
        return TvHolder(binding, mContext)
    }

    override fun getItemCount(): Int {
        return tvDb.size
    }

    override fun onBindViewHolder(holder: TvHolder, position: Int) {
        holder.load(tvDb[position])
    }
}