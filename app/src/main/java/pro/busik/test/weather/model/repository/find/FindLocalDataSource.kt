package pro.busik.test.weather.model.repository.find

import pro.busik.test.weather.model.data.apiresponse.Find
import pro.busik.test.weather.model.repository.LocalDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FindLocalDataSource @Inject constructor() : LocalDataSource<Find>()