package com.abhishek.tmdb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import info.movito.themoviedbapi.model.MovieDb
import info.movito.themoviedbapi.model.Multi
import info.movito.themoviedbapi.model.tv.TvSeries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchFragment : Fragment(), CoroutineScope by CoroutineScope(Dispatchers.IO) {

    private lateinit var mSearchAdapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_search, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val searchbar: SearchView = view.findViewById(R.id.searchbar)
        val recyclerView: RecyclerView = view.findViewById(R.id.rv_search)
        val layoutManager = LinearLayoutManager(context)
        val mSearchList = ArrayList<Multi>()

        mSearchAdapter = SearchAdapter(context, mSearchList)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = mSearchAdapter

        searchbar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                if (s.isBlank()) return false
                launch {
                    val results =
                        MovieFragment.tmdbApi.search.searchMulti(
                            s, null, null
                        ).results


                    launch(Dispatchers.Main) {
                        mSearchList.clear()
                        mSearchAdapter.notifyDataSetChanged()
                        for (result: Multi in results) {
                            mSearchList.add(result)
                            mSearchAdapter.notifyItemInserted(mSearchList.size)
                        }
                    }
                }
                return true
            }
        })

        recyclerView.addOnItemTouchListener(RecyclerItemClickListener(
            context, recyclerView, object :
                RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View?, position: Int) {
                    if (view != null) {
                        navigate(view, mSearchList.get(position))
                    }
                }

                override fun onLongItemClick(view: View?, position: Int) {
                }
            }
        ))
    }

    fun navigate(view: View, db: Multi) {
        val args = Bundle()
        if (db.mediaType == Multi.MediaType.MOVIE) {
            args.putBoolean(AdvancedFragment.IS_MOVIE, true)
            args.putSerializable(AdvancedFragment.MOVIES_DB, (db as MovieDb))
        } else if (db.mediaType == Multi.MediaType.TV_SERIES) {
            args.putBoolean(AdvancedFragment.IS_MOVIE, false)
            args.putSerializable(AdvancedFragment.TV_DB, (db as TvSeries))
        } else {
            return
        }
        Navigation.findNavController(view).navigate(R.id.advanced_fragment, args)
    }
}