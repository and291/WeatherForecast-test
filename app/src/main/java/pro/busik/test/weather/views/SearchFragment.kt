package pro.busik.test.weather.views

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.support.v7.widget.SearchView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_search.*
import pro.busik.test.weather.R
import pro.busik.test.weather.databinding.FragmentSearchBinding
import pro.busik.test.weather.databinding.ListItemForecastBinding
import pro.busik.test.weather.model.ForecastItem
import pro.busik.test.weather.utils.SafeLog
import pro.busik.test.weather.viewmodel.SearchViewModel
import android.app.SearchManager
import android.content.Context
import dagger.android.support.DaggerFragment
import pro.busik.test.weather.viewmodel.SearchViewModelFactory
import javax.inject.Inject

class SearchFragment : DaggerFragment() {

    private val searchQueryKey = "SearchQueryKey"
    private val adapter = Adapter(arrayListOf())

    private var initialSearchQuery: String = "Москва"
    private var searchView: SearchView? = null

    private lateinit var viewModel: SearchViewModel

    @Inject lateinit var searchViewModelFactory: SearchViewModelFactory

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, searchViewModelFactory)
                .get(SearchViewModel::class.java)

        //Restore search query
        savedInstanceState?.let {
            initialSearchQuery = it.getString(searchQueryKey)
            SafeLog.v("Search query restored: $initialSearchQuery")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val dataBinding: FragmentSearchBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_search, container, false)
        dataBinding.searchViewModel = viewModel
        dataBinding.executePendingBindings()
        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        rvForecastItems.layoutManager = LinearLayoutManager(context)
        rvForecastItems.adapter = adapter
        rvForecastItems.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))

        viewModel.forecastItems.observe(this, Observer<List<ForecastItem>> {
            it?.let {
                val diff = DiffUtil.calculateDiff(adapter.getDiffCallback(it))
                adapter.update(diff, it)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.fragment_search, menu)

        if(searchView == null) {
            val searchMenuItem = menu!!.findItem(R.id.action_search)
            val searchView = searchMenuItem.actionView as SearchView

            //force search view to fill entire toolbar
            searchView.maxWidth = Integer.MAX_VALUE

            //set configuration
            val searchManager = context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))

            //set search query
            searchMenuItem.expandActionView()
            searchView.setQuery(initialSearchQuery, false)
            searchView.clearFocus()

            viewModel.setSearchView(searchView)
            this.searchView = searchView
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        //Save current search query
        val currentSearchQuery = searchView?.query.toString()
        outState.putString(searchQueryKey, currentSearchQuery)
        SafeLog.v("Current search query saved: $currentSearchQuery")
    }

    private inner class Adapter(private val items: MutableList<ForecastItem>) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutInflater = LayoutInflater.from(this@SearchFragment.context)
            val binding = ListItemForecastBinding.inflate(layoutInflater, parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        internal fun update(diffResult: DiffUtil.DiffResult, updatedList: List<ForecastItem>){
            items.clear()
            items.addAll(updatedList)
            diffResult.dispatchUpdatesTo(this)
        }

        internal fun getDiffCallback(updatedList: List<ForecastItem>) : ForecastItemDiffCallback {
            return ForecastItemDiffCallback(adapter.items, updatedList)
        }
    }

    private class ViewHolder(private val binding: ListItemForecastBinding)
        : RecyclerView.ViewHolder(binding.root) {

        internal fun bind(forecastItem: ForecastItem){
            binding.forecastItem = forecastItem
            binding.executePendingBindings()
        }
    }

    private class ForecastItemDiffCallback(val old: List<ForecastItem>, val new: List<ForecastItem>)
        : DiffUtil.Callback() {

        override fun getOldListSize(): Int = old.size

        override fun getNewListSize(): Int = new.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            //it's better to check by item.id, but forecast item has no ids
            return old[oldItemPosition] == new[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return old[oldItemPosition] == new[newItemPosition]
        }
    }
}
