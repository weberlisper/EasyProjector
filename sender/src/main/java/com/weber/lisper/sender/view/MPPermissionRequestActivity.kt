package com.weber.lisper.sender.view

import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.weber.lisper.common.Logger
import com.weber.lisper.sender.R
import com.weber.lisper.sender.constant.EXTRA_DATA_FOR_SCREEN_CAPTURE
import com.weber.lisper.sender.constant.REQUEST_CODE_FOR_SCREEN_CAPTURE
import com.weber.lisper.sender.screencapture.ScreenCapture
import com.weber.lisper.sender.service.MediaProjectService

private const val TAG = "MPPermissionRequest"

class MPPermissionRequestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                ScreenCapture.instance.start(this, data!!)
            } else {
                Logger.w(TAG, "onActivityResult: request permission for screen capture refused")
                ScreenCapture.instance.rejectPermission()
            }
        }
        finish()
    }
}