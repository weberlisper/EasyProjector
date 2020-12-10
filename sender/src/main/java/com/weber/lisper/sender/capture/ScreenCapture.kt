package com.weber.lisper.sender.capture

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import com.weber.lisper.common.Logger
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

        fun onStart() {

        }

        fun onStop() {

        }

        fun onPermissionRejected() {

        }
    }

    private var listener: OnScreenCaptureListener? = null
    private var width = 0
    private var height = 0
    private var densityDpi = 0

    private lateinit var mediaProjection: MediaProjection
    private lateinit var h264Encoder: H264Encoder
    private lateinit var virtualDisplay: VirtualDisplay

    @Volatile
    private var hasInCapturing = false

    fun setOnScreenCaptureListener(listener: OnScreenCaptureListener) {
        if (hasInCapturing) {
            Logger.w(
                TAG,
                "setOnScreenCaptureListener: ScreenCapture has in capturing, cannot reset OnScreenCaptureListener"
            )
            return
        }
        this.listener = listener
    }

    fun start(context: Context, width: Int, height: Int, densityDpi: Int) {
        if (hasInCapturing) {
            Logger.w(
                TAG,
                "start: ScreenCapture has in capturing, should stop it first"
            )
            return
        }
        hasInCapturing = true
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
        hasInCapturing = false
        listener?.onPermissionRejected()
    }

    fun stop() {
        if (!hasInCapturing) {
            Logger.w(
                TAG,
                "stop: ScreenCapture has not in capturing"
            )
            return
        }
        hasInCapturing = false
        h264Encoder.stop()
        mediaProjection.stop()
        virtualDisplay.release()
        listener?.onStop()
    }

    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { ScreenCapture() }
    }

}