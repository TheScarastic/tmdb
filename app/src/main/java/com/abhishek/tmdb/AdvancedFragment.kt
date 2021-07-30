package com.abhishek.tmdb

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import info.movito.themoviedbapi.model.MovieDb
import info.movito.themoviedbapi.model.tv.TvSeries
import java.util.*

class AdvancedFragment : Fragment() {

    companion object {
        val MOVIES_DB = "MOVIESDB"
        val TV_DB  = "TVDB"
        val IS_MOVIE = "ISMOVIE"
    }

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

        val poster: ImageView = view.findViewById(R.id.poster)
        val title: TextView = view.findViewById(R.id.title)
        val summmary: TextView = view.findViewById(R.id.summary)

        val year: TextView = view.findViewById(R.id.year_text)
        val region: TextView = view.findViewById(R.id.region_text)
        val rating: TextView = view.findViewById(R.id.rating_text)
        var posterPath: String? = null

        if (isMovie) {
            val movieDatabase = args.getSerializable(MOVIES_DB) as MovieDb
            title.text = movieDatabase.title
            summmary.text = movieDatabase.overview
            rating.text = movieDatabase.voteAverage.toString()
            region.text = movieDatabase.originalLanguage.uppercase(Locale.getDefault())
            year.text = (movieDatabase.releaseDate).split("-")[0]
            posterPath = movieDatabase.posterPath

        } else {
            val tvDatabase: TvSeries = args.getSerializable(TV_DB) as TvSeries
            title.text = tvDatabase.name
            summmary.text = tvDatabase.overview
            rating.text = tvDatabase.voteAverage.toString()
            posterPath = tvDatabase.posterPath
            // region.text = tvDatabase?.originalLanguage.uppercase(Locale.getDefault())
            // year.text = (tvDatabase?.releaseDate).split("-")[0]
        }

        poster.let {
            Glide.with(this)
                .load("https://www.themoviedb.org/t/p/w600_and_h900_bestv2$posterPath")
                .override(1600, 800)
                .into(it)
        }

    }
}