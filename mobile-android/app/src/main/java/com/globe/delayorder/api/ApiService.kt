package com.globe.delayorder.api

import com.globe.delayorder.model.ApproverActionRequest
import com.globe.delayorder.model.WorkOrderRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface ApiService {
    @GET("api/requests")
    suspend fun getRequests(): Response<List<WorkOrderRequest>>

    @PATCH("api/requests/{id}/action")
    suspend fun updateRequestAction(
        @Path("id") id: Int,
        @Body actionRequest: ApproverActionRequest
    ): Response<WorkOrderRequest>
}
