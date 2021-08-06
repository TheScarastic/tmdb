package com.abhishek.tmdb

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abhishek.tmdb.databinding.FragmentCommonBinding
import com.bumptech.glide.Glide
import info.movito.themoviedbapi.TmdbApi
import info.movito.themoviedbapi.model.MovieDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random


class MovieFragment : Fragment(R.layout.fragment_common),
    CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val binding by viewBinding(FragmentCommonBinding::bind)

    private var mTopMoviesDb = ArrayList<MovieDb>()
    private var mPopularMoviesDb = ArrayList<MovieDb>()

    private var mTopPage = 1
    private var mPopularPage = 1

    private var mRandom = 0
    private var mPosterTop = false

    private var mHandler: Handler = Handler(Looper.getMainLooper())

    companion object {
        lateinit var tmdbApi: TmdbApi
        const val UPDATE_POSTER = 1000
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun init() {
        val layoutManagerTop = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )
        val layoutManagerPopular = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )

        binding.recyclerviewTop.layoutManager = layoutManagerTop
        binding.recyclerviewPopular.layoutManager = layoutManagerPopular

        binding.recyclerviewTop.adapter = TmdbMoviesAdapter(mTopMoviesDb, requireContext())
        binding.recyclerviewPopular.adapter = TmdbMoviesAdapter(mPopularMoviesDb, requireContext())

        binding.recyclerviewTop.addOnItemTouchListener(RecyclerItemClickListener(
            context, binding.recyclerviewTop, object :
                RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View?, position: Int) {
                    if (view != null) {
                        navigate(view, mTopMoviesDb[position])
                    }
                }

                override fun onLongItemClick(view: View?, position: Int) {
                }
            }
        ))

        binding.recyclerviewPopular.addOnItemTouchListener(RecyclerItemClickListener(
            context, binding.recyclerviewPopular, object :
                RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View?, position: Int) {
                    if (view != null) {
                        navigate(view, mPopularMoviesDb[position])
                    }
                }

                override fun onLongItemClick(view: View?, position: Int) {
                }
            }
        ))

        binding.recyclerviewTop.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val totalCount = layoutManagerTop.itemCount
                val lastVisiblePos = layoutManagerTop.findFirstVisibleItemPosition() + 5
                if (lastVisiblePos >= totalCount) {
                    fetchMovies(mTopPage, 0)
                }
            }
        })

        binding.recyclerviewPopular.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val totalCount = layoutManagerPopular.itemCount
                val lastVisiblePos = layoutManagerPopular.findFirstVisibleItemPosition() + 5
                if (lastVisiblePos >= totalCount) {
                    fetchMovies(0, mPopularPage)
                }
            }
        })

        binding.poster.setOnClickListener {
            if (mPosterTop) {
                navigate(it, mTopMoviesDb[mRandom])
            } else {
                navigate(it, mPopularMoviesDb[mRandom])
            }
        }

        launch {
            tmdbApi = TmdbApi(BuildConfig.API_KEY)
            fetchMovies(mTopPage, mPopularPage)
        }

        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    UPDATE_POSTER -> setPoster()
                }
            }
        }
    }

    fun fetchMovies(topPage: Int, popularPage: Int) {
        launch {
            var topMoviePage: List<MovieDb> = listOf(MovieDb())
            var popularMoviePage: List<MovieDb> = listOf(MovieDb())

            if (topPage != 0) {
                topMoviePage = tmdbApi.movies.getTopRatedMovies("en", topPage).results
                mTopPage++
            }

            if (popularPage != 0) {
                popularMoviePage = tmdbApi.movies.getPopularMovies("en", popularPage).results
                mPopularPage++
            }

            launch(Dispatchers.Main) {
                for (item: MovieDb in topMoviePage) {
                    mTopMoviesDb.add(item)
                    binding.recyclerviewTop.adapter?.notifyItemInserted(mTopMoviesDb.size)
                }


                for (item: MovieDb in popularMoviePage) {
                    mPopularMoviesDb.add(item)
                    binding.recyclerviewPopular.adapter?.notifyItemInserted(mPopularMoviesDb.size)
                }

                if (!mHandler.hasMessages(UPDATE_POSTER)) {
                    setPoster()
                }
            }
        }
    }

    fun navigate(view: View, db: MovieDb) {
        val args = Bundle()
        args.putBoolean(AdvancedFragment.IS_MOVIE, true)
        args.putSerializable(AdvancedFragment.MOVIES_DB, db)
        Navigation.findNavController(view).navigate(R.id.advanced_fragment, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mHandler.removeCallbacksAndMessages(null)
    }

    fun setPoster() {
        val posterPath =
            if (Random.nextBoolean()) {
                mPosterTop = true
                mRandom = Random.nextInt(0, mTopMoviesDb.size)
                mTopMoviesDb[mRandom].posterPath
            } else {
                mPosterTop = false
                mRandom = Random.nextInt(0, mPopularMoviesDb.size)
                mPopularMoviesDb[mRandom].posterPath
            }

        Glide.with(this)
            .load("https://www.themoviedb.org/t/p/w600_and_h900_bestv2$posterPath")
            .placeholder(R.drawable.ic_broken_image)
            .into(binding.poster)

        mHandler.sendEmptyMessageDelayed(UPDATE_POSTER, 5000)
    }
}
