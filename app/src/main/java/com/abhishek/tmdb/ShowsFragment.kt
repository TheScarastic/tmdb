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
import info.movito.themoviedbapi.model.tv.TvSeries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class ShowsFragment : Fragment(), CoroutineScope by CoroutineScope(Dispatchers.IO) {

    private var mTopTvDb = ArrayList<TvSeries>()
    private var mPopularTvDb = ArrayList<TvSeries>()

    private lateinit var mTmdbTopTvAdapter: TmdbTvAdapter
    private lateinit var mTmdbPopularTvAdapter: TmdbTvAdapter

    private var mTopPage = 1
    private var mPopularPage = 1

    private var mRandom = 0
    private var mPosterTop = false

    private lateinit var mPoster: ImageView

    private var mHandler: Handler = Handler(Looper.getMainLooper())


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
                        navigate(view, mTopTvDb[position])
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
                        navigate(view, mPopularTvDb[position])
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
                    fetchShows(mTopPage, 0)
                }
            }
        })

        recyclerViewPopular.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val totalCount = layoutManagerPopular.itemCount
                val lastVisiblePos = layoutManagerPopular.findFirstVisibleItemPosition() + 5
                if (lastVisiblePos >= totalCount) {
                    fetchShows(0, mPopularPage)
                }
            }
        })

        mPoster.setOnClickListener {
            if (mPosterTop) {
                navigate(view, mTopTvDb[mRandom])
            } else {
                navigate(view, mPopularTvDb[mRandom])
            }
        }

        fetchShows(mTopPage, mPopularPage)

        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    MovieFragment.UPDATE_POSTER -> setPoster()
                }
            }
        }
    }

    fun navigate(view: View, db: TvSeries) {
        val args = Bundle()
        args.putBoolean(AdvancedFragment.IS_MOVIE, false)
        args.putSerializable(AdvancedFragment.TV_DB, db)
        Navigation.findNavController(view).navigate(R.id.advanced_fragment, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        reset()
    }

    fun fetchShows(topPage: Int, popularPage: Int) {
        var topTvPage: List<TvSeries> = listOf(TvSeries())
        var popularTvPage: List<TvSeries> = listOf(TvSeries())

        if (topPage != 0) {
            topTvPage = MovieFragment.tmdbApi.tvSeries.getTopRated("en", topPage).results
            mTopPage++
        }

        if (popularPage != 0) {
            popularTvPage = MovieFragment.tmdbApi.tvSeries.getPopular("en", popularPage).results
            mPopularPage++
        }

        launch(Dispatchers.Main) {
            for (item: TvSeries in topTvPage) {
                mTopTvDb.add(item)
                mTmdbTopTvAdapter.notifyItemInserted(mTopTvDb.size)
            }

            for (item: TvSeries in popularTvPage) {
                mPopularTvDb.add(item)
                mTmdbPopularTvAdapter.notifyItemInserted(mPopularTvDb.size)
            }


            if (!mHandler.hasMessages(MovieFragment.UPDATE_POSTER)) {
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

        Glide.with(this)
            .load("https://www.themoviedb.org/t/p/w600_and_h900_bestv2$posterPath")
            .placeholder(R.drawable.ic_broken_image)
            .into(mPoster)

        mHandler.sendEmptyMessageDelayed(MovieFragment.UPDATE_POSTER, 5000)
    }


    private fun reset() {
        mTopTvDb.clear()
        mPopularTvDb.clear()
        mTopPage = 1
        mPopularPage = 1
        mTmdbTopTvAdapter.notifyDataSetChanged()
        mTmdbPopularTvAdapter.notifyDataSetChanged()
        mHandler.removeCallbacksAndMessages(null)
    }

}
