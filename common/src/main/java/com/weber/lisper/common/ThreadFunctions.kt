package com.weber.lisper.common

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

private val executor = Executors.newFixedThreadPool(5)
private val handler = Handler(Looper.getMainLooper())

fun runOnBackground(runnable: Runnable) {
    executor.submit(runnable)
}

fun runOnUI(runnable: Runnable) {
    handler.post(runnable)
}

fun delayOnUI(runnable: Runnable, delay: Long) {
    handler.postDelayed(runnable, delay)
}
