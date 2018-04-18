package pro.busik.test.weather.model.repository

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import pro.busik.test.weather.model.data.apiresponse.ApiResponse
import pro.busik.test.weather.model.ResponseResult
import pro.busik.test.weather.model.ParameterSet
import pro.busik.test.weather.refrofit.ServiceGenerator
import pro.busik.test.weather.utils.SafeLog

abstract class RemoteDataSource<T : ApiResponse<T>> constructor(
        private val serviceGenerator: ServiceGenerator
){

    fun requestFromServer(query: ParameterSet<T>) : Observable<ResponseResult<T>> {

        return getObservable(serviceGenerator, query)
                .subscribeOn(Schedulers.io())
                .flatMap {
                    SafeLog.v("Forecast request: api response converted to ForecastResponse")
                    return@flatMap Observable.just(it.create(it))
                }
                .onErrorReturn {
                    //SafeLog.v("Forecast request: Error on api request", it)
                    return@onErrorReturn ResponseResult<T>(it)
                }
    }

    protected abstract fun getObservable(serviceGenerator: ServiceGenerator, query: ParameterSet<T>) : Observable<T>
}