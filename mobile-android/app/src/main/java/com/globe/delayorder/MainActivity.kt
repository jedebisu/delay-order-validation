package com.globe.delayorder

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.globe.delayorder.api.ApiService
import com.globe.delayorder.model.ApproverActionRequest
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    // Live Vercel API URL (Ensuring explicit ending slash)
    private val BASE_URL = "https://delay-order-validation-b01bqe7dc-jedebisus-projects.vercel.app/"
    private val approverEmail = "cpjuezan@globe.com.ph"

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadPendingOrders()
    }

    private fun loadPendingOrders() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getRequests()
                if (response.isSuccessful) {
                    val pendingOrders = response.body()?.filter { it.status == "PENDING" }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Connected! ${pendingOrders?.size ?: 0} pending orders found.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "HTTP Error: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "API Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
