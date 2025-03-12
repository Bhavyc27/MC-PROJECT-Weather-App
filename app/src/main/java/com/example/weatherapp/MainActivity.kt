package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.weatherapp.ui.theme.WeatherAppTheme
import androidx.compose.ui.unit.dp

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


@Composable
fun searchBar() {
    var text by remember { mutableStateOf("") }

    val context= LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(text="Wheather App", modifier = Modifier
            .fillMaxWidth()
            .padding(start=8.dp)
        )
        TextField(
            value = text,
            onValueChange = { text=it},
            placeholder = { Text("Enter city: ") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        Button(onClick = {fetchData(context)},
            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
                .width(120.dp)) {
            Text("Fetch Data")
        }
    }


}
fun fetchData(context:Context){
    //to be complete
    Toast.makeText(context,"Function is clicked",Toast.LENGTH_LONG).show()
}

