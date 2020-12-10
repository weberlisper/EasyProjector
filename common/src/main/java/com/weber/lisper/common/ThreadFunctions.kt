package com.weber.lisper.common

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

private val executor = Executors.newCachedThreadPool()
private val handler = Handler(Looper.getMainLooper())

fun runOnBackground(runnable: Runnable) {
    executor.submit(runnable)
}

fun runOnBackground(runnable: ()->Unit) {
    executor.submit(runnable)
}

fun runOnUI(runnable: Runnable) {
    handler.post(runnable)
}

fun runOnUI(runnable: ()->Unit) {
    handler.post(runnable)
}

fun delayOnUI(runnable: Runnable, delay: Long) {
    handler.postDelayed(runnable, delay)
}

fun delayOnUI(runnable: ()->Unit, delay: Long) {
    handler.postDelayed(runnable, delay)
}
