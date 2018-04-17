package pro.busik.test.weather.model

import pro.busik.test.weather.model.exceptions.DefinedException

data class FindResponse(val find: Find?,
                        val exception: DefinedException?)
{
    constructor(find: Find) : this(find, null)

    constructor(throwable: Throwable) : this(null, DefinedException(throwable))
}