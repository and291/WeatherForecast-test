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

class SearchViewModel(application: Application,
                      private val forecastRepository: ForecastRepository)
    : AndroidViewModel(application) {

    var isLoading = ObservableField(false)
    var labelShown = ObservableField(true)
    var labelMessage = ObservableField<String>()
    var forecastItems = MutableLiveData<List<ForecastItem>>()

    private val compositeDisposable = CompositeDisposable()

    init {
        isLoading.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                SafeLog.v("isLoading=${isLoading.get()}")
            }
        })
        forecastItems.observeForever {
            SafeLog.v("Displayed ${it?.size} response")
            labelShown.set(it == null || it.isEmpty())
        }
    }

    fun setSearchView(searchView: SearchView){
        compositeDisposable += RxSearchView.queryTextChangeEvents(searchView)
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged { old, new ->
                    //distinctUntilChanged if not submitted
                    return@distinctUntilChanged if(new.isSubmitted){
                         false
                    } else {
                        old.queryText().toString() == new.queryText().toString()
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { isLoading.set(true) }
                .switchMap {
                    return@switchMap forecastRepository
                            .getForecast(it.queryText().toString())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext{ isLoading.set(false) }
                .subscribeWith(object : DisposableObserver<ForecastResponse>() {
                    override fun onComplete() {
                        SafeLog.v("onComplete()")
                    }

                    override fun onNext(value: ForecastResponse?) {
                        value?.let {
                            //log exceptions
                            it.exception?.let { SafeLog.v("onNext()", it) }

                            //apply data
                            labelMessage.set(it.exception?.getLabelMessage(getApplication()))
                            forecastItems.value = it.forecast?.list ?: arrayListOf()
                        }
                    }

                    override fun onError(e: Throwable?) {
                        SafeLog.v("onError() $e")
                    }
                })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}