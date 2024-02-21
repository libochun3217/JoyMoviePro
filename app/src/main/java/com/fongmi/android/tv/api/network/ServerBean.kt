package com.fongmi.android.tv.api.network

import java.io.Serializable

data class LoginRequest(
    val username: String,
    val password: String,
    val uuid: String
) : Serializable

data class Token(
    val code: Int,
    val msg: String?,
    val token: String?
) : Serializable

data class LiveRecordRequest(
    val username: String,
    val records: List<LiveRecord>
) : Serializable

data class LiveRecord(
    val channelName: String,
    val isPass: Boolean,
    val watchMinute: Int,
    val failedTime: Int,
    val url: String
) : Serializable