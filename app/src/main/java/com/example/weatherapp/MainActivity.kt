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
import androidx.compose.ui.text.intl.Locale
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                WeatherApp()

            }
        }
    }
}

@Composable
fun WeatherApp() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "search") {
        composable("search") {
            searchBar(navController)  // Pass navController
        }
        composable("weatherDetail/{city}") { backStackEntry ->
            val city = backStackEntry.arguments?.getString("city") ?: "Unknown"
            WeatherDetailScreen(city) // Calls function from ShowWeather.kt
        }
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

fun funcDarkMode(darkMode:Int)
{
    if(darkMode==1)    //for darkmode
    {

    }
    else{              //else

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





@Preview(showBackground = true, showSystemUi = true)
@Composable
fun searchBar(navController:NavController) {
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
                                navController.navigate("weatherDetail/$text") // Navigate to details
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
        Button(onClick = {
            if (text.isNotEmpty()) {
                keyboardController?.hide()
                fetchData(context, text)
                searchHistory = getSearchList(context)
                navController.navigate("weatherDetail/$text") // Navigate to details
            }
            },
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.CenterHorizontally)
                .width(170.dp)) {
            Text("Fetch Data")
        }


    }

}


fun fetchData(context:Context,city:String){
    if(city.length>=1)
    {
        saveSearchCity(context, city_name = city)
    }
}



