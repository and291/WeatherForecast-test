package pro.busik.test.weather.refrofit

import io.reactivex.Observable
import pro.busik.test.weather.model.Forecast
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {

    @GET("/data/2.5/forecast?units=metric&lang=ru&mode=json&appid=0a49791459a25a1ec88fb1893630d971")
    fun forecast(@Query("q") query: String) : Observable<Forecast>
}

