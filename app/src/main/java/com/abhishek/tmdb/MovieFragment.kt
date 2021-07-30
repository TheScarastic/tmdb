package com.abhishek.tmdb

import android.annotation.SuppressLint
import android.os.*
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
import kotlin.random.Random


class MovieFragment : Fragment() {

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

    private val UPDATE_POSTER = 1000

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
                        navigate(view,position, mTopMoviesDb)
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
                        navigate(view,position, mPopularMoviesDb)
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
                    GetApi().execute(mTopPage, 0)
                    mTopPage++
                }
            }
        })

        recyclerViewPopular.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val totalCount = layoutManagerPopular.itemCount
                val lastVisiblePos = layoutManagerPopular.findFirstVisibleItemPosition() + 5
                if (lastVisiblePos >= totalCount) {
                    GetApi().execute(0, mPopularPage)
                    mPopularPage++
                }
            }
        })

        mPoster!!.setOnClickListener {
            if (mPosterTop) {
                navigate(view,mRandom, mTopMoviesDb)
            } else {
                navigate(view,mRandom, mPopularMoviesDb)
            }
        }

        GetApi().execute(mTopPage, mPopularPage)

        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    UPDATE_POSTER -> setPoster()
                }
            }
        }
    }

    fun navigate(view: View, position: Int, db: ArrayList<MovieDb>) {
        val args = Bundle()
        args.putBoolean(AdvancedFragment.IS_MOVIE, true)
        args.putSerializable(AdvancedFragment.MOVIES_DB, db[position])
        Navigation.findNavController(view).navigate(R.id.advanced_fragment, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        reset()
    }

    @SuppressLint("StaticFieldLeak")
    inner class GetApi : AsyncTask<Int, Void, String>() {
        private var topMoviePage: MovieResultsPage? = null
        private var popularMoviePage: MovieResultsPage? = null

        override fun doInBackground(vararg p: Int?): String? {
            val tmdbApi = TmdbApi("2bc3bb8279aa7bcc7bd18d60857dc82a")
            if (p[0] != 0) {
                topMoviePage = tmdbApi.movies.getTopRatedMovies("en", p[0])
                mTopPage++
            }

            if (p[1] != 0) {
                popularMoviePage = tmdbApi.movies.getPopularMovies("en", p[1])
                mPopularPage++
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            var dbSize = 0
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

            mHandler?.sendEmptyMessageDelayed(UPDATE_POSTER, 5000)
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