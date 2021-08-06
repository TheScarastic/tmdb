package com.abhishek.tmdb

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.abhishek.tmdb.databinding.FragmentSearchBinding
import info.movito.themoviedbapi.model.MovieDb
import info.movito.themoviedbapi.model.Multi
import info.movito.themoviedbapi.model.tv.TvSeries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search),
    CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val binding by viewBinding(FragmentSearchBinding::bind)
    private val mSearchList = ArrayList<Multi>()
    private var query = ""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun init() {
        val layoutManager = LinearLayoutManager(context)

        binding.rvSearch.layoutManager = layoutManager
        binding.rvSearch.adapter = SearchAdapter(requireContext(), mSearchList)

        binding.searchbar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                search(s)
                return false
            }
        })

        binding.rvSearch.addOnItemTouchListener(RecyclerItemClickListener(
            context, binding.rvSearch, object :
                RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View?, position: Int) {
                    if (view != null) {
                        navigate(view, mSearchList[position])
                    }
                }

                override fun onLongItemClick(view: View?, position: Int) {
                }
            }
        ))

        if (query.isEmpty()) search()
    }

    fun search(s: String = "") {
        mSearchList.clear()
        binding.rvSearch.adapter?.notifyDataSetChanged()

        query = if (s.isEmpty()) {
            "a"
        } else {
            s
        }
        launch {
            val results =
                MovieFragment.tmdbApi.search.searchMulti(
                    query, "", 0
                ).results

            launch(Dispatchers.Main) {
                for (result: Multi in results) {
                    if (result.mediaType == Multi.MediaType.PERSON) {
                        continue
                    }
                    mSearchList.add(result)
                    binding.rvSearch.adapter?.notifyItemInserted(mSearchList.size)
                }
            }
        }
    }

    fun navigate(view: View, db: Multi) {
        val args = Bundle()
        when (db.mediaType) {
            Multi.MediaType.MOVIE -> {
                args.putBoolean(AdvancedFragment.IS_MOVIE, true)
                args.putSerializable(AdvancedFragment.MOVIES_DB, (db as MovieDb))
            }
            Multi.MediaType.TV_SERIES -> {
                args.putBoolean(AdvancedFragment.IS_MOVIE, false)
                args.putSerializable(AdvancedFragment.TV_DB, (db as TvSeries))
            }
            else -> {
                return
            }
        }
        Navigation.findNavController(view).navigate(R.id.advanced_fragment, args)
    }
}