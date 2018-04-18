package pro.busik.test.weather.model

import pro.busik.test.weather.model.data.City
import pro.busik.test.weather.model.data.apiresponse.Find
import pro.busik.test.weather.model.data.apiresponse.Forecast

class ParameterGenerator {
    companion object {
        private const val appId = "0a49791459a25a1ec88fb1893630d971"
        private val commonMap = mapOf(
                "appid" to appId,
                "lang" to "ru",
                "mode" to "json",
                "units" to "metric"
        )
    }

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