package com.example.internetapi.functions

import android.content.Intent
import android.os.Build
import android.os.Bundle
import java.io.Serializable

fun <T : Serializable> Bundle.getSerializableCompat(key: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        getSerializable(key)?.let { value ->
            if (clazz.isInstance(value)) clazz.cast(value) else null
        }
    }
}

fun <T : Serializable> Intent.getSerializableExtraCompat(key: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        getSerializableExtra(key)?.let { value ->
            if (clazz.isInstance(value)) clazz.cast(value) else null
        }
    }
}
