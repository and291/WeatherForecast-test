package pro.busik.test.weather.model.repository

import io.reactivex.Observable
import pro.busik.test.weather.model.CachedResponse
import pro.busik.test.weather.model.ResponseResult
import pro.busik.test.weather.model.data.ParameterSet

abstract class LocalDataSource<T> {

    private val lifetime = 300_000.0
    private var mLastReceivedData: CachedResponse<T>? = null

    fun tryGetFromCache(parameterSet: ParameterSet<T>) : Observable<ResponseResult<T>>? {
        mLastReceivedData?.let {
            if(it.isFits(parameterSet, lifetime)){
                return it.getResponseResult()
            }
        }
        return null
    }

    fun save(parameterSet: ParameterSet<T>, data: T) {
        mLastReceivedData = CachedResponse(parameterSet, data)
    }
}