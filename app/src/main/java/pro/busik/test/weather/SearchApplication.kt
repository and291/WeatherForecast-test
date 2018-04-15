package pro.busik.test.weather

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class SearchApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().create(this)
    }

}