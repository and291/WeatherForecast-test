package pro.busik.test.weather.model.repository

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pro.busik.test.weather.model.exceptions.EmptySearchQueryException
import pro.busik.test.weather.model.Forecast
import pro.busik.test.weather.model.ForecastResponse
import pro.busik.test.weather.model.ForecastCached
import pro.busik.test.weather.model.exceptions.NoInternetConnectionException
import pro.busik.test.weather.refrofit.Api
import pro.busik.test.weather.refrofit.ServiceGenerator
import pro.busik.test.weather.utils.SafeLog
import javax.inject.Inject
import javax.inject.Singleton

class ForecastRepository @Inject constructor(private val netManager: NetManager) {
    @Inject lateinit var remoteDataSource: ForecastRemoteDataSource
    @Inject lateinit var localDataSource: ForecastLocalDataSource

    fun getForecast(query: String) : Observable<ForecastResponse> {
        //always set exception for empty search query
        if(query.isEmpty()){
            return Observable.just(ForecastResponse(EmptySearchQueryException()))
        }

        return localDataSource.tryGetFromCache(query) ?:
            if(!netManager.isConnectedToInternet){
                //set exception for network request if there is no internet connection
                Observable.just(ForecastResponse(NoInternetConnectionException()))
            } else {
                remoteDataSource.requestFromServer(query)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext{
                            it.forecast?.let {
                                localDataSource.save(query, it)
                            }
                        }
            }
    }
}

@Singleton
class ForecastLocalDataSource @Inject constructor(){
    private val lifetime = 300_000.0
    private var lastReceivedForecast: ForecastCached? = null

    fun tryGetFromCache(query: String) : Observable<ForecastResponse>? {
        lastReceivedForecast?.let {
            if(it.isFits(query, lifetime)){
                SafeLog.v("Forecast is taken from cache: $it")
                return it.getFromCache()
            }
        }
        return null
    }

    fun save(query: String, forecast: Forecast) {
        SafeLog.v("Forecast saved to cache: $forecast")
        lastReceivedForecast = ForecastCached(query, forecast)
    }
}

class ForecastRemoteDataSource @Inject constructor(){

    fun requestFromServer(query: String) : Observable<ForecastResponse> {
        val api = ServiceGenerator.createService(Api::class.java)
        return api.forecast(query)
                .subscribeOn(Schedulers.io())
                .flatMap {
                    SafeLog.v("Forecast request: api response converted to ForecastResponse")
                    return@flatMap Observable.just(ForecastResponse(it))
                }
                .onErrorReturn {
                    //SafeLog.v("Forecast request: Error on api request", it)
                    return@onErrorReturn ForecastResponse(it)
                }
    }
}