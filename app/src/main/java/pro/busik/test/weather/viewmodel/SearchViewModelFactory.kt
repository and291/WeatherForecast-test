package pro.busik.test.weather.viewmodel

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import pro.busik.test.weather.model.repository.find.FindRepository
import pro.busik.test.weather.model.repository.forecast.ForecastRepository

class SearchViewModelFactory(private val application: Application,
                             private val forecastRepository: ForecastRepository,
                             private val findRepository: FindRepository)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(application, forecastRepository, findRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}
