package com.fongmi.android.tv.api.network

import android.os.Build
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.charlee.android.tv.BuildConfig
import com.fongmi.android.tv.api.ServerApi.Companion.instance

object UserService {
    private const val TOKEN_KEY = "token_key"
    private var lastLogin = 0L

    fun userName() = "${BuildConfig.FLAVOR_mode}_${Build.BRAND}_${DeviceUtils.getModel()}_${Build.CPU_ABI}_Android-${DeviceUtils.getSDKVersionName()}_${DeviceUtils.getUniqueDeviceId()}"

    fun buildLoginReq(): LoginRequest {
        val uuid = DeviceUtils.getUniqueDeviceId()
        val password = EncryptUtils.encryptMD5ToString("$uuid${userName()}ruoyi")
        return LoginRequest(userName(), password, uuid)
    }

    fun login() {
        if (!getToken().isNullOrEmpty() || (System.currentTimeMillis() - lastLogin) < 1000*60*60) return
        instance.login(buildLoginReq()).req {
            it?.token?.let { saveToken(it) }
        }
    }

    fun getToken(): String? = SPUtils.getInstance().getString(TOKEN_KEY)
    fun removeToken() = SPUtils.getInstance().remove(TOKEN_KEY)

    private fun saveToken(token: String) {
        lastLogin = System.currentTimeMillis()
        SPUtils.getInstance().put(TOKEN_KEY, token)
    }
}
