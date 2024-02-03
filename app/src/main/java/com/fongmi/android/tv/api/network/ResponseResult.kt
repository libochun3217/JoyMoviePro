package com.fongmi.android.tv.api.network

data class ResponseResult<T>(
    val code: Int = -1,
    val msg: String? = "",
    val data: T? = null
)