package dev.rifqimfahmi.betterimageupload

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class ApplicationLoader: Application() {

    override fun onCreate() {
        preOnCreate()
        super.onCreate()
        postOnCreate()
    }

    private fun preOnCreate() {
        try {
            appCtx = applicationContext
        } catch (ignore: Throwable) {

        }
    }

    private fun postOnCreate() {
        if (appCtx == null) {
            appCtx = applicationContext
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        @JvmStatic
        var appCtx: Context? = null
    }
}