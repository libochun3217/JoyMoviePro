package com.fongmi.android.tv.api.network

import android.annotation.SuppressLint
import com.blankj.utilcode.util.LogUtils
import com.fongmi.android.tv.api.network.Constants.Companion.NETWORK_CODE
import com.fongmi.android.tv.api.network.Constants.Companion.NETWORK_TAG
import com.fongmi.android.tv.api.network.Constants.Companion.NETWORK_TIME_OUT
import com.fongmi.android.tv.api.network.Constants.Companion.NETWORK_TOKEN_EXPIRED
import com.fongmi.android.tv.api.network.Constants.Companion.NETWORK_TOKEN_NOT_FOUND
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject

class ResponseFilterInterceptor : Interceptor {
    companion object {
        const val TAG = "NetworkLogInterceptor"
    }

    @SuppressLint("MissingPermission")
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val newHeader = request.headers.newBuilder()
            .apply { headers().entries.forEach { add(it.key, it.value) } }
            .build()

        request = request.newBuilder()
            .headers(newHeader)
            .build()

        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            LogUtils.dTag(
                NETWORK_TAG,
                "network timeout \n url: ${request.url}, \n message: ${e.message}"
            )

            Response.Builder().protocol(Protocol.HTTP_1_1).request(request).code(NETWORK_TIME_OUT)
                .message(e.message ?: "").body("".toResponseBody()).build()
        }
        val responseString = response.body?.string()
        loginExpiredInterceptor(responseString)
        return response.newBuilder().body(responseString?.toResponseBody()).build()
    }

    private fun headers(): HashMap<String, String> {
        return hashMapOf("Authorization" to (UserService.getToken() ?: ""))
    }

    private fun loginExpiredInterceptor(responseString: String?) {
        if (isLoginExpired(responseString)) {
            // check time and login again
            if (!UserService.getToken().isNullOrEmpty()) {
                UserService.removeToken()
                UserService.login()
            }
        }
    }

    private fun isLoginExpired(responseString: String?): Boolean {
        return responseString?.let {
            try {
                val code = JSONObject(it).optInt(NETWORK_CODE)
                code == NETWORK_TOKEN_EXPIRED ||code == NETWORK_TOKEN_NOT_FOUND
            } catch (e: Exception) {
                return false
            }
        } ?: false
    }
}