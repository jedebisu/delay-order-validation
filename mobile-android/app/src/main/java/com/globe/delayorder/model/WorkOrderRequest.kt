package com.globe.delayorder.model

import com.google.gson.annotations.SerializedName

data class WorkOrderRequest(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("workordernumber") val workordernumber: String? = null,
    @SerializedName("partner") val partner: String? = null,
    @SerializedName("channel") val channel: String? = null,
    @SerializedName("delay") val delay: String? = null,
    @SerializedName("delaydateandtime") val delaydateandtime: String? = null,
    @SerializedName("with3waynotes") val with3waynotes: Boolean? = false,
    @SerializedName("person3wayed") val person3wayed: String? = null,
    @SerializedName("threewaynotes") val threewaynotes: String? = null,
    @SerializedName("status") val status: String? = "PENDING",
    @SerializedName("bderemarks") val bderemarks: String? = null,
    @SerializedName("approverEmail") val approverEmail: String? = null,
    @SerializedName("actionTakenAt") val actionTakenAt: String? = null
)

data class ApproverActionRequest(
    @SerializedName("status") val status: String,
    @SerializedName("bderemarks") val bderemarks: String,
    @SerializedName("delaydateandtime") val delaydateandtime: String? = null,
    @SerializedName("approverEmail") val approverEmail: String = "cpjuezan@globe.com.ph"
)
