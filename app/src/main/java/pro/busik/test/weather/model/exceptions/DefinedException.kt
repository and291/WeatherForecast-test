package pro.busik.test.weather.model.exceptions

import android.content.Context
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import pro.busik.test.weather.R

class DefinedException(cause: Throwable) : Exception(cause){
    fun getLabelMessage(context: Context) : String {
        //define proper string resource
        val stringId = when (cause) {
            is HttpException -> {
                if(cause.code() == 404)
                    R.string.exception_city_not_found
                R.string.exception_server_connection_error
            }
            is EmptySearchQueryException -> R.string.exception_empty_search_query
            is NoInternetConnectionException -> R.string.exception_no_internet
            else -> R.string.exception_unknown
        }

        //return proper text
        return context.getString(stringId)
    }
}