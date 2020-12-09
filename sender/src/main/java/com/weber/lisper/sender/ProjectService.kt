package com.weber.lisper.sender

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.weber.lisper.common.*
import com.weber.lisper.sender.constant.EXTRA_DATA_FOR_SCREEN_CAPTURE
import com.weber.lisper.sender.encoder.H264Encoder
import java.io.File
import java.nio.ByteBuffer


/**
 * 投屏服务
 */
private const val TAG = "ProjectService"

class ProjectService : Service() {

    private lateinit var mediaProjection: MediaProjection
    private lateinit var h264Encoder: H264Encoder
    private lateinit var virtualDisplay: VirtualDisplay
    private lateinit var fileSaver: FileSaver
    private lateinit var screenInfo: ScreenInfo

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createForegroundNotification(intent)

        screenInfo = getScreenInfo()

        initMediaProjection(intent)
        startVideoEncoder()
        initVirtualDisplay()
        fileSaver = FileSaver(
            FileSaver.FILE_CREATE_NEW,
            Environment.getExternalStorageDirectory().absolutePath + File.separator + "abcd.h264"
        )
        return START_NOT_STICKY
    }

    private fun createForegroundNotification(intent: Intent) {
        val channelId = "com.weber.lisper.sender"
        val channelName = "EasyProjector"
        val notificationChannel: NotificationChannel?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.setShowBadge(true)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val notification = NotificationCompat.Builder(this, channelId)
            .setTicker("Nature")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("EasyProjector镜像中")
            .setContentText("")
            .setContentIntent(pendingIntent)
            .build()
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(1, notification)
        }
    }

    private fun startVideoEncoder() {
        h264Encoder = H264Encoder()
        h264Encoder.start(
            screenInfo.width,
            screenInfo.height,
            object : H264Encoder.OnEncodeListener {
                override fun onEncoded(dataBuffer: ByteBuffer) {
                    val data = ByteArray(dataBuffer.remaining())
                    dataBuffer.get(data)
                    Logger.i(TAG, "onEncoded: video data size: " + data.toHex())
                    fileSaver.append(data)
                }
            })
    }

    private fun initVirtualDisplay() {
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "EasyProjector",
            screenInfo.width,
            screenInfo.height,
            screenInfo.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            h264Encoder.inputSurface,
            null,
            null
        )
    }

    private fun initMediaProjection(intent: Intent) {
        val requestData: Intent =
            intent.getParcelableExtra(EXTRA_DATA_FOR_SCREEN_CAPTURE)!!
        val mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, requestData)
    }
}