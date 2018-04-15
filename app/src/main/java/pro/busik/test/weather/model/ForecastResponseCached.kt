package pro.busik.test.weather.model

import io.reactivex.Observable
import java.util.*

data class ForecastResponseCached(private val query: String,
                                  private val forecastResponse: ForecastResponse){
    private val cacheDate: Date = Calendar.getInstance().time

    fun isFits(newQuery: String, timeout: Double) : Boolean {
        return this.query == newQuery &&
                Calendar.getInstance().timeInMillis - cacheDate.time < timeout
    }

    fun getAsObservable(): Observable<ForecastResponse> = Observable.just(forecastResponse)
}