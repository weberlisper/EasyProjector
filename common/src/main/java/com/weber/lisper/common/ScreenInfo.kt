package com.weber.lisper.common

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics


data class ScreenInfo(
    val width: Int,
    val height: Int,
    val densityDpi: Int
)

fun Context.getScreenInfo(): ScreenInfo {
    val resources: Resources = this.resources
    val dm: DisplayMetrics = resources.displayMetrics
    return ScreenInfo(dm.widthPixels, dm.heightPixels, dm.densityDpi)
}