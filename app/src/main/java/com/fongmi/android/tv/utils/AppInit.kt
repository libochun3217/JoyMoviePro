package com.fongmi.android.tv.utils

import com.blankj.utilcode.util.LogUtils

object AppInit {

    fun init() {
        LogUtils.getConfig().setBorderSwitch(false).setLogHeadSwitch(false)
    }
}