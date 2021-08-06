package com.abhishek.tmdb

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abhishek.tmdb.databinding.TmdbItemBinding
import com.bumptech.glide.Glide
import info.movito.themoviedbapi.model.MovieDb

class TmdbMoviesAdapter(private var movieDb: ArrayList<MovieDb>, context: Context) :
    RecyclerView.Adapter<TmdbMoviesAdapter.MovieHolder>() {
    private val mContext = context

    class MovieHolder(private val binding: TmdbItemBinding, context: Context) :
        RecyclerView.ViewHolder(binding.root) {

        private val ratingText: String = context.getString(R.string.rating)
        fun load(movieDb: MovieDb) {
            binding.title.text = movieDb.title
            binding.rating.text = (ratingText + movieDb.voteAverage.toString())
            Glide.with(itemView)
                .load("https://www.themoviedb.org/t/p/w600_and_h900_bestv2" + movieDb.posterPath)
                .placeholder(R.drawable.ic_broken_image)
                .override(450, 500)
                .into(binding.thumbnail)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MovieHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TmdbItemBinding.inflate(inflater, parent, false)
        return MovieHolder(binding, mContext)
    }

    override fun getItemCount(): Int {
        return movieDb.size
    }

    override fun onBindViewHolder(holder: MovieHolder, position: Int) {
        holder.load(movieDb[position])
    }
}