package com.example.weatherapp



import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.weatherapp.ui.theme.WeatherAppTheme
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModelProvider

import com.example.weatherapp.API.WeatherViewModel
import java.util.Locale






class MainActivity : ComponentActivity() {

    private lateinit var tts: TextToSpeech



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initTextToSpeech()

        val weatherViewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
        enableEdgeToEdge()

        setContent {

            var darkMode by remember {
                mutableStateOf(getDarkMode(this@MainActivity))
            }

            val toggleDarkButton = {
                darkMode = !darkMode
                saveDarkMode(this@MainActivity, darkMode)
            }




            WeatherAppTheme(darkMode, darkMode) {
                WeatherApp(
                    weatherViewModel = weatherViewModel,
                    darkMode = darkMode,
                    toggleDarkButton = toggleDarkButton,
                    tts = tts
                )
            }
        }
    }

    private fun initTextToSpeech() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!::tts.isInitialized) {
            initTextToSpeech()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::tts.isInitialized) {
            tts.stop()
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}






