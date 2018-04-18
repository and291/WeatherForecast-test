package pro.busik.test.weather.model.repository.find

import pro.busik.test.weather.model.data.apiresponse.Find
import pro.busik.test.weather.model.repository.NetManager
import pro.busik.test.weather.model.ParameterSet
import pro.busik.test.weather.model.ParameterGenerator
import pro.busik.test.weather.model.repository.Repository
import javax.inject.Inject

class FindRepository @Inject constructor(
        parameterGenerator: ParameterGenerator,
        netManager: NetManager,
        localDataSource: FindLocalDataSource,
        remoteDataSource: FindRemoteDataSource
) : Repository<Find>(netManager, localDataSource, remoteDataSource) {
    override val emptyParameterSet: ParameterSet<Find>
            = parameterGenerator.generate("")
}