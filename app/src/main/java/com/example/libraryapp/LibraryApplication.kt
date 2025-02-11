package com.example.libraryapp

import android.app.Application
import com.example.libraryapp.util.PrefUtil
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LibraryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PrefUtil.initPref(this)
        FirebaseApp.initializeApp(this)
    }
}