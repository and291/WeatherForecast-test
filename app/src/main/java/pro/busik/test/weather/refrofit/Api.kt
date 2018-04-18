package pro.busik.test.weather.refrofit

import io.reactivex.Observable
import pro.busik.test.weather.model.data.apiresponse.Find
import pro.busik.test.weather.model.data.apiresponse.Forecast
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface Api {
    @GET("data/2.5/find?type=like")
    fun find(@QueryMap parameters: Map<String, String>) : Observable<Find>

    @GET("/data/2.5/forecast")
    fun forecast(@QueryMap parameters: Map<String, String>) : Observable<Forecast>
}

