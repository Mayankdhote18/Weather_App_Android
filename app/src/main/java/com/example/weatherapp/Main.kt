package com.example.weatherapp

data class Main(
    val feels_like: Double,
    val grnd_level: Int?,   // made optional
    val humidity: Int,
    val pressure: Int,
    val sea_level: Int?,    // made optional
    val temp: Double,
    val temp_max: Double,
    val temp_min: Double
)
