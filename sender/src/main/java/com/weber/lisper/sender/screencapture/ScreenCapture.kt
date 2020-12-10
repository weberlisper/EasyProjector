package com.weber.lisper.sender.screencapture

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import com.weber.lisper.sender.encoder.H264Encoder
import com.weber.lisper.sender.view.MPPermissionRequestActivity
import java.nio.ByteBuffer

/**
 * 屏幕抓取器
 */
private const val TAG = "ScreenCapture"
private const val VIRTUAL_DISPLAY_NAME = "EasyProjector"

class ScreenCapture private constructor() {

    interface OnScreenCaptureListener {
        fun onCapture(data: ByteArray)

        fun onStart()

        fun onStop()

        fun onPermissionRejected()
    }

    private var listener: OnScreenCaptureListener? = null
    private var width = 0
    private var height = 0
    private var densityDpi = 0

    private lateinit var mediaProjection: MediaProjection
    private lateinit var h264Encoder: H264Encoder
    private lateinit var virtualDisplay: VirtualDisplay

    fun setOnScreenCaptureListener(listener: OnScreenCaptureListener) {
        this.listener = listener
    }

    fun start(context: Context, width: Int, height: Int, densityDpi: Int) {
        this.width = width
        this.height = height
        this.densityDpi = densityDpi
        askForPermission(context)
    }

    private fun askForPermission(context: Context) {
        val intent = Intent(context, MPPermissionRequestActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun start(context: Context, data: Intent) {
        listener?.onStart()
        initMediaProjection(context, data)
        startVideoEncoder()
        initVirtualDisplay()
    }

    private fun startVideoEncoder() {
        h264Encoder = H264Encoder()
        h264Encoder.start(
            width,
            height,
            object : H264Encoder.OnEncodeListener {
                override fun onEncoded(dataBuffer: ByteBuffer) {
                    val data = ByteArray(dataBuffer.remaining())
                    dataBuffer.get(data)
                    listener?.onCapture(data)
                }
            })
    }

    private fun initVirtualDisplay() {
        virtualDisplay = mediaProjection.createVirtualDisplay(
            VIRTUAL_DISPLAY_NAME,
            width,
            height,
            densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            h264Encoder.inputSurface,
            null,
            null
        )
    }

    private fun initMediaProjection(context: Context, data: Intent) {
        val mediaProjectionManager =
            context.getSystemService(Service.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, data)
    }

    fun rejectPermission() {
        listener?.onPermissionRejected()
    }

    fun stop() {

    }

    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { ScreenCapture() }
    }

}