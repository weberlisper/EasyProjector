package com.weber.lisper.sender.service

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.weber.lisper.common.*
import com.weber.lisper.sender.R
import com.weber.lisper.sender.capture.ScreenCapture
import java.io.File


/**
 * 投屏服务
 */
private const val TAG = "MediaProjectService"

class MediaProjectService : Service() {
    private lateinit var screenInfo: ScreenInfo

    override fun onCreate() {
        super.onCreate()
        screenInfo = getScreenInfo()
    }

    override fun onBind(intent: Intent): IBinder {
        createForegroundNotification(intent)
        return MediaProjectBinder()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createForegroundNotification(intent)
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

    /**
     * 开始录屏
     */
    fun startRecord() {
        var fileSaver: FileSaver? = null
        ScreenCapture.instance.setOnScreenCaptureListener(object :
            ScreenCapture.OnScreenCaptureListener {
            override fun onCapture(data: ByteArray) {
                Logger.i(TAG, "onEncoded: video data size: " + data.toHex())
                fileSaver?.append(data)
            }

            override fun onStart() {
                fileSaver = FileSaver(
                    FileSaver.FILE_CREATE_NEW,
                    buildFilePath()
                )
            }
        })
        startCaptureScreen()
    }

    /**
     * 开始投屏
     */
    fun startCast() {
        // TODO 设置投屏的监听
        startCaptureScreen()
    }

    private fun startCaptureScreen() {
        ScreenCapture.instance.start(
            this,
            screenInfo.width,
            screenInfo.height,
            screenInfo.densityDpi
        )
    }

    fun stop() {
        ScreenCapture.instance.stop()
    }

    inner class MediaProjectBinder : Binder() {
        fun getService(): MediaProjectService {
            return this@MediaProjectService;
        }
    }
}

private fun buildFilePath(): String {
    val fileName = "easy-projector-${getCurrentTimestamp()}"
    val dirPath =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "EasyProjector"
    val dir = File(dirPath)
    if (!dir.exists()) {
        dir.mkdirs()
    }
    return dirPath + File.separator + "${fileName}.h264"
}