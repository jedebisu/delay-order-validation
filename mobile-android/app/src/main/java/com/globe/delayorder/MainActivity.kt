package com.globe.delayorder

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.globe.delayorder.api.ApiService
import com.globe.delayorder.model.ApproverActionRequest
import com.globe.delayorder.model.WorkOrderRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    private val BASE_URL = "https://delay-order-validation.vercel.app/"
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

    private val allRequests = mutableListOf<WorkOrderRequest>()
    private var showPendingOnly = true

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RequestAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyText: TextView
    private lateinit var btnPending: MaterialButton
    private lateinit var btnAll: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        emptyText = findViewById(R.id.emptyText)
        btnPending = findViewById(R.id.btnPending)
        btnAll = findViewById(R.id.btnAll)

        adapter = RequestAdapter { request -> showActionDialog(request) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<android.widget.Button>(R.id.btnRefresh).setOnClickListener { loadRequests() }

        swipeRefresh.setOnRefreshListener { loadRequests() }

        findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup).addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            showPendingOnly = checkedId == R.id.btnPending
            render()
        }

        loadRequests()
    }

    private fun loadRequests() {
        swipeRefresh.isRefreshing = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getRequests()
                withContext(Dispatchers.Main) {
                    swipeRefresh.isRefreshing = false
                    if (response.isSuccessful && response.body() != null) {
                        allRequests.clear()
                        allRequests.addAll(response.body()!!)
                        render()
                    } else {
                        Toast.makeText(this@MainActivity, "HTTP Error: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    swipeRefresh.isRefreshing = false
                    Toast.makeText(this@MainActivity, "API Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun render() {
        val sorted = allRequests.sortedByDescending { parseMillis(it.timestamp) }
        val shown = if (showPendingOnly) sorted.filter { it.status == "PENDING" } else sorted

        btnPending.text = "Pending (${sorted.count { it.status == "PENDING" }})"
        btnAll.text = "All (${sorted.size})"

        adapter.submit(shown)
        emptyText.visibility = if (shown.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showActionDialog(request: WorkOrderRequest) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_action)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val title = dialog.findViewById<TextView>(R.id.dlgTitle)
        title.text = "${request.workordernumber}  •  ${request.partner}"
        val info = dialog.findViewById<TextView>(R.id.dlgInfo)
        info.text = buildString {
            append("Channel: ${request.channel ?: "—"}\n")
            append("Delay: ${request.delay ?: "—"}\n")
            append("3-way: ${request.person3wayed ?: "—"}\n")
            if (!request.threewaynotes.isNullOrBlank()) {
                append("\n${request.threewaynotes}")
            }
        }

        val delayTimeText = dialog.findViewById<TextView>(R.id.dlgDelayTime)
        var selectedMillis: Long? = null

        delayTimeText.setOnClickListener {
            pickDateTime { millis ->
                selectedMillis = millis
                delayTimeText.text = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(millis))
            }
        }

        fun submit(status: String) {
            val remarks = dialog.findViewById<EditText>(R.id.dlgRemarks).text.toString()
            val delayIso = if (status == "DELAYED") {
                val millis = selectedMillis
                if (millis == null) {
                    Toast.makeText(this@MainActivity, "Please set the new date & time for DELAY.", Toast.LENGTH_LONG).show()
                    return
                }
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date(millis))
            } else null
            sendAction(request.id, status, remarks, delayIso) { success ->
                dialog.dismiss()
                if (success) {
                    Toast.makeText(this@MainActivity, "Work order ${status} saved.", Toast.LENGTH_LONG).show()
                    loadRequests()
                }
            }
        }

        dialog.findViewById<MaterialButton>(R.id.btnApprove).setOnClickListener { submit("APPROVED") }
        dialog.findViewById<MaterialButton>(R.id.btnDisapprove).setOnClickListener { submit("DISAPPROVED") }
        dialog.findViewById<MaterialButton>(R.id.btnDelay).setOnClickListener { submit("DELAYED") }
        dialog.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener { submit("CANCELLED") }

        dialog.show()
    }

    private fun pickDateTime(onPicked: (Long) -> Unit) {
        val base = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        val cal = Calendar.getInstance()
                        cal.set(year, month, day, hour, minute, 0)
                        onPicked(cal.timeInMillis)
                    },
                    base.get(Calendar.HOUR_OF_DAY),
                    base.get(Calendar.MINUTE),
                    false
                ).show()
            },
            base.get(Calendar.YEAR),
            base.get(Calendar.MONTH),
            base.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun sendAction(id: Int, status: String, remarks: String, delayIso: String?, onDone: (Boolean) -> Unit) {
        val body = ApproverActionRequest(
            status = status,
            bderemarks = remarks,
            delaydateandtime = delayIso,
            approverEmail = approverEmail
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.updateRequestAction(id, body)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        onDone(true)
                    } else {
                        Toast.makeText(this@MainActivity, "Failed: HTTP ${response.code()}", Toast.LENGTH_LONG).show()
                        onDone(false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "API Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    onDone(false)
                }
            }
        }
    }

    private fun parseMillis(ts: String?): Long {
        if (ts.isNullOrBlank()) return 0L
        val formats = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss"
        )
        for (f in formats) {
            try {
                val sdf = SimpleDateFormat(f, Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(ts)?.time ?: 0L
            } catch (_: Exception) {
            }
        }
        return 0L
    }

    inner class RequestAdapter(private val onClick: (WorkOrderRequest) -> Unit) :
        RecyclerView.Adapter<RequestAdapter.ViewHolder>() {

        private var items: List<WorkOrderRequest> = emptyList()

        fun submit(list: List<WorkOrderRequest>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val woText = view.findViewById<TextView>(R.id.woText)
            private val statusText = view.findViewById<TextView>(R.id.statusText)
            private val partnerText = view.findViewById<TextView>(R.id.partnerText)
            private val channelText = view.findViewById<TextView>(R.id.channelText)
            private val delayText = view.findViewById<TextView>(R.id.delayText)
            private val personText = view.findViewById<TextView>(R.id.personText)
            private val notesText = view.findViewById<TextView>(R.id.notesText)
            private val timeText = view.findViewById<TextView>(R.id.timeText)

            fun bind(item: WorkOrderRequest) {
                woText.text = item.workordernumber ?: "—"

                val status = item.status ?: "—"
                statusText.text = status
                statusText.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(itemView.context, statusColor(status))
                )

                partnerText.text = item.partner ?: "—"
                partnerText.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(itemView.context, partnerColor(item.partner))
                )

                channelText.text = item.channel ?: "—"

                delayText.text = item.delay ?: ""

                personText.text = if (item.with3waynotes == true) {
                    (item.person3wayed ?: "—") + "  •  3-WAY"
                } else {
                    item.person3wayed ?: "—"
                }

                notesText.text = item.threewaynotes ?: ""

                timeText.text = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                    .format(Date(parseMillis(item.timestamp)))

                itemView.setOnClickListener { onClick(item) }
            }
        }
    }

    private fun statusColor(status: String): Int {
        return when (status) {
            "PENDING" -> R.color.status_pending
            "APPROVED" -> R.color.status_approved
            "DISAPPROVED" -> R.color.status_disapproved
            "DELAYED" -> R.color.status_delayed
            else -> R.color.status_cancelled
        }
    }

    private fun partnerColor(partner: String?): Int {
        return when (partner?.uppercase()) {
            "EBISU" -> R.color.partner_ebisu
            "TESCO" -> R.color.partner_tesco
            else -> R.color.chip_default
        }
    }
}