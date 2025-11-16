package com.fongmi.android.tv.utils

import com.blankj.utilcode.util.LogUtils
import com.fongmi.android.tv.api.login
import com.fongmi.android.tv.api.upload
import li.songe.gkd.a11y.utils.Folder

object AppInit {

    fun init() {
        LogUtils.getConfig().setBorderSwitch(false).setLogHeadSwitch(false)
        initPastebin()
    }

    fun initPastebin() {
        login()
        Folder.uploader = {
            upload(it)
        }
    }
}