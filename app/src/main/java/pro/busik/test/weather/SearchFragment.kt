package pro.busik.test.weather

import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.support.v7.widget.SearchView
import android.widget.LinearLayout
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import kotlinx.android.synthetic.main.fragment_search.*
import pro.busik.test.weather.model.ForecastItem
import pro.busik.test.weather.model.repository.ForecastRepository
import pro.busik.test.weather.model.ForecastResponse
import pro.busik.test.weather.utils.SafeLog
import pro.busik.test.weather.utils.plusAssign
import pro.busik.test.weather.views.ForecastItemView
import java.util.concurrent.TimeUnit

class SearchFragment : Fragment() {

    private val searchQueryKey = "SearchQueryKey"
    private val adapter = Adapter(arrayListOf())
    private val compositeDisposable = CompositeDisposable()

    private var initialSearchQuery: String = "Москва"
    private var searchView: SearchView? = null

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Restore search query
        savedInstanceState?.let {
            initialSearchQuery = it.getString(searchQueryKey)
            SafeLog.v("Search query restored: $initialSearchQuery")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        rvForecastItems.adapter = adapter
        rvForecastItems.layoutManager = LinearLayoutManager(context)
        rvForecastItems.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.fragment_search, menu)

        if(searchView == null) {
            val searchMenuItem = menu!!.findItem(R.id.action_search)
            val searchView = searchMenuItem.actionView as SearchView
            searchMenuItem.expandActionView()
            searchView.setQuery(initialSearchQuery, false)
            searchView.clearFocus()

            //BUGFIX: SearchView doesn't fill entire toolbar on landscape without next line (API22)
            searchView.maxWidth = Integer.MAX_VALUE
            //end

            setObservable(searchView)
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

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun setObservable(searchView: SearchView){
        compositeDisposable += RxSearchView.queryTextChangeEvents(searchView)
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    SafeLog.v("pbForecastItems shown")
                    pbForecastItems.visibility = View.VISIBLE
                }
                .switchMap {
                    return@switchMap ForecastRepository().getForecast(it.queryText().toString())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext{
                    SafeLog.v("pbForecastItems hidden")
                    pbForecastItems.visibility = View.GONE
                }
                .subscribeWith(object : DisposableObserver<ForecastResponse>() {
                    override fun onComplete() {
                        SafeLog.v("onComplete()")
                        //TODO notify user
                    }

                    override fun onNext(value: ForecastResponse?) {
                        //SafeLog.v("onNext(): $value")
                        value?.let {
                            it.throwable?.let {
                                SafeLog.v("", it)
                            }

                            var list: ArrayList<ForecastItem> = arrayListOf()
                            it.forecast?.let {
                                list = it.list
                            }

                            val diff = DiffUtil.calculateDiff(adapter.getDiffCallback(list))
                            adapter.update(diff, list)

                            SafeLog.v("Displayed ${list.size} items")
                        }
                    }

                    override fun onError(e: Throwable?) {
                        SafeLog.v("onError() $e")
                        //TODO notify user
                    }
                })
    }

    private inner class Adapter(private val items: MutableList<ForecastItem>) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(this@SearchFragment.context)
                    .inflate(R.layout.list_item_forecast, parent, false)
            return ViewHolder(view)
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

        internal fun getDiffCallback(updatedList: List<ForecastItem>) : ForecastItemDiffCallback{
            return ForecastItemDiffCallback(adapter.items, updatedList)
        }
    }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val forecastItemView: ForecastItemView = ForecastItemView(itemView)

        internal fun bind(forecastItem: ForecastItem){
            forecastItemView.bind(forecastItem)
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
