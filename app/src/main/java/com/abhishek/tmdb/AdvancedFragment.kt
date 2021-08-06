package com.abhishek.tmdb

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.abhishek.tmdb.databinding.FragmentAdvancedBinding
import com.bumptech.glide.Glide
import info.movito.themoviedbapi.model.Artwork
import info.movito.themoviedbapi.model.MovieDb
import info.movito.themoviedbapi.model.tv.TvSeries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AdvancedFragment : Fragment(R.layout.fragment_advanced),
    CoroutineScope by CoroutineScope(Dispatchers.IO) {

    companion object {
        const val MOVIES_DB = "MOVIESDB"
        const val TV_DB = "TVDB"
        const val IS_MOVIE = "ISMOVIE"
    }

    private val binding by viewBinding(FragmentAdvancedBinding::bind)

    private lateinit var mScreenshotAdapter: ScreenshotAdapter
    private var mArtwork = arrayListOf<Artwork>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun init() {
        val args = arguments
        val isMovie = args?.getBoolean(IS_MOVIE) as Boolean

        val layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )

        val posterPath: String?
        var region = "XX"
        var year = "XXXX"

        mScreenshotAdapter = ScreenshotAdapter(mArtwork)
        binding.rvScreenshot.layoutManager = layoutManager
        binding.rvScreenshot.adapter = mScreenshotAdapter

        val id: Int
        if (isMovie) {
            val movieDatabase = args.getSerializable(MOVIES_DB) as MovieDb
            id = movieDatabase.id
            binding.title.text = movieDatabase.title
            binding.summary.text = movieDatabase.overview
            binding.ratingText.text = movieDatabase.voteAverage.toString()
            posterPath = movieDatabase.posterPath
            region = movieDatabase.originalLanguage.uppercase(Locale.getDefault())
            try {
                year = movieDatabase.releaseDate.split("-")[0]
            } catch (e: Exception) {
            }
        } else {
            val tvDatabase = args.getSerializable(TV_DB) as TvSeries
            id = tvDatabase.id
            binding.title.text = tvDatabase.name
            binding.summary.text = tvDatabase.overview
            binding.ratingText.text = tvDatabase.voteAverage.toString()
            posterPath = tvDatabase.posterPath
            try {
                year = (tvDatabase.firstAirDate).split("-")[0]
                region = tvDatabase.originCountry[0].uppercase(Locale.getDefault())
            } catch (e: Exception) {
            }
        }

        binding.regionText.text = region
        binding.yearText.text = year

        Glide.with(this)
            .load("https://www.themoviedb.org/t/p/w600_and_h900_bestv2$posterPath")
            .placeholder(R.drawable.ic_broken_image)
            .into(binding.poster)

        launch {
            val artwork: List<Artwork> = if (isMovie) {
                MovieFragment.tmdbApi.movies.getImages(id, null).backdrops
            } else {
                MovieFragment.tmdbApi.tvSeries.getImages(id, null).backdrops
            }

            launch(Dispatchers.Main) {
                for (art: Artwork in artwork) {
                    mArtwork.add(art)
                    mScreenshotAdapter.notifyItemInserted(mArtwork.size)
                }
            }
        }
    }
}