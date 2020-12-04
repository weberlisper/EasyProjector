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
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.weber.lisper.common.Logger
import com.weber.lisper.sender.constant.EXTRA_DATA_FOR_SCREEN_CAPTURE
import com.weber.lisper.sender.encoder.H264Encoder
import java.nio.ByteBuffer


/**
 * 投屏服务
 */
private const val TAG = "ProjectService"

class ProjectService : Service() {

    private lateinit var mediaProjection: MediaProjection
    private lateinit var h264Encoder: H264Encoder
    private lateinit var virtualDisplay: VirtualDisplay

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createForegroundNotification(intent)

        initMediaProjection(intent)
        startVideoEncoder()
        initVirtualDisplay()
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
        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
    }

    private fun startVideoEncoder() {
        h264Encoder = H264Encoder()
        h264Encoder.start(1980, 1080, object : H264Encoder.OnEncodeListener {
            override fun onEncoded(data: ByteBuffer) {
                Logger.i(TAG, "onEncoded: ")
            }
        })
    }

    private fun initVirtualDisplay() {
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "EasyProjector", 1980, 1080, 320, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            h264Encoder.inputSurface, null, null
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