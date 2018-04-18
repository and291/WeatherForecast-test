package pro.busik.test.weather.model.repository.forecast

import io.reactivex.Observable
import pro.busik.test.weather.model.data.apiresponse.Forecast
import pro.busik.test.weather.model.ParameterSet
import pro.busik.test.weather.model.repository.RemoteDataSource
import pro.busik.test.weather.refrofit.Api
import pro.busik.test.weather.refrofit.ServiceGenerator
import javax.inject.Inject

class ForecastRemoteDataSource @Inject constructor(
        serviceGenerator: ServiceGenerator
) : RemoteDataSource<Forecast>(serviceGenerator){

    override fun getObservable(serviceGenerator: ServiceGenerator, query: ParameterSet<Forecast>): Observable<Forecast> {
        val api = serviceGenerator.createService(Api::class.java)
        return api.forecast(query.map)
    }
}

