package pro.busik.test.weather.model.exceptions

import android.content.Context
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import pro.busik.test.weather.R

class DefinedException(cause: Throwable) : Exception(cause){
    fun getLabelMessage(context: Context) : String {
        if(cause is HttpException){
            if(cause.code() == 404){
                return context.getString(R.string.exception_city_not_found)
            }
            return context.getString(R.string.exception_server_connection_error)
        }
        if(cause is EmptySearchQueryException){
            return context.getString(R.string.exception_empty_search_query)
        }
        if(cause is NoInternetConnectionException) {
            return context.getString(R.string.exception_no_internet)
        }
        return context.getString(R.string.exception_unknown)
    }
}