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
import info.movito.themoviedbapi.model.tv.TvSeries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class ShowsFragment : Fragment(R.layout.fragment_common),
    CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val binding by viewBinding(FragmentCommonBinding::bind)

    private var mTopTvDb = ArrayList<TvSeries>()
    private var mPopularTvDb = ArrayList<TvSeries>()

    private var mTopPage = 1
    private var mPopularPage = 1

    private var mRandom = 0
    private var mPosterTop = false

    private var mHandler: Handler = Handler(Looper.getMainLooper())

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

        binding.recyclerviewTop.adapter = TmdbTvAdapter(mTopTvDb, requireContext())
        binding.recyclerviewPopular.adapter = TmdbTvAdapter(mPopularTvDb, requireContext())

        binding.recyclerviewTop.addOnItemTouchListener(RecyclerItemClickListener(
            context, binding.recyclerviewTop, object :
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

        binding.recyclerviewPopular.addOnItemTouchListener(RecyclerItemClickListener(
            context, binding.recyclerviewPopular, object :
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

        binding.recyclerviewTop.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val totalCount = layoutManagerTop.itemCount
                val lastVisiblePos = layoutManagerTop.findFirstVisibleItemPosition() + 5
                if (lastVisiblePos >= totalCount) {
                    fetchShows(mTopPage, 0)
                }
            }
        })

        binding.recyclerviewPopular.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val totalCount = layoutManagerPopular.itemCount
                val lastVisiblePos = layoutManagerPopular.findFirstVisibleItemPosition() + 5
                if (lastVisiblePos >= totalCount) {
                    fetchShows(0, mPopularPage)
                }
            }
        })

        binding.poster.setOnClickListener {
            if (mPosterTop) {
                navigate(it, mTopTvDb[mRandom])
            } else {
                navigate(it, mPopularTvDb[mRandom])
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
        mHandler.removeCallbacksAndMessages(null)
    }

    fun fetchShows(topPage: Int, popularPage: Int) {
        launch {
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
                    binding.recyclerviewTop.adapter?.notifyItemInserted(mTopTvDb.size)
                }

                for (item: TvSeries in popularTvPage) {
                    mPopularTvDb.add(item)
                    binding.recyclerviewPopular.adapter?.notifyItemInserted(mPopularTvDb.size)
                }


                if (!mHandler.hasMessages(MovieFragment.UPDATE_POSTER)) {
                    setPoster()
                }
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
            .into(binding.poster)

        mHandler.sendEmptyMessageDelayed(MovieFragment.UPDATE_POSTER, 5000)
    }
}
