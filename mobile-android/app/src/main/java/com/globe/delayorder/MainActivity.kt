package com.globe.delayorder

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.globe.delayorder.api.ApiService
import com.globe.delayorder.model.ApproverActionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private val BASE_URL = "http://10.0.2.2:5001/"
    private val approverEmail = "cpjuezan@globe.com.ph"

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
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
                        Toast.makeText(this@MainActivity, "Loaded ${pendingOrders?.size ?: 0} pending orders", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Connection Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
