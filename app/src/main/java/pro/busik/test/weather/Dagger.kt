package pro.busik.test.weather

import android.content.Context
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Module
class AppModule{

    @Provides
    fun providesContext(application: SearchApplication): Context {
        return application.applicationContext
    }
}

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, AppModule::class])
interface AppComponent : AndroidInjector<SearchApplication> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<SearchApplication>()
}
