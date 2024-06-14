package com.fongmi.android.tv.api.network

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ThreadUtils
import com.fongmi.android.tv.App
import com.fongmi.android.tv.api.CacheManger.TYPE_VOD
import com.fongmi.android.tv.api.Decoder
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Config
import com.fongmi.android.tv.impl.Callback


object InternalConfig {
    val cacheLiveUrl = "https://youdu.fan/yd/tvlive1.txt"
    val defaultVod = "https://pastebin.com/raw/5FaPpZkr"
    private val KEY_VOD_CACHE = "key_vod_cache"

    fun check(callback: Callback) {
        App.execute {
            val json = Decoder.getJson(defaultVod)
            val lastJson = SPUtils.getInstance().getString(KEY_VOD_CACHE, "")
            if (json != lastJson) {
                LogUtils.d("refresh config")
                Config.getAll(TYPE_VOD).map {
                    it.delete()
                    LogUtils.d("delete ${it.url}")
                }
                ThreadUtils.runOnUiThreadDelayed({
                    val config = Config.find(defaultVod, TYPE_VOD)
                    VodConfig.load(config, callback)
                }, 1000)
                SPUtils.getInstance().put(KEY_VOD_CACHE, json)
            }
        }
    }
}