package com.fongmi.android.tv.api.network

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.fongmi.android.tv.api.CacheManger
import com.fongmi.android.tv.api.ServerApi.Companion.instance
import com.fongmi.android.tv.bean.Channel
import com.fongmi.android.tv.bean.Group
import com.fongmi.android.tv.bean.Live

object LiveService {
    private const val KEY_LIVE_UPLOAD = "key_live_upload"
    val liveRecords by lazy { CacheManger.getLiveRecords() }

    fun addLiveRecord(
        liveName: String?,
        liveUrl: String?,
        isPass: Boolean,
        watchMinutes: Int,
        failedTime: Int
    ) {
        if (liveUrl.isNullOrEmpty() || (watchMinutes == 0 && failedTime == 0)) return
        if (liveUrl.length >= 500) return
        val old = liveRecords.firstOrNull { it.url == liveUrl }
        if (old != null) {
            old.watchMinute = old.watchMinute + watchMinutes
            old.failedTime = old.failedTime + failedTime
            if (watchMinutes > 0) old.failedTime = 0
        } else {
            liveRecords.add(
                LiveRecord(
                    liveName ?: "",
                    isPass,
                    watchMinutes,
                    failedTime,
                    liveUrl ?: ""
                )
            )
        }
        LogUtils.dTag("LiveService", "add live $liveName, old $old")
    }

    fun upload() {
//        CacheManger.saveLiveRecords(liveRecords)
//        val lastUploadTime = SPUtils.getInstance().getLong(KEY_LIVE_UPLOAD, 0L)
//        if ((System.currentTimeMillis() - lastUploadTime) < 1000 * 60 * 60 * 24) return
//        instance.liveRecordUpload(LiveRecordRequest(UserService.userName(), liveRecords)).req {
//            SPUtils.getInstance().put(KEY_LIVE_UPLOAD, System.currentTimeMillis())
//            CacheManger.saveLiveRecords(ArrayList())
//        }
    }
}