package pro.busik.test.weather.model.repository

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import pro.busik.test.weather.model.ResponseResult
import pro.busik.test.weather.model.data.ParameterSet
import pro.busik.test.weather.refrofit.ServiceGenerator

abstract class RemoteDataSource<T> constructor(
        private val serviceGenerator: ServiceGenerator
){

    fun requestFromServer(query: ParameterSet<T>) : Observable<ResponseResult<T>> =
            getObservable(serviceGenerator, query)
                    .subscribeOn(Schedulers.io())
                    .flatMap { it -> Observable.just(ResponseResult<T>(it)) }
                    .onErrorReturn { it -> ResponseResult(it) }

    protected abstract fun getObservable(serviceGenerator: ServiceGenerator, query: ParameterSet<T>)
            : Observable<T>
}