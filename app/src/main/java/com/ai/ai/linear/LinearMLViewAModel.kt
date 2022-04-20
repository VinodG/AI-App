package com.ai.ai.linear

import android.content.Context
import android.util.Log
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
    private val TAG = "LinearMLViewAModel"
    private var interpreter: Interpreter? = null

    var isLoading = mutableStateOf(false)

    fun loadFromAsset() {
    }

    fun doInference(predict_x: Float): Float {
        var floatArray = floatArrayOf(1.0f)
        floatArray[0] = predict_x
        var outarray = Array(1) { floatArray }
        interpreter?.run(floatArray, outarray)
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

    fun downloadModel(verson: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true
            try {
                try {
                    repo.download(version = verson)?.let {
                        interpreter = Interpreter(loadModelFromApp(it)!!, 1)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, " Not downloaded : ${e.message.toString()}")
                }
                /*FirebaseApp.initializeApp(context)
                val conditions = CustomModelDownloadConditions.Builder()
                    .requireWifi()
                    .build()
                FirebaseModelDownloader.getInstance()
                    .getModel("linear-2", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                    .addOnCompleteListener {
                        Log.e(TAG, "downloadModel: onSuccess ${it.isSuccessful}")
                        if (it.isSuccessful) {
                            it.result.file?.let {
                                Log.e(TAG, "downloadModel: received file ${it.absoluteFile}")
                                interpreter = Interpreter(loadModelFromApp(it.absolutePath)!!, 1)
                            }
                        }
                        // Download complete. Depending on your app, you could enable the ML
                        // feature, or switch from the local model to the remote model, etc.
                    }.addOnCanceledListener {
                        Log.e(TAG, "downloadModel:  onCancel ")
                    }.addOnFailureListener {
                        Log.e(TAG, "downloadModel: onError ${it.message.toString()}")
                    }*/
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, " Not downloaded : ${e.message.toString()}")
            }
            isLoading.value = false
        }

    }

}