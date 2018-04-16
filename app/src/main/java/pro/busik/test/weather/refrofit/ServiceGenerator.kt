package pro.busik.test.weather.refrofit

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

object ServiceGenerator {

    private const val API_BASE_URL = "http://api.openweathermap.org/"

    private val gsonBuilder = GsonBuilder()
            .registerTypeAdapter(Date::class.java, JsonDeserializer<Date> { json, _, _ ->
                Date(json!!.asLong * 1000)
            })

    private val sHttpClientBuilder = OkHttpClient.Builder()

    private val sRetrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(API_BASE_URL)

    fun <S> createService(serviceClass: Class<S>): S {
        val client = sHttpClientBuilder.build()
        val retrofit = sRetrofitBuilder.client(client).build()
        return retrofit.create(serviceClass)
    }
}