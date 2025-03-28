package com.example.weatherapp.API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val baseURL="https://api.weatherapi.com"

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(baseURL).addConverterFactory(GsonConverterFactory.create()).build()

    }

    val weatherapi:WeatherAPI= getInstance().create(WeatherAPI::class.java)
}