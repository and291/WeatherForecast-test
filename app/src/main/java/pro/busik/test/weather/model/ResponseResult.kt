package pro.busik.test.weather.model

import pro.busik.test.weather.model.exceptions.DefinedException
import pro.busik.test.weather.utils.SafeLog

class ResponseResult<T>{
    var data: T? = null
    var exception: DefinedException? = null

    constructor(data: T) {
        this.data = data
        SafeLog.v("Created [ResponseResult]: $data $exception")
    }

    constructor(throwable: Throwable){
        exception = DefinedException(throwable)
        SafeLog.v("Created [ResponseResult]: $data $exception")
    }
}