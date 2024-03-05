package com.fongmi.android.tv.api.network

import android.os.Build
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.charlee.android.tv.BuildConfig
import com.fongmi.android.tv.api.ServerApi.Companion.instance

object UserService {
    private const val TOKEN_KEY = "token_key"
    private const val LAST_LOGIN_KEY = "last_login_key"

    fun userName() =
        "${BuildConfig.FLAVOR_mode}_${Build.BRAND}_${DeviceUtils.getModel()}_${AppUtils.getAppVersionName()}_Android-${DeviceUtils.getSDKVersionName()}_${DeviceUtils.getUniqueDeviceId()}"

    fun buildLoginReq(): LoginRequest {
        val uuid = DeviceUtils.getUniqueDeviceId()
        val password = EncryptUtils.encryptMD5ToString("$uuid${userName()}ruoyi")
        return LoginRequest(userName(), password, uuid)
    }

    fun login(force: Boolean = false) {
        val lastLogin = SPUtils.getInstance().getLong(LAST_LOGIN_KEY, 0)
        // 10 小时不重复登录
        if (!force && (System.currentTimeMillis() - lastLogin) < 1000 * 60 * 60 * 10) return
        instance.login(buildLoginReq()).req {
            it?.token?.let { saveToken(it) }
        }
    }

    fun getToken(): String? = SPUtils.getInstance().getString(TOKEN_KEY)
    fun removeToken() = SPUtils.getInstance().remove(TOKEN_KEY)

    private fun saveToken(token: String) {
        SPUtils.getInstance().put(TOKEN_KEY, token)
        SPUtils.getInstance().put(LAST_LOGIN_KEY, System.currentTimeMillis())
    }
}
