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
import pro.busik.test.weather.model.data.ForecastItem
import pro.busik.test.weather.utils.SafeLog
import pro.busik.test.weather.viewmodel.SearchViewModel
import android.app.SearchManager
import android.content.Context
import android.support.v4.widget.SimpleCursorAdapter
import android.widget.Toast
import dagger.android.support.DaggerFragment
import pro.busik.test.weather.model.data.City
import pro.busik.test.weather.viewmodel.SearchViewModelFactory
import javax.inject.Inject
import android.database.MatrixCursor
import android.provider.BaseColumns
import com.google.gson.Gson

class SearchFragment : DaggerFragment() {

    companion object {
        private const val ARG_INITIAL_SEARCH_QUERY = "ARG_INITIAL_SEARCH_QUERY"
        private const val ARG_SELECTED_CITY = "ARG_SELECTED_CITY"

        fun newInstance(initialSearchQuery: String, city: City?): SearchFragment {
            val bundle = Bundle()
            bundle.putString(ARG_INITIAL_SEARCH_QUERY, initialSearchQuery)
            bundle.putParcelable(ARG_SELECTED_CITY, city)
            val fragment = SearchFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private val searchQueryKey = "SearchQueryKey"
    private val adapter = Adapter(arrayListOf())

    private var searchView: SearchView? = null

    private lateinit var initialSearchQuery: String
    private lateinit var viewModel: SearchViewModel
    private lateinit var suggestionAdapter: SimpleCursorAdapter

    @Inject lateinit var searchViewModelFactory: SearchViewModelFactory

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //check argument exists and set search query variable
        arguments.let {
            if(it == null || !it.containsKey(ARG_INITIAL_SEARCH_QUERY)){
                throw RuntimeException("Initial search query not set")
            }
            initialSearchQuery = savedInstanceState?.getString(searchQueryKey)
                    ?: it.getString(ARG_INITIAL_SEARCH_QUERY)
        }

        //get view model
        viewModel = ViewModelProviders.of(this, searchViewModelFactory)
                .get(SearchViewModel::class.java)
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

        suggestionAdapter = SimpleCursorAdapter(context,
                android.R.layout.simple_list_item_1,
                null,
                arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1),
                intArrayOf(android.R.id.text1),
                0)

        viewModel.citySuggestions.observe(this, Observer<List<City>> {
            it?.let {
                Toast.makeText(context, "Found ${it.size} cities", Toast.LENGTH_LONG)
                        .show()

                val columns = arrayOf(
                        BaseColumns._ID,
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA)

                val cursor = MatrixCursor(columns)
                val gson = Gson()
                for (i in 0 until it.size) {
                    cursor.addRow(arrayOf(i, it[i].name, gson.toJson(it[i])))
                }
                suggestionAdapter.swapCursor(cursor)
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

            //set suggestion adapter
            searchView.suggestionsAdapter = suggestionAdapter

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
