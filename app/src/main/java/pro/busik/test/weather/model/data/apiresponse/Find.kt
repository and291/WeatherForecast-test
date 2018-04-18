package pro.busik.test.weather.model.data.apiresponse

import pro.busik.test.weather.model.data.City

data class Find(val cod: Int,
                val count: Int,
                val list: ArrayList<City>?,
                val message: String) : ApiResponse<Find>