package com.weber.lisper.common

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
private val timestampFormat = SimpleDateFormat("YYYYMMdd-HH-mm-ss-S")

fun getCurrentTimestamp(): String = timestampFormat.format(Date())