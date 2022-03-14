package com.ai.ai.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject


class Repo @Inject constructor(var api: Api, @ApplicationContext var context: Context) {
    suspend fun download(): String? {
        var filePath: String? = null
        api.downloadLocalV4("2").apply {
            if (isSuccessful) {
                var input: InputStream? = body()?.byteStream()
                val file = File(context.filesDir, "linearx.tflite")
                val fos = FileOutputStream(file)
                fos.use { output ->
                    val buffer = ByteArray(4 * 1024) // or other buffer size
                    var read: Int = 0
                    while (input?.read(buffer)?.also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
                fos.close()
                filePath = file.absolutePath
            }
        }
        println("filepath : $filePath")
        return filePath
    }
}