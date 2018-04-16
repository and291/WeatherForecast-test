package pro.busik.test.weather

import android.content.Context
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import pro.busik.test.weather.model.repository.ForecastRepository
import pro.busik.test.weather.model.repository.NetManager
import pro.busik.test.weather.viewmodel.SearchViewModelFactory
import pro.busik.test.weather.views.MainActivity
import pro.busik.test.weather.views.SearchFragment
import javax.inject.Singleton

@Module
class AppModule{

    @Provides
    fun providesContext(application: SearchApplication): Context {
        return application.applicationContext
    }
}

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    MainActivityModule::class,
    SearchFragmentModule::class
])
interface AppComponent : AndroidInjector<SearchApplication> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<SearchApplication>()
}

@Module
internal abstract class SearchFragmentModule{

    @Module
    companion object {
        @JvmStatic
        @Provides
        internal fun providesSearchViewModelFactory(application: SearchApplication, forecastRepository: ForecastRepository)
                : SearchViewModelFactory {
            return SearchViewModelFactory(application, forecastRepository)
        }
    }

    @ContributesAndroidInjector()
    internal abstract fun searchFragment(): SearchFragment
}

@Module
internal abstract class MainActivityModule{

    @ContributesAndroidInjector()
    internal abstract fun mainActivity(): MainActivity
}
