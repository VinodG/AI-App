package com.ai.ai.digits

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ai.ai.R
import com.ai.ai.databinding.ActivityDigitsRecognitionBinding
import com.ai.ai.digits.DigitRecognitionViewModel.*
import com.ai.ai.digits.DigitRecognitionViewModel.ApiStatus.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.Error

@AndroidEntryPoint
class DigitsRecognitionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDigitsRecognitionBinding
    private val viewModel: DigitRecognitionViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_digits_recognition)
        setClickListeners()
        setObserver()
    }

    private fun setObserver() {
        lifecycleScope.launchWhenStarted {
            viewModel.apiResponse.collect {
                binding.state = it
            }

        }
    }

    private fun setClickListeners() {
        binding.apply {
            btnClassify.setOnClickListener {
                var bitmap = paintView.getBitmap()
                ivPreview.setImageBitmap(bitmap)
                tvResult.text = viewModel.doInference(bitmap!!).toString()
                paintView.clear()
            }
            btnClear.setOnClickListener {
                paintView.clear()
            }
            btnRetry.setOnClickListener {
                viewModel.downloadModel()
            }
        }
    }

}