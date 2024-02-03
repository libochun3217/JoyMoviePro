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