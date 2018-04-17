package pro.busik.test.weather.model

import java.text.SimpleDateFormat
import java.util.*

data class ForecastItem(val dt: Date,
                        val main: ForecastItemMain) {
    fun getFormattedDate() : String
            = SimpleDateFormat("HH:mm - dd MMMM ", Locale.getDefault()).format(dt)
}