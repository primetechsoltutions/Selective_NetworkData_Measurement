package com.ptsl.selective_network_sdk.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("NetworkMesurment/GetBandwithFile")
    suspend fun getBandwidthFile(@Query("Size") networkType: String?): Response<ResponseBody>

    @POST("NetworkMesurment/SaveBandwithFile")
    suspend fun saveBandwidthFile(@Body body: RequestBody): Response<Unit>
}