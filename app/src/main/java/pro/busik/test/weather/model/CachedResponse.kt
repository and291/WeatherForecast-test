package pro.busik.test.weather.model

import io.reactivex.Observable
import pro.busik.test.weather.model.data.ParameterSet
import java.util.*

class CachedResponse<T>(
        private val parameterSet: ParameterSet<T>,
        private val apiResponse: T
){
    private val cacheDate: Date = Calendar.getInstance().time

    fun isFits(newParameterSet: ParameterSet<T>, timeout: Double) : Boolean {
        return parameterSet == newParameterSet &&
                Calendar.getInstance().timeInMillis - cacheDate.time < timeout
    }

    fun getResponseResult(): Observable<ResponseResult<T>> = Observable.just(ResponseResult(apiResponse))
}