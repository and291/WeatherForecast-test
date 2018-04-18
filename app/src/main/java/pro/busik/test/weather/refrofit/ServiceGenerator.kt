package pro.busik.test.weather.refrofit

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceGenerator @Inject constructor() {

    private val apiBaseUrl = "http://api.openweathermap.org/"
    private val gsonBuilder = GsonBuilder()
            .registerTypeAdapter(Date::class.java, JsonDeserializer<Date> { json, _, _ ->
                Date(json!!.asLong * 1000)
            })

    private val httpClientBuilder = OkHttpClient.Builder()
    private val retrofitBuilder = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
            .baseUrl(apiBaseUrl)

    fun <S> createService(serviceClass: Class<S>): S {
        val client = httpClientBuilder.build()
        val retrofit = retrofitBuilder.client(client).build()
        return retrofit.create(serviceClass)
    }
}