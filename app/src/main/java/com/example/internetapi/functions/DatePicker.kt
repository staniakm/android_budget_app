package com.example.internetapi.functions

import android.content.Context
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import android.widget.DatePicker
import java.time.LocalDate

private const val NIGHT_MODE_MASK = Configuration.UI_MODE_NIGHT_MASK
private const val NIGHT_MODE_YES = Configuration.UI_MODE_NIGHT_YES

fun DatePicker.toLocalDate(): LocalDate = LocalDate.of(this.year, this.month + 1, this.dayOfMonth)

fun resolveDatePickerThemeResId(uiMode: Int): Int? =
    if (uiMode and NIGHT_MODE_MASK == NIGHT_MODE_YES) android.R.style.Theme_DeviceDefault_Light else null

fun createDialogDatePicker(context: Context): DatePicker {
    val themeResId = resolveDatePickerThemeResId(context.resources.configuration.uiMode)
    val themedContext = if (themeResId == null) context else ContextThemeWrapper(context, themeResId)
    return DatePicker(themedContext)
}
