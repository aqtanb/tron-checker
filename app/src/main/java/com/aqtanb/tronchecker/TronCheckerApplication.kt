package com.aqtanb.tronchecker

import android.app.Application
import com.aqtanb.tronchecker.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class TronCheckerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TronCheckerApplication)
            modules(appModule)
        }
    }
}