package pro.busik.test.weather.utils

import android.util.Log
import pro.busik.test.weather.BuildConfig

object SafeLog {
    private const val tag = "weather"

    fun v(message: String){
        if(BuildConfig.DEBUG) {
            Log.v(tag, message)
        }
    }
}