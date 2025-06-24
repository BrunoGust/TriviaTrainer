package com.example.triviatrainerapp

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class MyApplication : Application(), ViewModelStoreOwner {

    // ✅ Propiedad requerida por ViewModelStoreOwner
    override val viewModelStore: ViewModelStore by lazy {
        ViewModelStore()
    }
}
