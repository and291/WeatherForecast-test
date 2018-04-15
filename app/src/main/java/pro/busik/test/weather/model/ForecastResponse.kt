package pro.busik.test.weather.model

data class ForecastResponse(val forecast: Forecast?,
                            val throwable: Throwable?){

    constructor(forecast: Forecast) : this(forecast, null)

    constructor(throwable: Throwable) : this(null, throwable)
}