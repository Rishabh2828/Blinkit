package com.shurish.blinkit.api

import com.shurish.blinkit.models.CheckStatus
import com.shurish.blinkit.models.Notification
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {
    @GET("apis/pg-sandbox/pg/v1/status/{merchantId}/{transactionId}")
    suspend fun checkStatus(
        @HeaderMap headers : Map<String, String>,
        @Path("merchantId") merchantId : String,
        @Path("transactionId") transactionId : String,


    ) : Response<CheckStatus>


    @Headers(
        "Content-Type: application/json",
        "Authorization: key=AAAAwBsPA90:APA91bFWkBIUoLFkv2cMXAXZXQKb-ERp1pQRmbA-RTgjCl7wPS_ANaK0TZROTskrbL3k3ujFvki3JcKFRnJ57CpZmuXGz-P5_uP3pGhHWSO0_lg6mLh6tKcjp4EWcRd1-1SuHaa_MGMp"
    )
    @POST("fcm/send")
    fun sendNotification(@Body notification : Notification) : Call<Notification>
}