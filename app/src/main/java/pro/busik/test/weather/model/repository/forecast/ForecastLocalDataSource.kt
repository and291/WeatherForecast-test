package pro.busik.test.weather.model.repository.forecast

import pro.busik.test.weather.model.data.apiresponse.Forecast
import pro.busik.test.weather.model.repository.LocalDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForecastLocalDataSource @Inject constructor() : LocalDataSource<Forecast>()

