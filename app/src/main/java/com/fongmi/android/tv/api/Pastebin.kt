package com.fongmi.android.tv.api

import com.blankj.utilcode.util.ToastUtils
import com.fongmi.android.tv.api.network.req
import java.text.SimpleDateFormat
import java.util.Locale

private var apiUserKey = ""
private val apiDevKey = "LJgKomuYvNVcfgMPRZrRXRPwShXGs3mk"

fun login() {
    if (apiUserKey.isNotEmpty()) return

    ServerApi.instance.loginV2(
        mapOf(
            "api_dev_key" to apiDevKey,
            "api_user_name" to "charlee3218",
            "api_user_password" to "8921603217abc"
        )
    ).req {
        apiUserKey = it ?: ""
        ToastUtils.showShort(apiUserKey)
    }

}


fun upload(data: String) {
    ServerApi.instance.upload(
        mapOf(
            "api_dev_key" to apiDevKey,
            "api_paste_code" to data,
            "api_paste_name" to format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm"),
            "api_paste_expire_date" to "1Y",
            "api_user_key" to apiUserKey,
            "api_option" to "paste"
        )
    ).req {

    }
}

private val formatDateMap by lazy { hashMapOf<String, SimpleDateFormat>() }

fun format(time: Long, formatStr: String): String {
    var df = formatDateMap[formatStr]
    if (df == null) {
        df = SimpleDateFormat(formatStr, Locale.getDefault())
        formatDateMap[formatStr] = df
    }
    return df.format(time)
}

