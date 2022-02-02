package com.example.internetapi.functions

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar

fun errorSnackBar(root: ViewGroup, data: String) {
    Snackbar.make(
        root,
        data,
        Snackbar.LENGTH_LONG
    ).show()
}

fun successSnackBar(root: ConstraintLayout, data: String) {
    Snackbar.make(
        root,
        data,
        Snackbar.LENGTH_LONG
    ).show()
}