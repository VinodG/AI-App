package com.ai.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ai.ai.ui.theme.AIAppTheme
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint
import org.tensorflow.lite.Interpreter

@AndroidEntryPoint
class LinearMLActivity : ComponentActivity() {
    val vm: LinearMLViewAModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIAppTheme {
                var inputValue = remember { mutableStateOf("10") }
                var result = remember { mutableStateOf("0") }

                Surface(color = MaterialTheme.colors.background) {
                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextField(
                            value = inputValue.value,
                            onValueChange = {
                                inputValue.value = it
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Result : ${result.value}",
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        Button(onClick = {
                            result.value = vm.doInference(
                                try {
                                    inputValue.value.toFloat()
                                } catch (e: Exception) {
                                    0.0f
                                }
                            ).toString()
                            inputValue.value = ""
                        }) {
                            Text(
                                text = "Evaluate",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (vm.isLoading.value)
                            CircularProgressIndicator()
                        else
                            Button(onClick = { vm.downloadModel() }) {
                                Text(
                                    text = "Download Model",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { vm.loadFromAsset() }) {
                            Text(
                                text = "Load From Asset",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

}


