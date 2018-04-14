package pro.busik.test.weather.views

import android.view.View
import android.widget.TextView
import pro.busik.test.weather.R
import pro.busik.test.weather.model.ForecastItem

class ForecastItemView(private val itemView: View) {

    private val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
    private val tvTemperature = itemView.findViewById<TextView>(R.id.tvTemperature)

    fun bind(forecastItem: ForecastItem) {
        tvDate.text = forecastItem.dt.toString()
        tvTemperature.text = forecastItem.main.temp.toString()
    }
}