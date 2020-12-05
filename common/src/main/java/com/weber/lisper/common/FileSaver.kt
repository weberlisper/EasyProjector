package com.weber.lisper.common

import androidx.annotation.IntDef
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

class FileSaver(
    @FileSaveMode private val saveMode: Int,
    filePath: String
) {

    private val singleExecutor = Executors.newSingleThreadExecutor()

    private val file: File = File(filePath).apply {
        if (saveMode == FILE_CREATE_NEW) {
            if (exists()) {
                delete()
            }
        }
        if (!exists()) {
            createNewFile()
        }
    }

    /**
     * 往后面追加数据
     */
    fun append(data: ByteArray) {
        singleExecutor.submit {
            var bos: BufferedOutputStream? = null
            try {
                bos = BufferedOutputStream(FileOutputStream(file))
                bos.write(data)
                bos.flush()
            } catch (e: Exception) {
                try {
                    bos?.close()
                } catch (ignore: Exception) {
                }
            }
        }
    }

    companion object {
        const val FILE_APPEND = 1
        const val FILE_CREATE_NEW = 2
    }
}

@IntDef(FileSaver.FILE_APPEND, FileSaver.FILE_CREATE_NEW)
annotation class FileSaveMode