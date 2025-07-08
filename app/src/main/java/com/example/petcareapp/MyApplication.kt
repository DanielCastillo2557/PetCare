package com.example.petcareapp

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config: HashMap<String, String> = HashMap()
        config["cloud_name"] = "dt25xxciq"
        config["api_key"] = "359533342217855"
        config["api_secret"] = "oSGo8FcSh_HWYkvL0RfVQx2s1lg"

        MediaManager.init(this, config)
    }
}
