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
import pro.busik.test.weather.model.ParameterGenerator
import pro.busik.test.weather.model.data.*
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
    private lateinit var searchQuery: SearchQuery
    private var skipFirstFilter: Boolean = true

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

    fun startSearch(searchView: SearchView, initialSearchQuery: SearchQuery){
        //set initial searchQuery
        searchQuery = initialSearchQuery
        skipFirstFilter = true

        //create shared observable
        val sharedObservable = RxSearchView.queryTextChangeEvents(searchView)
                .observeOn(AndroidSchedulers.mainThread())
//              .distinctUntilChanged { old, new ->
//                  //distinctUntilChanged if not submitted
//                  return@distinctUntilChanged if(new.isSubmitted){
//                       false
//                  } else {
//                      old.queryText().toString() == new.queryText().toString() //doesn't work on second emit (bug?)
//                  }
//              }
                .filter {
                    //skips filter on first emit so the user does not need to submit
                    if(skipFirstFilter){
                        skipFirstFilter = false
                        return@filter true
                    }

                    //distinct until query changed if not submitted
                    val result = if(it.isSubmitted) true else searchQuery.textQuery != it.queryText().toString()
                    searchQuery.textQuery = it.queryText().toString()
                    return@filter result
                }
                .debounce(300, TimeUnit.MILLISECONDS)
                .flatMap { it -> io.reactivex.Observable.just(it.queryText().toString()) }
                .doOnNext {
                    //remove city if query changed
                    if(searchQuery.city != null && it != searchQuery.city?.name){
                        searchQuery.city = null
                    }
                }
                .share()

        //forecast request
        compositeDisposable += sharedObservable
                .doOnNext { isLoading.set(true) }
                .switchMap { it -> forecastRepository.getData(parameterGenerator.generate(it, searchQuery.city)) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext{ isLoading.set(false) }
                .subscribeWith(object : DisposableObserver<ResponseResult<Forecast>>() {
                    override fun onComplete() {
                        SafeLog.v("Forecast: onComplete()")
                    }

                    override fun onNext(value: ResponseResult<Forecast>?) {
                        //apply data
                        value?.let {
                            labelMessage.set(it.exception?.getLabelMessage(getApplication()))
                            forecastItems.value = it.data?.list ?: arrayListOf()
                        }
                    }

                    override fun onError(e: Throwable?) {
                        e?.let { SafeLog.v("Forecast: onError()", it) }
                    }
                })

        //find request
        compositeDisposable += sharedObservable
                //do not perform find requests until selectedCity exists
                .filter { searchQuery.city == null }
                .switchMap { it -> findRepository.getData(parameterGenerator.generate(it)) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<ResponseResult<Find>>() {
                    override fun onComplete() {
                        SafeLog.v("Find: onComplete()")
                    }

                    override fun onNext(value: ResponseResult<Find>?) {
                        //apply data
                        value?.let {
                            //don't show suggestions if there is only one
                            citySuggestions.value = it.data?.list?.let {
                                if(it.size == 1) arrayListOf() else it
                            } ?: arrayListOf()
                        }
                    }

                    override fun onError(e: Throwable?) {
                        e?.let { SafeLog.v("Find: onError()", it) }
                    }
                })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun getCurrentSearchQuery() : SearchQuery = searchQuery
}