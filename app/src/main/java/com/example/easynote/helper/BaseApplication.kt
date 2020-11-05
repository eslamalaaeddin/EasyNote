package com.example.easynote.helper

import android.app.Application
import com.example.easynote.di.databaseModule
import com.example.easynote.di.repositoryModule
import com.example.easynote.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@BaseApplication)
            modules(listOf(repositoryModule, viewModelModule, databaseModule))
        }
    }
}