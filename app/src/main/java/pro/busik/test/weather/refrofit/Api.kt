package pro.busik.test.weather.refrofit

import io.reactivex.Observable
import pro.busik.test.weather.model.FindResponse
import pro.busik.test.weather.model.ForecastResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface Api {
    @Headers("Content-Type: text/json; charset=utf-8")
    @GET("data/2.5/find?type=like&lang=ru&mode=json&appid=0a49791459a25a1ec88fb1893630d971")
    fun find(@Query("q") query: String) : Observable<FindResponse>

    @Headers("Content-Type: text/json; charset=utf-8")
    @GET("/data/2.5/forecast?units=metric&lang=ru&mode=json&appid=0a49791459a25a1ec88fb1893630d971")
    fun forecast(@Query("q") query: String) : Observable<ForecastResponse>
}

