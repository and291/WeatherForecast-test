package pro.busik.test.weather.model.data

import pro.busik.test.weather.model.data.City
import pro.busik.test.weather.model.data.ForecastItem

data class Forecast(val city: City,
                    val list: ArrayList<ForecastItem>)

