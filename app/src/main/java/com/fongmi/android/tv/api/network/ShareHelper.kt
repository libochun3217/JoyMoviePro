package com.fongmi.android.tv.api.network

import android.graphics.Bitmap
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SpanUtils
import com.blankj.utilcode.util.ToastUtils
import com.charlee.android.tv.R
import com.fongmi.android.tv.App
import com.fongmi.android.tv.api.network.tools.ConfirmDialog

object ShareHelper {
    private const val KEY_WATCH_MINUTE = "key_watch_minute"
    private const val KEY_IMAGE_SAVED = "key_image_saved"
    private const val SHOW_TIMES = 100
    const val KEY_HAS_SHOW = "key_has_show1"

    fun addWatchMinute(watch: Int) {
        val watchMinute = SPUtils.getInstance().getInt(KEY_WATCH_MINUTE)
        SPUtils.getInstance().put(KEY_WATCH_MINUTE, watchMinute + watch)
    }

    fun check() {
        val watchMinute = SPUtils.getInstance().getInt(KEY_WATCH_MINUTE)
        if (watchMinute < 300) return
        val hasShow = SPUtils.getInstance().getInt(KEY_HAS_SHOW)
        if (hasShow >= SHOW_TIMES) return
        showDialog(hasShow)
    }

    private fun showDialog(hasShow: Int) {
        ActivityUtils.getTopActivity()?.let {
            ConfirmDialog().show(
                it,
                "动动小手",
                "请帮忙分享链接或二维码到微信群或好友，分享后不再弹出",
                "确定",
                {
                    saveImageAndClip(hasShow)
                    SPUtils.getInstance().put(KEY_HAS_SHOW, hasShow + 10)
                }) {
                SPUtils.getInstance().put(KEY_HAS_SHOW, hasShow + 1)
            }
        }
    }

    private fun saveImageAndClip(hasShow: Int) {
        App.execute {
            ClipboardUtils.copyText("https://www.123pan.com/s/gBTZjv-43YZv.html")
            if (hasShow > SHOW_TIMES / 2) {
                ToastUtils.showLong("分享后用户安装就不再弹出")
            } else ToastUtils.showLong("链接已复制，二维码已保存到相册,去分享吧")
            if (!SPUtils.getInstance().getBoolean(KEY_IMAGE_SAVED)) {
                val image = ImageUtils.getBitmap(R.drawable.scan)
                ImageUtils.save2Album(image, Bitmap.CompressFormat.JPEG)
                SPUtils.getInstance().put(KEY_IMAGE_SAVED, true)
            }
        }
    }
}