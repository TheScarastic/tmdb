package com.abhishek.tmdb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import info.movito.themoviedbapi.model.Artwork
import info.movito.themoviedbapi.model.MovieDb
import info.movito.themoviedbapi.model.tv.TvSeries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AdvancedFragment : Fragment(), CoroutineScope by CoroutineScope(Dispatchers.IO) {

    companion object {
        const val MOVIES_DB = "MOVIESDB"
        const val TV_DB = "TVDB"
        const val IS_MOVIE = "ISMOVIE"
    }

    private var mScreenshotAdapter: ScreenshotAdapater? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_advanced, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val args = arguments
        val isMovie = args?.getBoolean(IS_MOVIE) as Boolean

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_screenshot)
        val layoutMananager = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )

        val poster: ImageView = view.findViewById(R.id.poster)
        val title: TextView = view.findViewById(R.id.title)
        val summmary: TextView = view.findViewById(R.id.summary)

        val year: TextView = view.findViewById(R.id.year_text)
        val region: TextView = view.findViewById(R.id.region_text)
        val rating: TextView = view.findViewById(R.id.rating_text)
        val posterPath: String?

        val id: Int
        if (isMovie) {
            val movieDatabase = args.getSerializable(MOVIES_DB) as MovieDb
            id = movieDatabase.id
            title.text = movieDatabase.title
            summmary.text = movieDatabase.overview
            rating.text = movieDatabase.voteAverage.toString()
            region.text = movieDatabase.originalLanguage.uppercase(Locale.getDefault())
            year.text = movieDatabase.releaseDate.split("-")[0]
            posterPath = movieDatabase.posterPath

        } else {
            val tvDatabase: TvSeries = args.getSerializable(TV_DB) as TvSeries
            id = tvDatabase.id
            title.text = tvDatabase.name
            summmary.text = tvDatabase.overview
            rating.text = tvDatabase.voteAverage.toString()
            posterPath = tvDatabase.posterPath
            region.text = tvDatabase.originCountry[0].uppercase(Locale.getDefault())
            year.text = (tvDatabase.firstAirDate).split("-")[0]
        }
        poster.let {
            Glide.with(this)
                .load("https://www.themoviedb.org/t/p/w600_and_h900_bestv2$posterPath")
                .override(1600, 800)
                .into(it)
        }

        launch {
            val artwork: List<Artwork>
            if (isMovie) {
                artwork = MovieFragment.tmdbApi.movies.getImages(id, null).backdrops
            } else {
                artwork = MovieFragment.tmdbApi.tvSeries.getImages(id, null).backdrops
            }

            launch(Dispatchers.Main) {
                val backdropPath = arrayListOf<String>()
                for (art: Artwork in artwork) {
                    backdropPath.add(art.filePath)
                }
                mScreenshotAdapter = ScreenshotAdapater(context, backdropPath)

                recyclerView.layoutManager = layoutMananager
                recyclerView.adapter = mScreenshotAdapter
            }
        }

    }
}