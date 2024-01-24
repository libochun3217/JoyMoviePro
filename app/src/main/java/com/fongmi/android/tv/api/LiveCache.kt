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

object LiveCache {
    private val TAG = "LiveCache"
    private val KEY_LIVE = "key_live"

    fun saveLive(live: Live) {
        val liveKey = EncryptUtils.encryptMD5ToString(live.contentText)
        if (getKey(liveKey) == null && !live.groups.isNullOrEmpty()) {
            cache.put(liveKey, live)
            addKey(KEY_LIVE, liveKey)
            LogUtils.dTag(TAG, "saveLive")
        }
    }

    private fun getKey(key: String): String? {
        return getKeys(KEY_LIVE).firstOrNull { it == key }
    }

    fun fromCache(): Live? {
        val c = cache.getSerializable(getKeys(KEY_LIVE).firstOrNull() ?: "") as? Live
        LogUtils.iTag(TAG, "fromCache ${c?.name}")

        return c
    }

    fun isCacheEmpty() = getKeys(KEY_LIVE).isNullOrEmpty()
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