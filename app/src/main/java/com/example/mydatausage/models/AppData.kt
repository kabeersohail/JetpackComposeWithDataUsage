package com.example.mydatausage.models

import android.graphics.drawable.Drawable

data class AppData(
    val applicationName: String,
    val packageName: String,
    val uid: Int,
    var isSystemApp: Boolean,
    val icon: Drawable,
    val sent: String,
    val received: String,
    val total: String
)