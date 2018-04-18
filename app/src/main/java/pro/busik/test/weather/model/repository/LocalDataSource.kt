package pro.busik.test.weather.model.repository

import io.reactivex.Observable
import pro.busik.test.weather.model.data.apiresponse.ApiResponse
import pro.busik.test.weather.model.CachedResponse
import pro.busik.test.weather.model.ResponseResult
import pro.busik.test.weather.model.ParameterSet
import pro.busik.test.weather.utils.SafeLog

abstract class LocalDataSource<T : ApiResponse<T>> {

    companion object {
        private const val lifetime = 300_000.0
    }

    private var mLastReceivedData: CachedResponse<T>? = null

    fun tryGetFromCache(parameterSet: ParameterSet<T>) : Observable<ResponseResult<T>>? {
        mLastReceivedData?.let {
            if(it.isFits(parameterSet, lifetime)){
                SafeLog.v("Data is taken from cache: $it")
                return it.getResponseResult()
            }
        }
        return null
    }

    fun save(parameterSet: ParameterSet<T>, data: T) {
        SafeLog.v("Data saved to cache: $data")
        mLastReceivedData = CachedResponse(parameterSet, data)
    }
}