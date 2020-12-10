package com.weber.lisper.sender.capture

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.weber.lisper.common.Logger
import com.weber.lisper.common.runOnBackground
import java.nio.ByteBuffer

private const val TAG = "PcmCapture"

private const val SAMPLE_RATE = 44100
private const val CHANNEL = AudioFormat.CHANNEL_IN_STEREO
private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT

class PcmCapture : Runnable {

    interface OnPcmCaptureListener {
        fun onCapture(pcmData: ByteArray)

        fun onStart() {

        }

        fun onStop() {

        }
    }

    private var minBufferSize = 0
    private lateinit var audioRecord: AudioRecord

    private var listener: OnPcmCaptureListener? = null

    @Volatile
    private var hasInCapturing = false

    fun setOnAudioCaptureListener(listener: OnPcmCaptureListener) {
        if (hasInCapturing) {
            Logger.w(
                TAG,
                "setOnAudioCaptureListener: PcmCapture has in capturing, cannot reset OnAudioCaptureListener"
            )
            return
        }
        this.listener = listener
    }

    fun start() {
        if (hasInCapturing) {
            Logger.w(
                TAG,
                "start: PcmCapture has in capturing, should stop it first"
            )
            return
        }

        minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, ENCODING, minBufferSize
        )
        runOnBackground(this)
    }


    override fun run() {
        audioRecord.startRecording()
        val audioData = ByteBuffer.allocate(minBufferSize)
        while (hasInCapturing) {
            val size = audioRecord.read(audioData, 0)
            if (size > 0) {
                
            }
        }
    }

    fun stop() {
        if (!hasInCapturing) {
            Logger.w(
                TAG,
                "stop: PcmCapture not in capturing"
            )
            return
        }
        hasInCapturing = false
    }
}