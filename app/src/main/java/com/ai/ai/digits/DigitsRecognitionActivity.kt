package com.ai.ai.digits

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ai.ai.R
import com.ai.ai.databinding.ActivityDigitsRecognitionBinding

class DigitsRecognitionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDigitsRecognitionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_digits_recognition)
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.btnClassify.setOnClickListener {
            var bitmap = binding.paintView.getBitmap()
            bitmap = Bitmap.createScaledBitmap(
                bitmap!!,
                28,
                28,
                false
            )
            binding.ivPreview.setImageBitmap(bitmap)
        }
        binding.btnClear.setOnClickListener {
            binding.paintView.clear()
        }

    }

    fun setInput() {
//
//// Creates inputs for reference.
//        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 28, 28, 1), DataType.FLOAT32)
//        inputFeature0.loadBuffer(byteBuffer)
//
//// Runs model inference and gets result.
//        val outputs = model.process(inputFeature0)
//        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//
//// Releases model resources if no longer used.
//        model.close()
    }
}