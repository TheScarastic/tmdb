package com.abhishek.tmdb

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import info.movito.themoviedbapi.model.MovieDb

class TmdbMoviesAdapter(private var movieDb: ArrayList<MovieDb>, context: Context?) :
    RecyclerView.Adapter<TmdbMoviesAdapter.MyviewHolder>() {
    val mContext = context

    inner class MyviewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var thumbnail: ImageView = view.findViewById(R.id.thumbnail)
        private var title: TextView = view.findViewById(R.id.title)
        private var rating: TextView = view.findViewById(R.id.rating)
        private val ratingText: String? = mContext?.getString(R.string.rating)

        fun load(movieDb: MovieDb) {
            title.text = movieDb.title
            rating.text = (ratingText + movieDb.voteAverage.toString())
            Glide.with(itemView)
                .load("https://www.themoviedb.org/t/p/w600_and_h900_bestv2" + movieDb.posterPath)
                .override(500, 500)
                .into(thumbnail)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TmdbMoviesAdapter.MyviewHolder {
        return MyviewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.tmdb_item, parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return movieDb.size
    }

    override fun onBindViewHolder(holder: MyviewHolder, position: Int) {
        holder.load(movieDb[position])
    }
}