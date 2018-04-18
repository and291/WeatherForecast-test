package pro.busik.test.weather.model

import android.content.Context
import pro.busik.test.weather.R
import pro.busik.test.weather.model.data.City
import pro.busik.test.weather.model.data.apiresponse.Find
import pro.busik.test.weather.model.data.apiresponse.Forecast
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParameterGenerator @Inject constructor(context: Context) {

    private val commonMap: Map<String, String> = mapOf(
            "appid" to "0a49791459a25a1ec88fb1893630d971",
            "lang" to context.getString(R.string.api_lang_parameter_value),
            "mode" to "json",
            "units" to "metric"
    )

    fun generate(query: String, city: City?) : ParameterSet<Forecast> {
        val pair = when {
            city != null -> Pair("id", city.id.toString())
            else -> Pair("q", query)
        }

        val map = commonMap.toMutableMap()
                .plus(pair)
        return ParameterSet(map)
    }

    fun generate(query: String) : ParameterSet<Find> {
        val map = commonMap.toMutableMap()
                .plus(Pair("q", query))
        return ParameterSet(map)
    }
}