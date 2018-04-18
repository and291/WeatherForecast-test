package pro.busik.test.weather.model

import io.reactivex.Observable
import pro.busik.test.weather.model.data.ParameterSet
import pro.busik.test.weather.utils.SafeLog
import java.util.*

class CachedResponse<T>(
        private val parameterSet: ParameterSet<T>,
        private val apiResponse: T
){
    init {
        SafeLog.v("Created [CachedResponse]: $apiResponse")
    }

    private val cacheDate: Date = Calendar.getInstance().time

    fun isFits(newParameterSet: ParameterSet<T>, timeout: Double) : Boolean {
        val result = parameterSet == newParameterSet &&
                Calendar.getInstance().timeInMillis - cacheDate.time < timeout
        SafeLog.v("Fits=$result [CachedResponse]: $apiResponse")
        return result
    }

    fun getResponseResult(): Observable<ResponseResult<T>> {
        SafeLog.v("Reused [CachedResponse]: $apiResponse")
        return Observable.just(ResponseResult(apiResponse))
    }
}