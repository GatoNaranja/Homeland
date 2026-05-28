package gato.naranja.homeland

import android.app.Application

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(
            GlobalExceptionHandler(this, defaultHandler)
        )
    }
}