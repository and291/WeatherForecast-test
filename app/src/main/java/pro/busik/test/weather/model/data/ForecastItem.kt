package pro.busik.test.weather.model.data

import pro.busik.test.weather.model.data.ForecastItemMain
import java.text.SimpleDateFormat
import java.util.*

data class ForecastItem(val dt: Date,
                        val main: ForecastItemMain) {
    fun getFormattedDate() : String
            = SimpleDateFormat("HH:mm - dd MMMM ", Locale.getDefault()).format(dt)
}