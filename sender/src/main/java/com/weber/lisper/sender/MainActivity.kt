package com.weber.lisper.sender

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.weber.lisper.common.Logger
import com.weber.lisper.sender.constant.EXTRA_DATA_FOR_SCREEN_CAPTURE
import com.weber.lisper.sender.constant.REQUEST_CODE_FOR_SCREEN_CAPTURE

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestMediaProjectionPermission()
    }

    private fun requestMediaProjectionPermission() {
        val mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(intent, REQUEST_CODE_FOR_SCREEN_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FOR_SCREEN_CAPTURE) {
            if (resultCode == RESULT_OK) {
                val intent = Intent(this, ProjectService::class.java)
                intent.putExtra(EXTRA_DATA_FOR_SCREEN_CAPTURE, data)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                }
            } else {
                Logger.w(TAG, "onActivityResult: request permission for screen capture refused")
            }
        }
    }
}