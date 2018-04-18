package pro.busik.test.weather.model

import pro.busik.test.weather.model.exceptions.DefinedException

class ResponseResult<T>{
    var data: T? = null
    var exception: DefinedException? = null

    constructor(data: T) {
        this.data = data
    }

    constructor(throwable: Throwable){
        exception = DefinedException(throwable)
    }
}