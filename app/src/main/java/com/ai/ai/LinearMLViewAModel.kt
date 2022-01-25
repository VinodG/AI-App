package com.ai.ai

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.ai.data.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject

@HiltViewModel
class LinearMLViewAModel @Inject constructor(
    @ApplicationContext var context: Context,
    var repo: Repo
) : ViewModel() {
    private var interpreter = Interpreter(loadModelFromAsset()!!, 1)

    var isLoading = mutableStateOf(false)

    private fun loadModelFromAsset(): MappedByteBuffer? {
        var assetFileDescriptor = context.assets.openFd("linear2.tflite")
        var fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        var fileChannel = fileInputStream.channel
        var startOffSet = assetFileDescriptor.startOffset
        var length = assetFileDescriptor.length
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffSet, length)
    }

    fun loadFromAsset() {
        interpreter = Interpreter(loadModelFromAsset()!!, 1)
    }

    fun doInference(predict_x: Float): Float {
        var floatArray = floatArrayOf(1.0f)
        floatArray[0] = predict_x
        var outarray = Array(1) { floatArray }
//        var floatArray = StaticType.get1Dim()
//        floatArray[0] = predict_x
//        var outarray = StaticType.get2Dim()
        interpreter.run(floatArray, outarray)
        println(outarray[0][0])
        return outarray[0][0]
    }

    private fun loadModelFromApp(filePath: String): MappedByteBuffer? {
        var file = File(filePath);
        var mappedByteBuffer: MappedByteBuffer? = null
        try {
            var inputStream: FileInputStream = FileInputStream(file);
            try {
                var fileChannel: FileChannel = inputStream.channel;
                mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                inputStream.close()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace();
        }
        return mappedByteBuffer
    }

    fun downloadModel() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true
            try {
                repo.download()?.let {
                    interpreter = Interpreter(loadModelFromApp(it)!!, 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Not downloaded")
            }
            isLoading.value = false
        }

    }

}