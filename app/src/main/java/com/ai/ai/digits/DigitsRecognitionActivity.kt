package com.ai.ai.digits

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.ai.ai.R
import com.ai.ai.databinding.ActivityDigitsRecognitionBinding
import com.ai.ai.digits.DigitRecognitionViewModel.ApiStatus.*
import com.github.mikephil.charting.components.XAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.github.mikephil.charting.data.BarData

import com.github.mikephil.charting.data.BarDataSet

import com.github.mikephil.charting.data.BarEntry

import java.util.ArrayList
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter


@AndroidEntryPoint
class DigitsRecognitionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDigitsRecognitionBinding
    private val viewModel: DigitRecognitionViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_digits_recognition)
        binding.state = None
        setClickListeners()
        setObserver()
    }

    private fun setObserver() {
        lifecycleScope.launchWhenStarted {
            viewModel.apiResponse.collect {
                binding.state = it
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.probabilities.collect {
                Log.e("ssss", "setObserver: " + it.joinToString { "," })
            }
        }
    }

    private fun setClickListeners() {
        binding.apply {
            btnClassify.setOnClickListener {
                var bitmap = paintView.getBitmap()
                ivPreview.setImageBitmap(bitmap)
                var (probabilities, digit) = viewModel.doInference(bitmap!!)
                tvResult.text = digit.toString()
                showBarChart(probabilities)
                paintView.clear()
            }
            btnClear.setOnClickListener {
                paintView.clear()
            }
            btnRetry.setOnClickListener {
                viewModel.downloadModel()
            }
            btnDownload.setOnClickListener {
                viewModel.downloadModel()
            }
        }
    }

    private fun showBarChart(probabilities: FloatArray) {
        val title = "Probabilities"
        val barDataSet = BarDataSet(probabilities.mapIndexed { index, value ->
            BarEntry(
                index.toFloat(),
                value
            )
        }, title)
        binding.chart.setScaleEnabled(false)
        binding.chart.data = BarData(barDataSet)
        binding.chart.xAxis?.let {
            it.setAvoidFirstLastClipping(false)
            it.position = XAxis.XAxisPosition.BOTTOM
            it.setDrawGridLines(false)
        }
        binding.chart.description.isEnabled = false
        binding.chart.invalidate()
    }

}