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

    private val compositeDisposable = CompositeDisposable()
    private val adapter = Adapter(arrayListOf())

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

        val searchView = searchMenuItem.actionView as SearchView
        compositeDisposable += RxSearchView.queryTextChanges(searchView)
                .debounce(500, TimeUnit.MILLISECONDS)
                .flatMap {
                    val api = ServiceGenerator.createService(Api::class.java)
                    return@flatMap api.forecast(it.toString())
                }
                .observeOn(AndroidSchedulers.mainThread())
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
