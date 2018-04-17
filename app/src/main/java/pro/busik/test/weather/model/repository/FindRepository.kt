package pro.busik.test.weather.model.repository

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import pro.busik.test.weather.model.FindResponse
import pro.busik.test.weather.refrofit.Api
import pro.busik.test.weather.refrofit.ServiceGenerator
import pro.busik.test.weather.utils.SafeLog
import javax.inject.Inject

class FindRepository @Inject constructor(private val serviceGenerator: ServiceGenerator) {
    fun getFind(query: String) : Observable<FindResponse> {
        val api = serviceGenerator.createService(Api::class.java)
        return api.find(query)
                .subscribeOn(Schedulers.io())
                .flatMap {
                    SafeLog.v("Find request: api response converted to FindResponse")
                    return@flatMap Observable.just(FindResponse(it))
                }
                .onErrorReturn {
                    return@onErrorReturn FindResponse(it)
                }
    }
}