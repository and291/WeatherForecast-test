package pro.busik.test.weather.model.data

import kotlin.math.roundToInt

data class ForecastItemMain(val temp: Float) {
    fun getFormattedTemperature() : String {
        val roundedTemp = temp.roundToInt()
        return String.format("%s%d °C", if(roundedTemp > 0) "+" else "", roundedTemp)
    }
}
