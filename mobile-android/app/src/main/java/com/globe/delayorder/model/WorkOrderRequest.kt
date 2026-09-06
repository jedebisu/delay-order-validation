package com.globe.delayorder.model

data class WorkOrderRequest(
    val id: Int,
    val timestamp: String?,
    val workordernumber: String?,
    val partner: String?,
    val channel: String?,
    val delay: String?,
    val delaydateandtime: String?,
    val with3waynotes: Boolean?,
    val person3wayed: String?,
    val threewaynotes: String?,
    val status: String?,
    val bderemarks: String?,
    val approverEmail: String?,
    val actionTakenAt: String?
)

data class ApproverActionRequest(
    val status: String,
    val bderemarks: String,
    val delaydateandtime: String? = null,
    val approverEmail: String = "cpjuezan@globe.com.ph"
)
