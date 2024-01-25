package com.fongmi.android.tv.api

import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.LogUtils
import com.fongmi.android.tv.api.CacheKeyManager.addKey
import com.fongmi.android.tv.api.CacheKeyManager.cache
import com.fongmi.android.tv.api.CacheKeyManager.getKeys
import com.fongmi.android.tv.bean.Live
import com.github.catvod.utils.Path
import java.io.File

/**
 * 先取缓存、再更新缓存
 * live 缓存策略
 * 1、url 得到response
 * 2、response 得到 hash
 * 3、hash 得到 live
 *
 * config 策略
 * 1、得到所有config url
 * 2、url 得到 response
 */
object CacheManger {
    private val TAG = "CacheManger"
    private val KEY_LIVE = "key_live"
    private val KEY_CONFIG = "key_config"
    const val TYPE_LIVE = 1

    fun saveLive(live: Live) {
        if (live.groups.isNullOrEmpty()) {
            LogUtils.dTag(TAG, "saveLive groups == null")
            return
        }
        cache.put(live.contentHash, live)
        addKey(KEY_LIVE, live.contentHash)
        LogUtils.dTag(TAG, "saveLive ${live.name}")
    }

    fun saveConfig(url: String) {
        addKey(KEY_CONFIG, url)
    }

    fun getConfigs(): ArrayList<String> = getKeys(KEY_CONFIG)

    fun saveResponse(url: String, response: String) {
        if (response.isNullOrEmpty()) return
        cache.put(url, response)
        LogUtils.dTag(TAG, "saveResponse $url")
    }

    fun getResponse(url: String) = cache.getString(url)


    fun fromCache(url: String): Live? {
        getResponse(url)?.let {
            val hash = EncryptUtils.encryptMD5ToString(it)
            val c = cache.getSerializable(hash) as? Live
            LogUtils.iTag(TAG, "fromCache")
            if (c?.groups.isNullOrEmpty()) return null
            return c
        }
        return null
    }
}

object CacheKeyManager {
    private val KEY_TAG = "key_tag"
    private val keys by lazy {
        cache.getSerializable(
            KEY_TAG,
            HashMap<String, ArrayList<String>>()
        ) as HashMap<String, ArrayList<String>>
    }
    val cache = CacheDiskUtils.getInstance(File(Path.tv(), "cache"))

    fun getKeys(keyTag: String): ArrayList<String> {
        if (keys[keyTag] == null) keys[keyTag] = ArrayList()
        return keys[keyTag]!!
    }

    fun addKey(keyTag: String, key: String) {
        getKeys(keyTag).add(key)
        cache.put(KEY_TAG, keys)
    }

}