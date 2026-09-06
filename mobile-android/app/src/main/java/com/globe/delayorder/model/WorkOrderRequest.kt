package com.globe.delayorder.model

data class WorkOrderRequest(
    val id: Int,
    val timestamp: String,
    val work_order_number: String,
    val partner: String,
    val channel: String,
    val delay_info: String?,
    val delay_date_and_time: String?,
    val with_3_way_notes: Boolean,
    val person_3_wayed: String?,
    val three_way_notes: String?,
    val status: String,
    val bde_remarks: String?,
    val approver_email: String?,
    val action_taken_at: String?
)

data class ApproverActionRequest(
    val status: String,
    val bderemarks: String,
    val delaydateandtime: String? = null,
    val approverEmail: String = "cpjuezan@globe.com.ph"
)
