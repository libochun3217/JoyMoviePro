package com.fongmi.android.tv.api

import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.LogUtils
import com.fongmi.android.tv.bean.Live
import com.github.catvod.utils.Path
import java.io.File
import java.util.logging.Logger

object LiveCache {
    private val cache = CacheDiskUtils.getInstance(File(Path.tv(), "live_cache"))
    private val TAG = "LiveCache"
    private val KEY_TAG = "key_tag"

    fun saveLive(live: Live) {
        if (cache.getSerializable(live.textHash) == null && !live.groups.isNullOrEmpty()) {
            cache.put(live.textHash, live)
            addKey(live.textHash)
            LogUtils.dTag(TAG, "saveLive")
        }
    }

    private fun getKeys(): ArrayList<String> {
        return cache.getSerializable(KEY_TAG, ArrayList<String>()) as ArrayList<String>
    }

    private fun addKey(key: String) {
        val keys = getKeys().apply { add(key) }
        cache.put(KEY_TAG, keys)
    }

    fun fromCache(): Live? {
        LogUtils.dTag(TAG, "fromCache")
        return cache.getSerializable(getKeys().firstOrNull() ?: "") as? Live
    }
}