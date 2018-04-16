package pro.busik.test.weather.model.repository

import io.reactivex.Observable
import pro.busik.test.weather.model.Forecast
import pro.busik.test.weather.model.ForecastCached
import pro.busik.test.weather.model.ForecastResponse
import pro.busik.test.weather.utils.SafeLog
import javax.inject.Inject
import javax.inject.Singleton

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