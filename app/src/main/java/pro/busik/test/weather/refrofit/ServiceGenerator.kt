package pro.busik.test.weather.refrofit

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceGenerator {

    private val API_BASE_URL = "http://api.openweathermap.org/"

    private val sHttpClientBuilder = OkHttpClient.Builder()

    private val sRetrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(API_BASE_URL)

    fun <S> createService(serviceClass: Class<S>): S {
        val client = sHttpClientBuilder.build()
        val retrofit = sRetrofitBuilder.client(client).build()
        return retrofit.create(serviceClass)
    }
}