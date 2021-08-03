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
import info.movito.themoviedbapi.model.Multi
import info.movito.themoviedbapi.model.tv.TvSeries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class SearchAdapter(context: Context?, searchList: List<Multi>) :
    RecyclerView.Adapter<SearchAdapter.MyViewHolder>(), CoroutineScope by CoroutineScope(
    Dispatchers.IO
) {
    private var mSearchList = searchList
    private var mContext = context

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var searchPoster: ImageView = view.findViewById(R.id.search_poster)
        private var searchTitle: TextView = view.findViewById(R.id.search_title)
        private var searchType: TextView = view.findViewById(R.id.search_type)

        fun load(searchList: Multi) {
            val title: String?
            val poster: String?
            if (searchList.mediaType == Multi.MediaType.MOVIE) {
                title = (searchList as MovieDb).title
                poster = searchList.posterPath
                searchType.text = mContext?.getString(R.string.search_movie)
                mContext?.let { searchType.setTextColor(it.getColor(R.color.orange)) }
            } else if (searchList.mediaType == Multi.MediaType.TV_SERIES) {
                title = (searchList as TvSeries).name
                poster = searchList.posterPath
                searchType.text = mContext?.getString(R.string.search_show)
                mContext?.let { searchType.setTextColor(it.getColor(R.color.green)) }
            } else {
                return
            }
            searchTitle.text = title
            Glide.with(itemView)
                .load("https://www.themoviedb.org/t/p/w300_and_h450_bestv2$poster")
                .placeholder(R.drawable.ic_broken_image)
                .override(300, 450)
                .into(searchPoster)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchAdapter.MyViewHolder {


        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.tmdb_search, parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mSearchList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.load(mSearchList.get(position))
    }
}
