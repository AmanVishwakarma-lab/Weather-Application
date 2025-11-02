package com.example.Weather_Forecast

//0bc915fcb3fe6f4fe7f32e8d3b379398     weather api key


import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.Weather_Forecast.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects
import android.widget.SearchView
import com.google.android.material.internal.ViewUtils.hideKeyboard
import java.util.TimeZone

class MainActivity : AppCompatActivity() {
    private  val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
//        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fetchWeatherData("Allahabad")
        searchCity()
    }

    private fun searchCity() {
        val searchView=binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    fetchWeatherData(query)
                    hideKeyboard()
                    searchView.clearFocus()

                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun fetchWeatherData(city : String) {
        val retrofit= Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)
        binding.city.text=city
        val response=retrofit.getWeatherData(city,"0bc915fcb3fe6f4fe7f32e8d3b379398","metric")
        response.enqueue(object : Callback<WeatherApp>{
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody=response.body()
                if(response.isSuccessful && responseBody != null){
                    Toast.makeText(this@MainActivity, "Result for City", Toast.LENGTH_SHORT).show()
                    binding.degree.text=responseBody.main.temp.toString()+"°C"
                    binding.humidity.text=responseBody.main.humidity.toString()+" %"
                    binding.windSpeed.text=responseBody.wind.speed.toString()+" m/s"
                    binding.seaLevel.text=responseBody.main.sea_level.toString()+" hPa"
                    binding.maxTemp.text="Max : "+responseBody.main.temp_max.toString()+"°C"
                    binding.minTemp.text="Min : "+responseBody.main.temp_min.toString()+"°C"
                    binding.sunset.text=time(responseBody.sys.sunset.toLong())
                    binding.sunrise.text=time(responseBody.sys.sunrise.toLong())
                    binding.day.text=dayName(System.currentTimeMillis())
                    binding.date.text=dateName()
                    val condition=responseBody.weather.firstOrNull()?.main?:"Unknown"
                    binding.condition.text=condition
                    binding.mood.text=condition

                    updatePicAccToCondition(condition)
                }else {
                    Toast.makeText(this@MainActivity, "City not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Log.e("WeatherAPI", "API call failed: ${t.localizedMessage}")
                Toast.makeText(this@MainActivity, "Failed to fetch weather data", Toast.LENGTH_SHORT).show()

            }

        })

    }

    private fun updatePicAccToCondition(condition: String) {
        when (condition) {
            "Sunny","Clear","Clear Sky" -> {
                binding.main.setBackgroundResource(R.drawable.sunny_background)
                binding.lottiAnimationView.setAnimation(R.raw.sun)
            }
            "Party Clouds","Clouds","Overcast","Mist","Foggy" -> {
                binding.main.setBackgroundResource(R.drawable.colud_background)
                binding.lottiAnimationView.setAnimation(R.raw.cloud)
            }
            "Light Rain","Drizzle","Moderate Rain","Showers","Heavy Rain" -> {
                binding.main.setBackgroundResource(R.drawable.rain_background)
                binding.lottiAnimationView.setAnimation(R.raw.rain)
            }
            "Light Snow","Moderate Snow","Heavy Snow","Blizzard" -> {
                binding.main.setBackgroundResource(R.drawable.snow_background)
                binding.lottiAnimationView.setAnimation(R.raw.snow)
            }
            else->{
                binding.main.setBackgroundResource(R.drawable.sunny_background)
                binding.lottiAnimationView.setAnimation(R.raw.sun)
            }
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        val view = this.currentFocus
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun time(timestamp: Long): String {
        val date = Date(timestamp * 1000) // Convert seconds to milliseconds
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault()) // 12-hour format with AM/PM
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
    fun dayName(timeStamp:Long):String{
        val sdf=SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }
    fun dateName():String{
        val sdf=SimpleDateFormat("dd MMMM YYYY",Locale.getDefault())
        return sdf.format(Date())
    }
}