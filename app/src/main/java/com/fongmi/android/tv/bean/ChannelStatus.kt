package com.fongmi.android.tv.bean

import com.blankj.utilcode.util.LogUtils
import java.io.Serializable

data class ChannelStatus(
    var watchMinutes: Int = 0,
    var watchStartTimestamp: Long = 0,
    var failedTime: Int = 0
) : Serializable {
    companion object {
        private const val TAG = "ChannelStatus"
        fun checkStatus(live: Live) {
            live.groups.map {
                it.channel.map { channel ->
                    channel.channelStatus?.let {
                        val minutes = ((System.currentTimeMillis() - it.watchStartTimestamp) / 1000 / 60).toInt()
                        it.watchMinutes = it.watchMinutes + minutes
                        LogUtils.iTag(TAG, "${channel.name} watchMinutes ${it.watchMinutes}")
                    }
                }
            }
        }
    }
}