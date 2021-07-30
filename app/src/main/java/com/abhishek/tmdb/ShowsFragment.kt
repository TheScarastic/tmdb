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
import info.movito.themoviedbapi.TvResultsPage
import info.movito.themoviedbapi.model.MovieDb
import info.movito.themoviedbapi.model.tv.TvSeries
import kotlin.random.Random

class ShowsFragment : Fragment() {

    private var mTopTvDb = ArrayList<TvSeries>()
    private var mPopularTvDb = ArrayList<TvSeries>()

    private var mTmdbTopTvAdapter: TmdbTvAdapter? = null
    private var mTmdbPopularTvAdapter: TmdbTvAdapter? = null

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

        mPoster = view.findViewById(R.id.poster)

        val layoutManagerTop = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )
        val layoutManagerPopular = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )

        mTmdbTopTvAdapter = TmdbTvAdapter(mTopTvDb, context)
        mTmdbPopularTvAdapter = TmdbTvAdapter(mPopularTvDb, context)

        recyclerViewTop.layoutManager = layoutManagerTop
        recyclerViewPopular.layoutManager = layoutManagerPopular

        recyclerViewTop.adapter = mTmdbTopTvAdapter
        recyclerViewPopular.adapter = mTmdbPopularTvAdapter

        recyclerViewTop.addOnItemTouchListener(RecyclerItemClickListener(
            context, recyclerViewTop, object :
                RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View?, position: Int) {
                    if (view != null) {
                        navigate(view, position, mTopTvDb)
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
                        navigate(view, position, mPopularTvDb)
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
                navigate(view,mRandom, mTopTvDb)
            } else {
                navigate(view,mRandom, mPopularTvDb)
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

    fun navigate(view: View, position: Int, db: ArrayList<TvSeries>) {
        val args = Bundle()
        args.putBoolean(AdvancedFragment.IS_MOVIE, false)
        args.putSerializable(AdvancedFragment.TV_DB, db[position])
        Navigation.findNavController(view).navigate(R.id.advanced_fragment, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        reset()
    }

    @SuppressLint("StaticFieldLeak")
    inner class GetApi : AsyncTask<Int, Void, String>() {
        private var topTvPage: TvResultsPage? = null
        private var popularTvPage: TvResultsPage? = null

        override fun doInBackground(vararg p: Int?): String? {
            val tmdbApi = TmdbApi(BuildConfig.API_KEY)
            if (p[0] != 0) {
                topTvPage = tmdbApi.tvSeries.getTopRated("en", p[0])
                mTopPage++
            }

            if (p[1] != 0) {
                popularTvPage = tmdbApi.tvSeries.getPopular("en", p[1])
                mPopularPage++
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            var dbSize = 0
            if (topTvPage != null) {
                dbSize = mTopTvDb.size
                for (item: TvSeries in topTvPage!!.results) {
                    mTopTvDb.add(item)
                    dbSize++
                    mTmdbTopTvAdapter?.notifyItemInserted(dbSize)
                }
            }

            if (popularTvPage != null) {
                dbSize = mPopularTvDb.size
                for (item: TvSeries in popularTvPage!!.results) {
                    mPopularTvDb.add(item)
                    dbSize++
                    mTmdbPopularTvAdapter?.notifyItemInserted(dbSize)
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
                mRandom = Random.nextInt(0, mTopTvDb.size)
                mTopTvDb[mRandom].posterPath
            } else {
                mPosterTop = false
                mRandom = Random.nextInt(0, mTopTvDb.size)
                mPopularTvDb[mRandom].posterPath
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
        mTopTvDb.clear()
        mPopularTvDb.clear()
        mTopPage = 1
        mPopularPage = 1
        mTmdbTopTvAdapter?.notifyDataSetChanged()
        mTmdbPopularTvAdapter?.notifyDataSetChanged()
        mHandler?.removeCallbacksAndMessages(null)
    }

}
