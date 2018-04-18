package pro.busik.test.weather.model.repository

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import pro.busik.test.weather.model.ResponseResult
import pro.busik.test.weather.model.data.ParameterSet
import pro.busik.test.weather.model.exceptions.EmptySearchQueryException
import pro.busik.test.weather.model.exceptions.NoInternetConnectionException

abstract class Repository<T> constructor(
        private val netManager: NetManager,
        private val localDataSource: LocalDataSource<T>,
        private val remoteDataSource: RemoteDataSource<T>
) {
    abstract val emptyParameterSet: ParameterSet<T>

    fun getData(query: ParameterSet<T>) : Observable<ResponseResult<T>> {
        //always set exception for empty search query
        if(query == emptyParameterSet){
            return Observable.just(ResponseResult(EmptySearchQueryException()))
        }

        return localDataSource.tryGetFromCache(query) ?:
        if(!netManager.isConnectedToInternet){
            //set exception for network request if there is no internet connection
            Observable.just(ResponseResult(NoInternetConnectionException()))
        } else {
            remoteDataSource.requestFromServer(query)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext{
                        it.data?.let {
                            localDataSource.save(query, it)
                        }
                    }
        }
    }
}