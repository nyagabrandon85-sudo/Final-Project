package com.deepseek.rentease

import android.app.Application
import com.cloudinary.android.MediaManager

class RentEaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Cloudinary
        val config = mapOf(
            "cloud_name" to "dscyndwlo",
            "secure" to true
        )
        MediaManager.init(this, config)
    }
}
