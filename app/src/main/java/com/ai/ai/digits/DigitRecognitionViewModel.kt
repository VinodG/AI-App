package com.ai.ai.digits

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.ai.data.Repo
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject

@HiltViewModel
class DigitRecognitionViewModel @Inject constructor(
    @ApplicationContext var context: Context,
    var repo: Repo
) : ViewModel() {
    private val OUTPUT_CLASSES_COUNT: Int = 10
    private val FLOAT_TYPE_SIZE = 4
    private var inputImageWidth = 28
    private var inputImageHeight = 28
    private val modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight
    private val TAG = "DigitRecognition"

    //    private var interpreter = Interpreter(loadModelFromAsset()!!, 1)
    private var interpreter: Interpreter? = null // Interpreter(loadModelFromAsset()!!, 1)
    private var _apiResponse: MutableStateFlow<ApiStatus> = MutableStateFlow(ApiStatus.Loading)
    var apiResponse: StateFlow<ApiStatus> = _apiResponse
    private var _probabilities: MutableStateFlow<List<Float>> = MutableStateFlow(listOf<Float>())
    var probabilities: StateFlow<List<Float>> = _probabilities


    /*private fun loadModelFromAsset(): MappedByteBuffer? {
        var assetFileDescriptor = context.assets.openFd("mnist.tflite")
        var fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        var fileChannel = fileInputStream.channel
        var startOffSet = assetFileDescriptor.startOffset
        var length = assetFileDescriptor.length
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffSet, length)
    }*/

    fun doInference(bitmap: Bitmap): Pair<FloatArray, Int> {
        return interpreter?.let { interpreter ->
            var bitmap = Bitmap.createScaledBitmap(
                bitmap,
                28,
                28,
                false
            )
            val inputShape = interpreter.getInputTensor(0).shape()
            inputImageWidth = inputShape[1]
            inputImageHeight = inputShape[2]
            val outputShape = interpreter.getOutputTensor(0).shape()

            println("$TAG :  input shape ${inputShape.size}  ->  ${inputShape[0]},  ${inputShape[1]},  ${inputShape[2]} and ${inputShape[3]}}")
            println("$TAG :  output shape ${outputShape.size}  ->  ${outputShape[0]},  ${outputShape[1]} /*,  ${inputShape[2]} and ${inputShape[3]}*/}")

            val result = Array(1) { FloatArray(OUTPUT_CLASSES_COUNT) }
            val byteBuffer = convertBitmapToByteBuffer(bitmap)
            interpreter.run(byteBuffer, result)
            viewModelScope.launch {
                _probabilities.emit(result[0].toList())
            }

            println(TAG + " ${result[0].map { it.toString() }.joinToString { "$it" }}")
            Pair(result[0], result[0].indices.maxByOrNull { result[0][it] } ?: -1)
        } ?: Pair(floatArrayOf(), -1)
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
            _apiResponse.emit(ApiStatus.Loading)
            try {
                repo.downloadDigitsModel().let {
                    if (it == null) {
                        _apiResponse.value =
                            ApiStatus.Error(java.lang.Exception(" Received Null value"))
                    } else {
                        interpreter = Interpreter(loadModelFromApp(it)!!, 1)
                        _apiResponse.emit(ApiStatus.Success)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _apiResponse.emit(ApiStatus.Error(e))
                Log.e(TAG, " Not downloaded : ${e.message.toString()}")
            }
        }

    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputImageWidth * inputImageHeight)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in pixels) {
            val r = (pixelValue shr 16 and 0xFF)
            val g = (pixelValue shr 8 and 0xFF)
            val b = (pixelValue and 0xFF)
            // Convert RGB to grayscale and normalize pixel value to [0..1]
            val normalizedPixelValue = (r + g + b) / 3.0f / 255.0f
            byteBuffer.putFloat(normalizedPixelValue)
        }

        return byteBuffer
    }

    sealed class ApiStatus {
        object Loading : ApiStatus()
        object Success : ApiStatus()
        object None : ApiStatus()
        data class Error(var exception: Exception) : ApiStatus()
    }

}