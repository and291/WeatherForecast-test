package pro.busik.test.weather.model.repository

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pro.busik.test.weather.model.ForecastResponse
import pro.busik.test.weather.model.ForecastResponseCached
import pro.busik.test.weather.refrofit.Api
import pro.busik.test.weather.refrofit.ServiceGenerator
import pro.busik.test.weather.utils.SafeLog

class ForecastRepository{
    private val remoteDataSource = ForecastRemoteDataSource()

    fun getForecast(query: String) : Observable<ForecastResponse> {
        //always empty response for empty search query
        if(query.isEmpty()){
            return Observable.just(ForecastResponse.getEmptyResponse())
        }

        return ForecastLocalDataSource.tryGetFromCache(query)
                ?: remoteDataSource.requestFromServer(query)
                        .switchMap {
                            ForecastLocalDataSource.saveForecast(query, it)
                            return@switchMap Observable.just(it)
                        }
    }
}

object ForecastLocalDataSource {
    private const val timeout = 300_000.0
    private var lastResponse: ForecastResponseCached? = null

    fun tryGetFromCache(query: String) : Observable<ForecastResponse>? {
        return if(lastResponse?.isFits(query, timeout) == true) lastResponse?.getAsObservable() else null
    }

    fun saveForecast(query: String, forecastResponse: ForecastResponse) {
        lastResponse = ForecastResponseCached(query, forecastResponse)
    }
}

class ForecastRemoteDataSource {

    fun requestFromServer(query: String) : Observable<ForecastResponse> {
        val api = ServiceGenerator.createService(Api::class.java)
        return api.forecast(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    SafeLog.v("Error occurred", it)
                }
                .onErrorResumeNext(Observable.just(ForecastResponse.getEmptyResponse()))
    }
}