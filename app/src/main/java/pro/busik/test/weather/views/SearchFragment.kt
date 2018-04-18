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
import dagger.android.support.DaggerFragment
import pro.busik.test.weather.model.data.City
import pro.busik.test.weather.viewmodel.SearchViewModelFactory
import javax.inject.Inject
import android.database.MatrixCursor
import android.provider.BaseColumns
import com.google.gson.Gson
import pro.busik.test.weather.model.data.SearchQuery

class SearchFragment : DaggerFragment() {

    companion object {
        private const val CURRENT_SEARCH_QUERY_KEY = "CURRENT_SEARCH_QUERY_KEY"
        private const val ARG_INITIAL_SEARCH_QUERY = "ARG_INITIAL_SEARCH_QUERY"

        fun newInstance(searchQuery: SearchQuery): SearchFragment {
            val bundle = Bundle()
            bundle.putParcelable(ARG_INITIAL_SEARCH_QUERY, searchQuery)
            val fragment = SearchFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var initialSearchQuery: SearchQuery

    private lateinit var suggestionAdapter: SimpleCursorAdapter

    private lateinit var searchViewModel: SearchViewModel
    @Inject lateinit var searchViewModelFactory: SearchViewModelFactory

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //check argument exists and recover search query variable
        initialSearchQuery = arguments.let {
            if(it == null || !it.containsKey(ARG_INITIAL_SEARCH_QUERY)){
                throw RuntimeException("Initial search query not set")
            }
            savedInstanceState?.getParcelable(CURRENT_SEARCH_QUERY_KEY) ?:
                    it.getParcelable(ARG_INITIAL_SEARCH_QUERY)
        }
        SafeLog.v("Initial search query recovered: $initialSearchQuery")

        //get view model
        searchViewModel = ViewModelProviders.of(this, searchViewModelFactory)
                .get(SearchViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val dataBinding: FragmentSearchBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_search, container, false)
        dataBinding.searchViewModel = searchViewModel
        dataBinding.executePendingBindings()
        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //set rv
        val forecastItemAdapter = Adapter(arrayListOf())
        rvForecastItems.layoutManager = LinearLayoutManager(context)
        rvForecastItems.adapter = forecastItemAdapter
        rvForecastItems.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))

        //
        suggestionAdapter = SimpleCursorAdapter(context,
                android.R.layout.simple_list_item_2,
                null,
                arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2),
                intArrayOf(android.R.id.text1, android.R.id.text2),
                0)

        //updates forecast
        searchViewModel.forecastItems.observe(this, Observer<List<ForecastItem>> {
            it?.let {
                val diff = DiffUtil.calculateDiff(forecastItemAdapter.getDiffCallback(it))
                forecastItemAdapter.update(diff, it)
            }
        })

        //updates suggestions
        searchViewModel.citySuggestions.observe(this, Observer<List<City>> {
            it?.let {
                val columns = arrayOf(
                        BaseColumns._ID,
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_TEXT_2,
                        SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA)

                val cursor = MatrixCursor(columns)
                val gson = Gson()
                for (i in 0 until it.size) {
                    cursor.addRow(arrayOf(i, it[i].name, "id=${it[i].id}", gson.toJson(it[i])))
                }
                suggestionAdapter.swapCursor(cursor)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.fragment_search, menu)

        val searchMenuItem = menu!!.findItem(R.id.action_search)
        searchMenuItem.expandActionView() //expand view to set query

        val searchManager = context!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        //set search view
        val searchView = searchMenuItem.actionView as SearchView
        searchView.maxWidth = Integer.MAX_VALUE //force search view to fill entire toolbar
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName)) //set configuration
        searchView.suggestionsAdapter = suggestionAdapter //set suggestion adapter
        searchView.setQuery(initialSearchQuery.textQuery, false)
        searchView.clearFocus() //clear focus to remove keyboard

        //pass SearchView & initial SearchQuery to view model
        searchViewModel.startSearch(searchView, initialSearchQuery)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        //Save current search query
        val currentSearchQuery = searchViewModel.getCurrentSearchQuery()
        outState.putParcelable(CURRENT_SEARCH_QUERY_KEY, currentSearchQuery)
        SafeLog.v("Current search query saved: $currentSearchQuery")
    }

    private inner class Adapter(private val items: MutableList<ForecastItem>)
        : RecyclerView.Adapter<ViewHolder>() {

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
            return ForecastItemDiffCallback(items, updatedList)
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
