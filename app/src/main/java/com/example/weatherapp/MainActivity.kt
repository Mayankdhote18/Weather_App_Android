package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    
    private val API_KEY = "8e0b40ecf7574d0c0141b82b336f72a7"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fetchWeatherData("Pune")
        SearchCity()
    }

    private fun SearchCity() {
     val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
              if (query != null)
              {
                  fetchWeatherData(query)
              }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun fetchWeatherData(cityName:String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            // FIX (from previous issue): Added trailing slash to BaseUrl
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        // Used the defined constant
        val responce = retrofit.getWeatherData(cityName, API_KEY, "metric")

        responce.enqueue(object : Callback<WeatherApp> {

            override fun onResponse(p0: Call<WeatherApp>, p1: Response<WeatherApp>) {
                val responseBody = p1.body()

                if (p1.isSuccessful && responseBody != null) {
                    val temperature = responseBody.main.temp.toString()
                    val humidity = responseBody.main.humidity
                    val windspeed = responseBody.wind.speed
                    val sunRise = responseBody.sys.sunrise.toLong()
                    val sunSet = responseBody.sys.sunset.toLong()
                    val seaLevel= responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main?: "unknown"
                    val maxTemp= responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min

                    binding.temp.text = "$temperature °C"
                    binding.weather.text = condition
                    binding.maxtemp.text = "Max Temp : $maxTemp °C"
                    binding.mintemp.text = "Min Temp : $minTemp °C"
                    binding.humidity.text = "$humidity %"
                    binding.windspeed.text = "$windspeed m/s"
                    binding.sunset.text = "${time(sunSet)}"
                    binding.sunrise.text = "${time(sunRise)}"
                    binding.sea.text = "$seaLevel hPa"
                    binding.condition.text =condition
                    binding.day.text =dayName(System.currentTimeMillis())
                        binding.date.text = date()
                        binding.cityname.text ="$cityName"


                  changeImagesAccordingToWeatherCondition(condition)


                   // Log.d("WEATHER_APP", "Success: Temperature set to $temperature")

                } else {
                    // CRITICAL FIX: Log the HTTP error code if the API call fails (e.g., 401, 404)
                    Log.e("WEATHER_APP", "Unsuccessful API Response. Code: ${p1.code()}, Message: ${p1.message()}")
                    binding.temp.text = "Error: ${p1.code()}"
                }
            }

            override fun onFailure(p0: Call<WeatherApp>, p1: Throwable) {
                // CRITICAL FIX: Handle network connection errors (like no internet or timeout)
                Log.e("WEATHER_APP", "Network/Conversion Failure: ${p1.message}", p1)
                binding.temp.text = "Network Error"
            }
        })


    }

    private fun changeImagesAccordingToWeatherCondition(conditions: String) {
        when(conditions){
            "Clear Sky", "Sunny", "Clear" ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)

            }

            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy" ->{
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)

            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" ->{
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)

            }

            "Light Snow", "Moderate Snow", "Heavy Snow", " Blizzard" ->{
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)

            }
            else ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun date():String {
        val sdf = SimpleDateFormat("dd MMMM YYYY", Locale.getDefault())
        return sdf.format((Date()))
    }


    private fun time(timestamp: Long):String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timestamp*1000)))
    }


    fun dayName(timestamp: Long): String{
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }
}
