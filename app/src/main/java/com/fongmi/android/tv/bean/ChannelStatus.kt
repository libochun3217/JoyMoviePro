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
                        if (it.watchStartTimestamp > 0) {
                            val minutes =
                                ((System.currentTimeMillis() - it.watchStartTimestamp) / 1000 / 60).toInt()
                            it.watchMinutes = it.watchMinutes + minutes
                            if (minutes > 0) it.failedTime = 0
                            it.watchStartTimestamp = 0
                        }
                        LogUtils.iTag(
                            TAG,
                            "${channel.name} watchMinutes ${it.watchMinutes} failed times ${it.failedTime}"
                        )
                    }
                }
            }
        }

        fun removeFailedChannel(live: Live) {
            live.groups.map {
                val failed = it.channel.filter { it?.channelStatus?.failedTime ?: 0 > 0 }
                it.channel.removeAll(failed)
                if (failed.isNotEmpty()) {
                    LogUtils.iTag(TAG, "remove ${failed.map { it.name }}")
                }
            }
        }
    }
}