package com.fongmi.android.tv.api

import com.fongmi.android.tv.api.network.LiveRecordRequest
import com.fongmi.android.tv.api.network.LoginRequest
import com.fongmi.android.tv.api.network.ResponseResult
import com.fongmi.android.tv.api.network.Token
import com.fongmi.android.tv.api.network.VodRecordRequest
import com.fongmi.android.tv.api.network.retrofit
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ServerApi {
    companion object {
        val instance by lazy { retrofit.create(ServerApi::class.java) }
    }

    // 登录接口
    @POST("/loginV2")
    fun login(@Body request: LoginRequest): Call<Token>


    // 直播记录上报接口
    @POST("/admin/live/addRecords")
    fun liveRecordUpload(@Body request: LiveRecordRequest): Call<ResponseResult<String>>


    // 点播记录上报接口
    @POST("/admin/vod/addRecords")
    fun vodRecordUpload(@Body request: VodRecordRequest): Call<ResponseResult<String>>
}