package com.weber.lisper.common

import kotlin.experimental.and

fun ByteArray.toHex(): String {
    val sb = StringBuffer()
    for (i in 0 until size) {
        val hex = Integer.toHexString((this[i] and 0xFF.toByte()).toInt())
        if (hex.length < 2) {
            sb.append(0)
        }
        sb.append(hex)
    }
    return sb.toString()
}