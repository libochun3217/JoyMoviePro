package com.fongmi.android.tv.api.network

import com.blankj.utilcode.util.SPUtils
import com.fongmi.android.tv.api.ServerApi.Companion.instance
import com.fongmi.android.tv.bean.Live

object LiveService {
    private const val KEY_LIVE_UPLOAD = "key_live_upload"

    fun upload(live: Live) {
        val lastUploadTime = SPUtils.getInstance().getLong(KEY_LIVE_UPLOAD, 0L)
        if ((System.currentTimeMillis() - lastUploadTime) < 1000 * 60 * 60 * 24) return
        val uploadChannels = ArrayList<LiveRecord>()
        live.groups.map { group ->
            group.channel.map {
                if (it.channelStatus != null) {
                    uploadChannels.add(
                        LiveRecord(
                            it.name,
                            group.pass.isEmpty(),
                            it.channelStatus.watchMinutes,
                            it.channelStatus.failedTime,
                            it.current.split("\\$")[0]
                        )
                    )
                }
            }
        }
        instance.liveRecordUpload(LiveRecordRequest(UserService.userName(), uploadChannels)).req {
            if (it?.isSuccess() == true) {
                SPUtils.getInstance().put(KEY_LIVE_UPLOAD, System.currentTimeMillis())
                live.groups.map {
                    it.channel.map {
                        it.channelStatus = null
                    }
                }
            }
        }
    }
}