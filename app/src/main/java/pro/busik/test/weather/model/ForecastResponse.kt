package pro.busik.test.weather.model

data class ForecastResponse(val city: City,
                            val list: ArrayList<ForecastItem>) {
    companion object {
        fun getEmptyResponse() = ForecastResponse(City(0, "none"), arrayListOf())
    }
}