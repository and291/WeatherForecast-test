package pro.busik.test.weather.model

import pro.busik.test.weather.model.exceptions.DefinedException

data class ForecastResponse(val forecast: Forecast?,
                            val exception: DefinedException?){

    constructor(forecast: Forecast) : this(forecast, null)

    constructor(throwable: Throwable) : this(null, DefinedException(throwable))
}