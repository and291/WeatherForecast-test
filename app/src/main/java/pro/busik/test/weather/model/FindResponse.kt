package pro.busik.test.weather.model

data class FindResponse(val cod: Int,
                        val count: Int,
                        val list: ArrayList<City>?,
                        val message: String)