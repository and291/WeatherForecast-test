package pro.busik.test.weather

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.*
import android.support.v7.widget.SearchView
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import pro.busik.test.weather.utils.SafeLog
import pro.busik.test.weather.utils.plusAssign
import java.util.concurrent.TimeUnit


/**
 * A placeholder fragment containing a simple view.
 */
class SearchFragment : Fragment() {

    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.fragment_search, menu)
        val searchMenuItem = menu!!.findItem(R.id.action_search)

        val searchView = searchMenuItem.actionView as SearchView
        compositeDisposable += RxSearchView.queryTextChanges(searchView)
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribeWith(object : DisposableObserver<CharSequence>() {
                    override fun onComplete() {
                        SafeLog.v("onComplete()")
                    }

                    override fun onNext(value: CharSequence?) {
                        SafeLog.v("onNext(): ${value}")
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
}
