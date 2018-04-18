package pro.busik.test.weather.model.repository.forecast

import pro.busik.test.weather.model.data.apiresponse.Forecast
import pro.busik.test.weather.model.repository.NetManager
import pro.busik.test.weather.model.ParameterSet
import pro.busik.test.weather.model.ParameterGenerator
import pro.busik.test.weather.model.repository.Repository
import javax.inject.Inject

class ForecastRepository @Inject constructor(
        parameterGenerator: ParameterGenerator,
        netManager: NetManager,
        localDataSource: ForecastLocalDataSource,
        remoteDataSource: ForecastRemoteDataSource
) : Repository<Forecast>(netManager, localDataSource, remoteDataSource) {
    override val emptyParameterSet: ParameterSet<Forecast>
            = parameterGenerator.generate("", null)
}

