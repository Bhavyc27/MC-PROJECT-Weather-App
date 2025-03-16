package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.weatherapp.ui.theme.WeatherAppTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {

                searchBar()

            }
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




@Composable
fun searchBar() {
    var text by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    var showHistory by remember { mutableStateOf(false) }
    
    var searchHistory by remember { mutableStateOf(emptyList<String>()) }

    val context= LocalContext.current

    LaunchedEffect(Unit) {
        searchHistory = getSearchList(context)
    }



    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = "Weather App", fontSize = 20.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
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
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search ICON",
                    tint = Color.Gray,
                    modifier = Modifier
                        .padding(8.dp)
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
            if (text.length>=1) {
                keyboardController?.hide() // Hide the keyboard
                fetchData(context, text)
                searchHistory = getSearchList(context)
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

fun getResultFromApi(){
    //to be complete
}

