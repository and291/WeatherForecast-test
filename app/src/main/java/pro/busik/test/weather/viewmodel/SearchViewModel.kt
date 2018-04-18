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
import pro.busik.test.weather.model.*
import pro.busik.test.weather.model.repository.find.FindRepository
import pro.busik.test.weather.model.repository.forecast.ForecastRepository
import pro.busik.test.weather.model.data.City
import pro.busik.test.weather.model.data.Find
import pro.busik.test.weather.model.data.Forecast
import pro.busik.test.weather.model.data.ForecastItem
import pro.busik.test.weather.model.ParameterGenerator
import pro.busik.test.weather.utils.SafeLog
import pro.busik.test.weather.utils.plusAssign
import java.util.concurrent.TimeUnit

class SearchViewModel(application: Application,
                      private val parameterGenerator: ParameterGenerator,
                      private val forecastRepository: ForecastRepository,
                      private val findRepository: FindRepository)
    : AndroidViewModel(application) {

    var isLoading = ObservableField(false)
    var labelShown = ObservableField(true)
    var labelMessage = ObservableField<String>()
    var forecastItems = MutableLiveData<List<ForecastItem>>()
    var citySuggestions = MutableLiveData<List<City>>()

    private val compositeDisposable = CompositeDisposable()
    private var selectedCity: City? = null //can be not null only until query changed
    private var currentQuery: String? = null //used for input filtering

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
        val sharedObservable = RxSearchView.queryTextChangeEvents(searchView)
                .debounce(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .filter {
                    //distinct until query changed if not submitted
                    val result = if(it.isSubmitted) true else currentQuery != it.queryText().toString()
                    currentQuery = it.queryText().toString()
                    return@filter result
                }
                .flatMap { it -> io.reactivex.Observable.just(it.queryText().toString()) }
                .doOnNext {
                    //remove city if query changed
                    if(selectedCity != null && it != selectedCity?.name){
                        selectedCity = null
                    }
                }
                .share()

        compositeDisposable += sharedObservable
//                .distinctUntilChanged { old, new ->
//                    //distinctUntilChanged if not submitted
//                    return@distinctUntilChanged if(new.isSubmitted){
//                         false
//                    } else {
//                        old.queryText().toString() == new.queryText().toString() //doesn't work on second emit (bug?)
//                    }
//                }
                .doOnNext { isLoading.set(true) }
                .switchMap { it -> forecastRepository.getData(parameterGenerator.generate(it, selectedCity)) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext{ isLoading.set(false) }
                .subscribeWith(object : DisposableObserver<ResponseResult<Forecast>>() {
                    override fun onComplete() {
                        SafeLog.v("onComplete()")
                    }

                    override fun onNext(value: ResponseResult<Forecast>?) {
                        value?.let {
                            //log exceptions
                            it.exception?.let { SafeLog.v("onNext()", it) }

                            //apply data
                            labelMessage.set(it.exception?.getLabelMessage(getApplication()))
                            forecastItems.value = it.data?.list ?: arrayListOf()
                        }
                    }

                    override fun onError(e: Throwable?) {
                        SafeLog.v("onError() $e")
                    }
                })


        compositeDisposable += sharedObservable
                //do not perform any requests until selectedCity exists
                .filter { selectedCity == null }
                .switchMap { it -> findRepository.getData(parameterGenerator.generate(it)) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<ResponseResult<Find>>() {
                    override fun onComplete() {
                        SafeLog.v("onComplete()")
                    }

                    override fun onNext(value: ResponseResult<Find>?) {
                        value?.let {
                            //log exceptions
                            it.exception?.let { SafeLog.v("onNext()", it) }

                            //apply data
                            //don't show suggestions if there is only one
                            citySuggestions.value = it.data?.list?.let {
                                if(it.size == 1) arrayListOf() else it
                            } ?: arrayListOf()
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