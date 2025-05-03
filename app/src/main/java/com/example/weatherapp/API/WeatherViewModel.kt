package com.example.weatherapp.API

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    private val weatherapi = RetrofitInstance.weatherapi

    private val _weatherResult = MutableLiveData<NetworkResponse<WeatherModel>>()
    val weatherResult: LiveData<NetworkResponse<WeatherModel>> = _weatherResult

    fun getData(city: String) {

          viewModelScope.launch {

              _weatherResult.value = NetworkResponse.Loading
              try {

                  val res = weatherapi.getWeather(Constant.apiKey, city)
                  if (res.isSuccessful) {
                      res.body()?.let {
                          _weatherResult.value = NetworkResponse.Success(it)
                      }
                  } else {

                      _weatherResult.value = NetworkResponse.Error("This city is not available.")
                  }
              } catch (e: Exception) {
                  _weatherResult.value = NetworkResponse.Error("No Internet connection.")
              }
          }
    }
}
