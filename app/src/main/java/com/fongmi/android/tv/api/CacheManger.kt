package com.fongmi.android.tv.api

import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.LogUtils
import com.fongmi.android.tv.api.CacheKeyManager.cache
import com.fongmi.android.tv.api.network.InternalConfig
import com.fongmi.android.tv.api.network.LiveRecord
import com.fongmi.android.tv.api.network.VodRecord
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
    private val KEY_VOD_RECORDS = "key_vod_records"
    private const val KEY_LIVE_RECORDS = "key_live_records"

    const val TYPE_LIVE = 1
    const val TYPE_VOD = 0

    fun saveVodRecord(vodRecord: VodRecord) {
        getVodRecords().apply {
            add(vodRecord)
            cache.put(KEY_VOD_RECORDS, this)
        }
    }

    fun saveLiveRecords(liveRecords: ArrayList<LiveRecord>) = cache.put(KEY_LIVE_RECORDS, liveRecords)

    fun getLiveRecords() = cache.getSerializable(KEY_LIVE_RECORDS) as? ArrayList<LiveRecord> ?: ArrayList()

    fun clearVodRecords() = cache.put(KEY_VOD_RECORDS, ArrayList<VodRecord>())

    fun getVodRecords() = cache.getSerializable(KEY_VOD_RECORDS) as? ArrayList<VodRecord> ?: ArrayList()

    fun saveResponse(url: String, response: String) {
        if (response.isNullOrEmpty() || response.contains("<head><title>404 Not Found</title></head>")) return
        cache.put(url, response)
        LogUtils.dTag(TAG, "saveResponse $url")
    }

    fun getResponse(url: String): String? {
        if (url == InternalConfig.cacheUrl) return InternalConfig.build()
        if (url == InternalConfig.cacheLiveUrl) return InternalConfig.buildLive()
        return cache.getString(url)
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
        val keyList = getKeys(keyTag)
        if (!keyList.contains(key)) {
            getKeys(keyTag).add(key)
            cache.put(KEY_TAG, keys)
        }
    }

}