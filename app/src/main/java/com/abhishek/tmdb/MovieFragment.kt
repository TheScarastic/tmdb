package com.abhishek.tmdb

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import info.movito.themoviedbapi.TmdbApi
import info.movito.themoviedbapi.model.MovieDb
import info.movito.themoviedbapi.model.core.MovieResultsPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random


class MovieFragment : Fragment(), CoroutineScope by CoroutineScope(Dispatchers.IO) {

    private var mTopMoviesDb = ArrayList<MovieDb>()
    private var mPopularMoviesDb = ArrayList<MovieDb>()

    private var mTmdbTopMoviesAdapter: TmdbMoviesAdapter? = null
    private var mTmdbPopularMoviesAdapter: TmdbMoviesAdapter? = null

    private var mTopPage = 1
    private var mPopularPage = 1

    private var mRandom = 0
    private var mPosterTop = false

    private var mPoster: ImageView? = null

    private var mHandler: Handler? = null

    companion object {
        lateinit var tmdbApi: TmdbApi
        const val UPDATE_POSTER = 1000
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_common, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val recyclerViewTop: RecyclerView = view.findViewById(R.id.recyclerview_top)
        val recyclerViewPopular: RecyclerView = view.findViewById(R.id.recyclerview_popular)



        mPoster = view.findViewById(R.id.poster) as ImageView

        val layoutManagerTop = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )
        val layoutManagerPopular = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )

        mTmdbTopMoviesAdapter = TmdbMoviesAdapter(mTopMoviesDb, context)
        mTmdbPopularMoviesAdapter = TmdbMoviesAdapter(mPopularMoviesDb, context)

        recyclerViewTop.layoutManager = layoutManagerTop
        recyclerViewPopular.layoutManager = layoutManagerPopular

        recyclerViewTop.adapter = mTmdbTopMoviesAdapter
        recyclerViewPopular.adapter = mTmdbPopularMoviesAdapter

        recyclerViewTop.addOnItemTouchListener(RecyclerItemClickListener(
            context, recyclerViewTop, object :
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

        recyclerViewPopular.addOnItemTouchListener(RecyclerItemClickListener(
            context, recyclerViewTop, object :
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

        recyclerViewTop.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val totalCount = layoutManagerTop.itemCount
                val lastVisiblePos = layoutManagerTop.findFirstVisibleItemPosition() + 5
                if (lastVisiblePos >= totalCount) {
                    fetchMovies(mTopPage, 0)
                }
            }
        })

        recyclerViewPopular.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val totalCount = layoutManagerPopular.itemCount
                val lastVisiblePos = layoutManagerPopular.findFirstVisibleItemPosition() + 5
                if (lastVisiblePos >= totalCount) {
                    fetchMovies(0, mPopularPage)
                }
            }
        })

        mPoster!!.setOnClickListener {
            if (mPosterTop) {
                navigate(view, mTopMoviesDb[mRandom])
            } else {
                navigate(view, mPopularMoviesDb[mRandom])
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
        var dbSize: Int
        var topMoviePage: MovieResultsPage? = null
        var popularMoviePage: MovieResultsPage? = null

        launch {
            if (topPage != 0) {
                topMoviePage = tmdbApi.movies.getTopRatedMovies("en", topPage)
                mTopPage++
            }

            if (popularPage != 0) {
                popularMoviePage = tmdbApi.movies.getPopularMovies("en", popularPage)
                mPopularPage++
            }

            launch(Dispatchers.Main) {
                if (topMoviePage != null) {
                    dbSize = mTopMoviesDb.size
                    for (item: MovieDb in topMoviePage!!.results) {
                        mTopMoviesDb.add(item)
                        dbSize++
                        mTmdbTopMoviesAdapter?.notifyItemInserted(dbSize)
                    }
                }

                if (popularMoviePage != null) {
                    dbSize = mPopularMoviesDb.size
                    for (item: MovieDb in popularMoviePage!!.results) {
                        mPopularMoviesDb.add(item)
                        dbSize++
                        mTmdbPopularMoviesAdapter?.notifyItemInserted(dbSize)

                    }
                }
                if (!mHandler!!.hasMessages(UPDATE_POSTER)) {
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
        reset()
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

        mPoster?.let {
            Glide.with(this)
                .load("https://www.themoviedb.org/t/p/w600_and_h900_bestv2$posterPath")
                .override(1600, 800)
                .into(it)

            mHandler?.sendEmptyMessageDelayed(Companion.UPDATE_POSTER, 5000)
        }
    }

    private fun reset() {
        mTopMoviesDb.clear()
        mPopularMoviesDb.clear()
        mTopPage = 1
        mPopularPage = 1
        mTmdbTopMoviesAdapter?.notifyDataSetChanged()
        mTmdbTopMoviesAdapter?.notifyDataSetChanged()
        mHandler?.removeCallbacksAndMessages(null)
    }
}
