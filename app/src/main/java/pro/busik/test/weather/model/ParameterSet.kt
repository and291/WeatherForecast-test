package pro.busik.test.weather.model

import pro.busik.test.weather.model.data.apiresponse.ApiResponse

data class ParameterSet<T : ApiResponse<T>>(val map: Map<String, String>)