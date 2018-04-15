package pro.busik.test.weather.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.Observable
import android.databinding.ObservableField
import android.support.v7.widget.SearchView
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import pro.busik.test.weather.model.ForecastItem
import pro.busik.test.weather.model.ForecastResponse
import pro.busik.test.weather.model.repository.ForecastRepository
import pro.busik.test.weather.utils.SafeLog
import pro.busik.test.weather.utils.plusAssign
import java.util.concurrent.TimeUnit

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    var isLoading = ObservableField(false)
    var items = MutableLiveData<List<ForecastItem>>()

    private val compositeDisposable = CompositeDisposable()

    init {
        isLoading.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                SafeLog.v("isLoading=${isLoading.get()}")
            }
        })
        items.observeForever {
            SafeLog.v("Displayed ${it?.size} items")
        }
    }

    fun setSearchView(searchView: SearchView){
        compositeDisposable += RxSearchView.queryTextChangeEvents(searchView)
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { isLoading.set(true) }
                .switchMap {
                    return@switchMap ForecastRepository().getForecast(it.queryText().toString())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext{ isLoading.set(false) }
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

                            items.value = list
                        }
                    }

                    override fun onError(e: Throwable?) {
                        SafeLog.v("onError() $e")
                        //TODO notify user
                    }
                })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}