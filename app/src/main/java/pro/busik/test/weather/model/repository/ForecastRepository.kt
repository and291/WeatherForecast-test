package pro.busik.test.weather.model.repository

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import pro.busik.test.weather.model.exceptions.EmptySearchQueryException
import pro.busik.test.weather.model.ForecastResponse
import pro.busik.test.weather.model.exceptions.NoInternetConnectionException
import javax.inject.Inject

class ForecastRepository @Inject constructor(
        private val netManager: NetManager,
        private val localDataSource: ForecastLocalDataSource,
        private val remoteDataSource: ForecastRemoteDataSource) {

    fun getForecast(query: String) : Observable<ForecastResponse> {
        //always set exception for empty search query
        if(query.isEmpty()){
            return Observable.just(ForecastResponse(EmptySearchQueryException()))
        }

        return localDataSource.tryGetFromCache(query) ?:
            if(!netManager.isConnectedToInternet){
                //set exception for network request if there is no internet connection
                Observable.just(ForecastResponse(NoInternetConnectionException()))
            } else {
                remoteDataSource.requestFromServer(query)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext{
                            it.forecast?.let {
                                localDataSource.save(query, it)
                            }
                        }
            }
    }
}