package com.example.internetapi.functions

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts


fun getResultFromActiviy(
    activity: ComponentActivity,
    function: (ActivityResult) -> Unit
): ActivityResultLauncher<Intent> {
    return activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            function.invoke(result)
        }
    }
}