package pro.busik.test.weather.model

import io.reactivex.Observable
import java.util.*

data class ForecastCached(private val query: String,
                          private val forecast: Forecast){
    private val cacheDate: Date = Calendar.getInstance().time

    fun isFits(newQuery: String, timeout: Double) : Boolean {
        return this.query == newQuery &&
                Calendar.getInstance().timeInMillis - cacheDate.time < timeout
    }

    fun getFromCache(): Observable<ForecastResponse> = Observable.just(ForecastResponse(forecast))
}