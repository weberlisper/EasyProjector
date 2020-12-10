package com.weber.lisper.sender.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.weber.lisper.sender.R
import com.weber.lisper.sender.service.MediaProjectService

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var mediaProjectService: MediaProjectService

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            mediaProjectService = (service as MediaProjectService.MediaProjectBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindService(
            Intent(this, MediaProjectService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    fun startRecord(view: View) {
        mediaProjectService.startRecord()
    }

    fun startCast(view: View) {
        mediaProjectService.startCast()
    }


    fun stop(view: View) {
        mediaProjectService.stop()
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }
}