package com.fongmi.android.tv.api.network

import java.io.Serializable

data class ResponseResult<T>(
    val code: Int = -1,
    val msg: String? = "",
    val data: T? = null
): Serializable {
    fun isSuccess() = code == 200
}