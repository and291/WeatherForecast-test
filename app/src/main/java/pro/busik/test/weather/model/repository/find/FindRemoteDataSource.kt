package pro.busik.test.weather.model.repository.find

import io.reactivex.Observable
import pro.busik.test.weather.model.data.Find
import pro.busik.test.weather.model.data.ParameterSet
import pro.busik.test.weather.model.repository.RemoteDataSource
import pro.busik.test.weather.refrofit.Api
import pro.busik.test.weather.refrofit.ServiceGenerator
import javax.inject.Inject

class FindRemoteDataSource @Inject constructor(
        serviceGenerator: ServiceGenerator
) : RemoteDataSource<Find>(serviceGenerator) {

    override fun getObservable(serviceGenerator: ServiceGenerator, query: ParameterSet<Find>): Observable<Find> {
        val api = serviceGenerator.createService(Api::class.java)
        return api.find(query.map)
    }
}