package com.abhishek.tmdb

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abhishek.tmdb.databinding.TmdbSearchBinding
import com.bumptech.glide.Glide
import info.movito.themoviedbapi.model.MovieDb
import info.movito.themoviedbapi.model.Multi
import info.movito.themoviedbapi.model.tv.TvSeries

class SearchAdapter(context: Context, searchList: List<Multi>) :
    RecyclerView.Adapter<SearchAdapter.SearchHolder>() {
    private var mSearchList = searchList
    private var mContext = context

    class SearchHolder(private val binding: TmdbSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun load(searchList: Multi, context: Context) {
            val title: String?
            val poster: String?
            when (searchList.mediaType) {
                Multi.MediaType.MOVIE -> {
                    title = (searchList as MovieDb).title
                    poster = searchList.posterPath
                    binding.searchType.text = context.getString(R.string.search_movie)
                    binding.searchType.setTextColor(context.getColor(R.color.orange))
                }
                Multi.MediaType.TV_SERIES -> {
                    title = (searchList as TvSeries).name
                    poster = searchList.posterPath
                    binding.searchType.text = context.getString(R.string.search_show)
                    binding.searchType.setTextColor(context.getColor(R.color.green))
                }
                else -> {
                    return
                }
            }

            binding.searchTitle.text = title
            Glide.with(binding.root)
                .load("https://www.themoviedb.org/t/p/w300_and_h450_bestv2$poster")
                .error(R.drawable.ic_broken_image)
                .override(300, 450)
                .into(binding.searchPoster)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TmdbSearchBinding.inflate(inflater, parent, false)
        return SearchHolder(binding)
    }

    override fun getItemCount(): Int {
        return mSearchList.size
    }

    override fun onBindViewHolder(holder: SearchHolder, position: Int) {
        holder.load(mSearchList[position], mContext)
    }
}
