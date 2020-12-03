package com.weber.lisper.sender.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface
import com.weber.lisper.common.Logger
import com.weber.lisper.common.runOnBackground
import java.nio.ByteBuffer

/**
 * H264编码器，通过Surface作为输入，ByteBuffer作为输出
 */
private const val TAG = "H264Encoder"

private const val COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
private const val FRAME_RATE = 30
private const val BIT_RATE = 6 * 1000 * 1000
private const val I_FRAME_INTERVAL = 100

class H264Encoder() {

    interface OnEncodeListener {
        fun onEncoded(data: ByteBuffer)
    }

    private lateinit var _surface: Surface

    /**
     * 作为输入的Surface
     */
    val inputSurface: Surface by lazy { _surface }

    private lateinit var mediaCodec: MediaCodec

    private var onEncodeListener: OnEncodeListener? = null

    /**
     * 判断是否正在运行中
     */
    @Volatile
    private var isRunning = false

    fun start(width: Int, height: Int, onEncodeListener: OnEncodeListener) {
        if (isRunning) {
            Logger.i(TAG, "start: encoder is running")
            return
        }

        this.onEncodeListener = onEncodeListener

        // 初始化编码器
        initMediaCodec(width, height)

        // 开始编码数据
        runOnBackground {
            isRunning = true
            realEncode()
        }
    }

    private fun initMediaCodec(width: Int, height: Int) {
        val mediaFormat = initMediaFormat(width, height)
        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        _surface = mediaCodec.createInputSurface()
        mediaCodec.start()
    }

    private fun initMediaFormat(width: Int, height: Int) =
        MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            // 1、设置色彩空间
            setInteger(MediaFormat.KEY_COLOR_FORMAT, COLOR_FORMAT)
            // 2、设置帧率
            setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
            // 3、设置比特率
            setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
            // 4、设置I帧间长度
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
        }


    private fun realEncode() {
        Logger.i(TAG, "realEncode: start encode")
        while (isRunning) {
            val bufferInfo = MediaCodec.BufferInfo()
            val outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, -1)
            if (outputIndex >= 0) {
                val data = mediaCodec.getOutputBuffer(outputIndex)
                data?.let {
                    onEncodeListener?.onEncoded(it)
                }
                mediaCodec.releaseOutputBuffer(outputIndex, false)
            }
        }
    }

    fun stop() {
        isRunning = false
        mediaCodec.stop()
        mediaCodec.release()
    }
}