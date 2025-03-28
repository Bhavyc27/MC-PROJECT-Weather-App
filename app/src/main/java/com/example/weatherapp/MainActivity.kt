package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Bundle
import android.text.Layout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.weatherapp.ui.theme.WeatherAppTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil.compose.AsyncImage
import com.example.weatherapp.API.NetworkResponse
import com.example.weatherapp.API.WeatherModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val weatherViewModel=ViewModelProvider(this)[WeatherViewModel::class.java]
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                WeatherApp(weatherViewModel)

            }
        }
    }
}


@Composable
fun WeatherApp(weatherViewModel: WeatherViewModel) {
    val navController = rememberNavController()


    NavHost(navController, startDestination = "search") {
        composable("search") {
            searchBar(weatherViewModel,navController)  // Pass navController
        }
//        composable("weatherDetail/{city}") { backStackEntry ->
//            val city = backStackEntry.arguments?.getString("city") ?: "Unknown"
//            WeatherDetailScreen(city) // Calls function from ShowWeather.kt
//        }
    }
}



fun saveSearchCity(context:Context,city_name:String){
    var savesearchpref=context.getSharedPreferences("Search_City_History",Context.MODE_PRIVATE)
    val history=savesearchpref.getStringSet("cities", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    history.add(city_name)
    savesearchpref.edit().putStringSet("cities", history).apply()

}
fun getSearchList(context:Context):List<String>{
    val savesearchpref=context.getSharedPreferences("Search_City_History",Context.MODE_PRIVATE)
    return savesearchpref.getStringSet("cities", mutableSetOf())?.toList()?: emptyList()

}

fun funcDarkMode(darkMode: Int) {
    if (darkMode == 1) {
        // Enable Dark Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    } else {
        // Enable Light Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}

fun getCityNameFromCoordinates(context: Context, latitude: Double, longitude: Double, callback: (String) -> Unit) {
    val geocoder = Geocoder(context)
    try {
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        val cityName = addresses?.firstOrNull()?.locality ?: "Unknown"
        callback(cityName)
    } catch (e: Exception) {
        callback("Unknown")
    }
}

fun fetchCurrentLocation(context: Context, onLocationReceived: (String) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                getCityNameFromCoordinates(context, location.latitude, location.longitude) {
                    cityName->
                    if(cityName!=="unknown") {
                        onLocationReceived(cityName)
                    }
                    else {
                        Toast.makeText(context,"Location is not found", Toast.LENGTH_LONG).show()
                    }

                }
            } else {
                Toast.makeText(context, "Unable to fetch location", Toast.LENGTH_SHORT).show()
            }
        }
    } else {
        Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
    }
}




fun fetchData(context:Context, city:String){
    if(city.length>=1)
    {
        saveSearchCity(context, city_name = city)

    }
}

@Composable
fun searchBar(viewModel: WeatherViewModel, navController:NavController) {
    val weatherResult=viewModel.weatherResult.observeAsState()

    var text by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    var showHistory by remember { mutableStateOf(false) }

    var searchHistory by remember { mutableStateOf(emptyList<String>()) }

    val context= LocalContext.current



    var darkMode by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        searchHistory = getSearchList(context)
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



    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(40.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically, // Aligns items vertically
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Weather App",
                fontSize = 20.sp,
                modifier = Modifier.padding(start=75.dp),
)

            IconButton(onClick = {
                darkMode = 1 - darkMode
                funcDarkMode(darkMode)
            },
                modifier = Modifier.padding(start=70.dp)) {
                Image(
                    painter = painterResource(if (darkMode == 1) R.drawable.dark_mode else R.drawable.light_mode),
                    contentDescription = if (darkMode == 1) "Dark Mode" else "Light Mode",
                    modifier = Modifier.size(24.dp), // Ensures proper icon size
                    contentScale = ContentScale.Fit
                )
            }
        }




        Spacer(modifier = Modifier.height(10.dp))
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFDCD0FF), shape = RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "MenuICON",
                    tint = Color.Gray,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { showHistory = !showHistory }
                )

                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text("Enter City", color = Color.Gray, fontSize = 18.sp)
                    },
                    textStyle = TextStyle(
                        color = Color.Black,
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
                            if (text.isNotEmpty()) {
                                keyboardController?.hide()
                                fetchData(context, text)
                                searchHistory = getSearchList(context)
                                viewModel.getData(text)

//                                navController.navigate("weatherDetail/$text") // Navigate to details

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
                CircularProgressIndicator()
            }
            is NetworkResponse.Success -> {
                weatherDetail(res.data)
            }
            null -> {}
        }

    }

}
@Composable
fun weatherDetail(data : WeatherModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location icon",
                modifier = Modifier.size(40.dp)
            )
            Text(text = data.location.name, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = data.location.country, fontSize = 15.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = " ${data.current.temp_c} ° c",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        AsyncImage(
            modifier = Modifier.size(160.dp),
            model = "https:${data.current.condition.icon}".replace("64x64","128x128"),
            contentDescription = "Condition icon"
        )
        Text(
            text = data.current.condition.text,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeatherKeyVal("Humidity",data.current.humidity)
                    WeatherKeyVal("Wind Speed",data.current.wind_kph+" km/h")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeatherKeyVal("UV",data.current.uv)
                    WeatherKeyVal("Participation",data.current.precip_mm+" mm")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeatherKeyVal("Local Time",data.location.localtime.split(" ")[1])
                    WeatherKeyVal("Local Date",data.location.localtime.split(" ")[0])
                }
            }
        }



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






