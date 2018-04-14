package pro.busik.test.weather.model

data class ForecastResponse(val city: City,
                            val list: ArrayList<ForecastItem>)