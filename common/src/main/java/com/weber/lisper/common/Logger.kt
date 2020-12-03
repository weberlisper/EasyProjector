package com.weber.lisper.common

import android.text.TextUtils
import android.util.Log
import java.lang.Exception

private const val COMMON_TAG = "EasyProjector"

object Logger {

    fun i(tag: String, msg: String) {
        Log.i(buildTag(tag), msg)
    }

    fun i(tag: String, msg: String, e: Exception) {
        Log.i(buildTag(tag), msg, e)
    }

    fun w(tag: String, msg: String) {
        Log.w(buildTag(tag), msg)
    }

    fun w(tag: String, msg: String, e: Exception) {
        Log.w(buildTag(tag), msg, e)
    }

    private fun buildTag(tag: String) = if (TextUtils.isEmpty(tag)) {
        COMMON_TAG
    } else {
        "$COMMON_TAG : $tag"
    }

}