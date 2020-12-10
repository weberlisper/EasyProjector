package com.weber.lisper.common

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
private val timestampFormat = SimpleDateFormat("YYYYMMddHHmmssSSS")

fun getCurrentTimestamp(): String = timestampFormat.format(Date())