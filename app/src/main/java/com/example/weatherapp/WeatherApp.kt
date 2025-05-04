package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weatherapp.API.WeatherViewModel
import com.google.android.gms.location.LocationServices

@SuppressLint("SuspiciousIndentation")
@Composable
fun WeatherApp(weatherViewModel: WeatherViewModel, darkMode: Boolean, toggleDarkButton:()->Unit, tts: TextToSpeech) {
    val navController = rememberNavController()


    NavHost(navController, startDestination = "search") {
        composable("search") {
            searchBar(weatherViewModel, navController,toggleDarkButton,darkMode,tts)  // Pass navController
        }

//        }

    }
}




@SuppressLint("NewApi")
fun saveSearchCity(context: Context, city_name: String) {
    val savesearchpref = context.getSharedPreferences("Search_City_History", Context.MODE_PRIVATE)
    val currentHistory = savesearchpref.getString("cities_history", "") ?: ""

    // Split existing history and remove duplicates
    val historyList = currentHistory.split("|").filter { it.isNotEmpty() }.toMutableList()

    // Remove if already exists to avoid duplicates
    historyList.remove(city_name)

    // Add to beginning (most recent first)
    historyList.add(0, city_name)

    // Limit to last 10 searches


    // Join with delimiter and save
    savesearchpref.edit().putString("cities_history", historyList.joinToString("|")).apply()
}

fun getSearchList(context: Context): List<String> {
    val savesearchpref = context.getSharedPreferences("Search_City_History", Context.MODE_PRIVATE)
    val historyString = savesearchpref.getString("cities_history", "") ?: ""
    return historyString.split("|").filter { it.isNotEmpty() }
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




fun fetchData(context: Context, city:String){
    if(city.length>=1)
    {
        saveSearchCity(context, city_name = city)

    }
}

