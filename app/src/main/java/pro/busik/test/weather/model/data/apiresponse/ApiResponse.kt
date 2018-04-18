package pro.busik.test.weather.model.data.apiresponse

import pro.busik.test.weather.model.ResponseResult

interface ApiResponse<T>{
    fun create(forecast: T) : ResponseResult<T> = ResponseResult(forecast)
}