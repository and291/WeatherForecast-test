package pro.busik.test.weather.model.repository

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import pro.busik.test.weather.model.ForecastResponse
import pro.busik.test.weather.refrofit.Api
import pro.busik.test.weather.refrofit.ServiceGenerator
import pro.busik.test.weather.utils.SafeLog
import javax.inject.Inject

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