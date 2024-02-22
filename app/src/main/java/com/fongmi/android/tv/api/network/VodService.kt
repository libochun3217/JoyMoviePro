package com.fongmi.android.tv.api.network

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.fongmi.android.tv.api.CacheManger
import com.fongmi.android.tv.api.ServerApi
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.api.network.Constants.Companion.NETWORK_TAG
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.player.Players

object VodService {
    private const val TAG = "VodService"
    private const val KEY_VOD_UPLOAD = "key_vod_upload"

    fun checkVodStatus(vod: Vod?, siteKey: String?, players: Players?, watchMinute: Long) {
        val site = VodConfig.get().getSite(siteKey)
        val config = VodConfig.get().config
        val vodRecord = VodRecord(
            watchMinute = watchMinute,
            vodName = vod?.vodName,
            vodActor = vod?.vodActor,
            vodArea = vod?.vodArea,
            vodContent = vod?.vodContent,
            vodDirector = vod?.vodDirector,
            vodWith = players?.videoWidth,
            vodHeight = players?.videoHeight,
            vodPic = vod?.vodPic,
            vodRemarks = vod?.vodRemarks,
            vodYear = vod?.vodYear,
            siteApi = site?.api,
            siteName = site?.name,
            siteType = site?.type,
            configName = config?.name,
            configUrl = config?.url,
            playHeader = players?.headers?.map { "${it.key}=${it.value}" }?.joinToString("@@@"),
            playUrl = players?.url,
            typeName = vod?.typeName
        )
        CacheManger.saveVodRecord(vodRecord)
        val lastUploadTime = SPUtils.getInstance().getLong(KEY_VOD_UPLOAD, 0L)
        if ((System.currentTimeMillis() - lastUploadTime) < 1000 * 60 * 60 * 24) return

        ServerApi.instance.vodRecordUpload(VodRecordRequest(CacheManger.getVodRecords())).req {
            if (it?.isSuccess() == true) {
                LogUtils.dTag(NETWORK_TAG, "vod records upload success")
                SPUtils.getInstance().put(KEY_VOD_UPLOAD, System.currentTimeMillis())
                CacheManger.clearVodRecords()
            }
        }
    }
}