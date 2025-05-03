package com.example.weatherapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.weatherapp.API.NetworkResponse
import com.example.weatherapp.API.WeatherModel
import com.example.weatherapp.API.WeatherViewModel
import kotlinx.coroutines.delay

val PREFS_NAME = "weather_app_prefs"
val KEY_DARK_MODE = "dark_mode"

fun saveDarkMode(context: Context, isDarkMode: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        putBoolean(KEY_DARK_MODE, isDarkMode)
    }
}

fun getDarkMode(context: Context): Boolean {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_DARK_MODE, false)
}


@Composable
fun searchBar(viewModel: WeatherViewModel, navController: NavController, toggleDarkButton:()->Unit, darkMode: Boolean, tts: TextToSpeech) {
    val weatherResult=viewModel.weatherResult.observeAsState()

    var text by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current




    var showHistory by remember { mutableStateOf(false) }

    var searchHistory by remember { mutableStateOf(emptyList<String>()) }

    val context= LocalContext.current

    var autoRefreshEnabled by remember { mutableStateOf(false) }

    // Function to perform search
    fun performSearch() {
        if (text.isNotEmpty()) {
            keyboardController?.hide()
            fetchData(context, text)
            searchHistory = getSearchList(context)
            viewModel.getData(text)
        }
    }

    // Auto-refresh effect
    LaunchedEffect(text, autoRefreshEnabled) {
        if (text.isNotEmpty() && autoRefreshEnabled) {
            while (true) {
                performSearch()
                delay(60_000)
            }
        }
    }




    LaunchedEffect(Unit) {
        searchHistory = getSearchList(context)
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.get(0)
            if (!spokenText.isNullOrEmpty()) {
                text = spokenText
                keyboardController?.hide()
                fetchData(context, spokenText)
                searchHistory = getSearchList(context)
                viewModel.getData(spokenText)
            }
        }
    }



    val locationPermissionState = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchCurrentLocation(context) { cityName ->
                text = cityName
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }


    Column(modifier = Modifier

        .fillMaxSize()
        .padding(WindowInsets.systemBars.asPaddingValues())
        .background(MaterialTheme.colorScheme.background)// Add padding for system bars


    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically, // Aligns items vertically
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Weather App",
                fontSize = 20.sp,
                color= MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start=75.dp),
            )


            IconButton(onClick = {
                toggleDarkButton()
            },
                modifier = Modifier.padding(start=70.dp)) {
                Image(
                    painter = painterResource(if (darkMode) R.drawable.dark_mode else R.drawable.light_mode),
                    contentDescription = if (darkMode) "Dark Mode" else "Light Mode",
                    modifier = Modifier.size(if (darkMode) 40.dp else 24.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }


        Spacer(modifier = Modifier.height(10.dp))
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "MenuICON",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { showHistory = !showHistory }
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text("Enter City", fontSize = 18.sp)
                    },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.inversePrimary,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Gray,
                        errorTextColor = Color.Red,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.LightGray,
                        errorContainerColor = Color.Transparent,
                        cursorColor = Color.Black,
                        errorCursorColor = Color.Red,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Red,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray,
                        disabledPlaceholderColor = Color.LightGray,
                        errorPlaceholderColor = Color.Red
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                Icon(
                    painter = painterResource(R.drawable.mic_24px),
                    contentDescription = "Voice Search",
                    tint = Color.Gray,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a city name")
                            }
                            try {
                                voiceLauncher.launch(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Voice search not supported on this device", Toast.LENGTH_SHORT).show()
                            }
                        }
                )

                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color.Gray,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED
                            ) {
                                fetchCurrentLocation(context) { detectedCity ->
                                    text = detectedCity
                                }
                            } else {
                                locationPermissionState.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                )

                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search ICON",
                    tint = Color.Gray,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable(onClick = {
                            performSearch()

                            if(text.isNotEmpty())
                            {
                                autoRefreshEnabled=true
                            }
                        })

                )

            }
        }
        if (showHistory && searchHistory.size>=1 ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                searchHistory.forEach { city ->
                    Text(
                        text = city,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .clickable {
                                text = city
                                showHistory = false
                            }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        when(val res=weatherResult.value)
        {
            is NetworkResponse.Error -> {
                Text(
                    text = res.message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
            NetworkResponse.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

            }
            is NetworkResponse.Success -> {

                weatherDetail(res.data,darkMode,tts)
            }
            null -> {}
        }
    }



}




@Composable
fun weatherDetail(data: WeatherModel, darkMode: Boolean, tts: TextToSpeech) {
    val context = LocalContext.current



    val compassMap = mapOf(
        "N" to "North", "NNE" to "North-Northeast", "NE" to "Northeast", "ENE" to "East-Northeast",
        "E" to "East", "ESE" to "East-Southeast", "SE" to "Southeast", "SSE" to "South-Southeast",
        "S" to "South", "SSW" to "South-Southwest", "SW" to "Southwest", "WSW" to "West-Southwest",
        "W" to "West", "WNW" to "West-Northwest", "NW" to "Northwest", "NNW" to "North-Northwest"
    )

    val windDirectionDescription = compassMap[data.current.wind_dir] ?: data.current.wind_dir

    val speechText = """
                    Weather in ${data.location.name}, ${data.location.country}.
                    Current temperature is ${data.current.temp_c}°C.
                    Condition is ${data.current.condition.text}.
                    Humidity is ${data.current.humidity} percent.
                    Wind speed is ${data.current.wind_kph} kilometers per hour.
                    UV Index is ${data.current.uv}.
                    Precipitation is ${data.current.precip_mm} mm.
                    Is it daytime? ${if (data.current.is_day == 1) "Yes" else "No"}.
                    Wind direction is $windDirectionDescription, with a wind angle of ${data.current.wind_degree} degrees.
                    Pressure is ${data.current.pressure_mb} millibars.
                    Gust speed is ${data.current.gust_kph} kilometers per hour.
                    Cloud cover is ${data.current.cloud} percent.
                    Visibility is ${data.current.vis_km} kilometers.
                    Last updated at ${data.current.last_updated}.
                    Local Time is ${data.location.localtime.split(" ")[1]} and Local Date is ${data.location.localtime.split(" ")[0]}.
                """.trimIndent()




    // Speak the text
    LaunchedEffect(data) {

        tts.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, null)
    }





    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Location Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            val iconPainter = if (!darkMode) {
                painterResource(id = R.drawable.light_mode_location)
            } else {
                painterResource(id = R.drawable.dark_mode_location)
            }
            Icon(
                painter = iconPainter,
                contentDescription = "Location icon",
                tint = if (!darkMode) Color.Red else Color.White,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = data.location.name,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.inversePrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = data.location.country,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.inversePrimary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Temperature
        Text(
            text = " ${data.current.temp_c} ° c",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.inversePrimary
        )

        // Weather Icon
        AsyncImage(
            modifier = Modifier.size(160.dp),
            model = "https:${data.current.condition.icon}".replace("64x64", "128x128"),
            contentDescription = "Condition icon"
        )

        // Condition Text
        Text(
            text = data.current.condition.text,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.inversePrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // First Card (Basic Info)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeatherKeyVal("Humidity", data.current.humidity)
                    WeatherKeyVal("Wind Speed", data.current.wind_kph + " km/h")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeatherKeyVal("UV", data.current.uv)
                    WeatherKeyVal("Precipitation", data.current.precip_mm + " mm")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeatherKeyVal("Local Time", data.location.localtime.split(" ")[1])
                    WeatherKeyVal("Local Date", data.location.localtime.split(" ")[0])
                }
            }
        }



        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // Detailed Weather Card
        WeatherDetailsCard(
            isDay = data.current.is_day,
            windDegree = data.current.wind_degree,
            windDir = data.current.wind_dir,
            pressureMb = data.current.pressure_mb,
            gustKph = data.current.gust_kph,
            humidity = data.current.humidity,
            cloud = data.current.cloud,
            visibilityKm = data.current.vis_km,
            lastUpdated = data.current.last_updated
        )
    }
}

@Composable
fun WeatherKeyVal(key : String, value : String) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = key, fontWeight = FontWeight.SemiBold, color = Color.Gray)
    }
}

@Composable
fun WeatherDetailsCard(
    isDay: Int,
    windDegree: String,
    windDir: String,
    pressureMb: String,
    gustKph: String,
    humidity: String,
    cloud: String,
    visibilityKm: String,
    lastUpdated: String
) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Detailed Weather Information",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )



            // First row of details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem("Day/Night", if (isDay == 1) "Day" else "Night")
                DetailItem("Wind Degree", "$windDegree°")
                DetailItem("Wind Direction", windDir)
            }

            // Second row of details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem("Pressure", "$pressureMb mb")
                DetailItem("Gust Speed", "$gustKph kph")
                DetailItem("Humidity", "$humidity%")
            }

            // Third row of details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem("Cloud Cover", "$cloud%")
                DetailItem("Visibility", "$visibilityKm km")
                DetailItem("Last Updated", lastUpdated)
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}