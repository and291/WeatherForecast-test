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
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_search.*
import pro.busik.test.weather.refrofit.Api
import pro.busik.test.weather.model.ForecastItem
import pro.busik.test.weather.model.ForecastResponse
import pro.busik.test.weather.refrofit.ServiceGenerator
import pro.busik.test.weather.utils.SafeLog
import pro.busik.test.weather.utils.plusAssign
import pro.busik.test.weather.views.ForecastItemView
import java.util.concurrent.TimeUnit


/**
 * A placeholder fragment containing a simple view.
 */
class SearchFragment : Fragment() {

    private val SEARCH_QUERY_KEY = "SEARCH_QUERY_KEY"
    private var initialSearchQuery: String = "Москва" //default city :)
    private lateinit var searchView: SearchView

    private val compositeDisposable = CompositeDisposable()
    private val adapter = Adapter(arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            initialSearchQuery = it.getString(SEARCH_QUERY_KEY)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvForecastItems.adapter = adapter
        rvForecastItems.layoutManager = LinearLayoutManager(context)
        rvForecastItems.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.fragment_search, menu)
        val searchMenuItem = menu!!.findItem(R.id.action_search)

        searchView = searchMenuItem.actionView as SearchView
        initialSearchQuery.let {
            searchMenuItem.expandActionView()
            searchView.setQuery(it, false)
            searchView.clearFocus()
        }

        //BUGFIX: SearchView doesn't fill entire toolbar on landscape without next line (API22)
        searchView.maxWidth = Integer.MAX_VALUE
        //end
        compositeDisposable += RxSearchView.queryTextChangeEvents(searchView)
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    pbForecastItems.visibility = View.VISIBLE
                }
                .switchMap {
                    val api = ServiceGenerator.createService(Api::class.java)
                    val query = it.queryText().toString()
                    return@switchMap api.forecast(query)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError {
                                SafeLog.v("Error occurred", it)
                            }
                            .onErrorResumeNext(Observable.just(ForecastResponse.getEmptyResponse()))
                }
                .doOnNext{
                    pbForecastItems.visibility = View.GONE
                }
                .subscribeWith(object : DisposableObserver<ForecastResponse>() {
                    override fun onComplete() {
                        SafeLog.v("onComplete()")
                    }

                    override fun onNext(value: ForecastResponse?) {
                        SafeLog.v("onNext(): ${value}")
                        val diff = DiffUtil.calculateDiff(adapter.getDiffCallback(value!!.list))
                        adapter.update(diff, value!!.list)
                    }

                    override fun onError(e: Throwable?) {
                        SafeLog.v("onError() ${e}")
                    }
                })

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchView.query.toString())
    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        compositeDisposable.dispose()
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
            return old[oldItemPosition].hashCode() == new[newItemPosition].hashCode() //TODO check
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return old[oldItemPosition].equals(new[newItemPosition])
        }

    }
}
