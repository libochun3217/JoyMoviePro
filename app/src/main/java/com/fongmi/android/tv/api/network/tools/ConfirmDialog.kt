package com.fongmi.android.tv.api.network.tools

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.charlee.android.tv.R

class ConfirmDialog {

    fun show(
        activity: Activity?,
        title: String?,
        tips: String,
        positiveString: String? = null,
        positiveAction: () -> Unit,
        negativeAAction: (() -> Unit)? = null
    ) {
        if (activity == null) return
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_share, null, false)
        dialogView.findViewById<TextView>(R.id.tv_title).apply {
            isVisible = !title.isNullOrEmpty()
            text = title
        }
        dialogView.findViewById<TextView>(R.id.tv_tips).apply {
            if (tips.isNotBlank()) {
                visibility = View.VISIBLE
                text = tips
            }
            if (title.isNullOrEmpty()) {
                val layoutParams = layoutParams as RelativeLayout.LayoutParams
                layoutParams.topMargin = SizeUtils.dp2px(32f)
            }
        }

        val dialog = AlertDialog.Builder(activity).create().apply {
            setCancelable(false)
            show()
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setContentView(dialogView)
        }
        dialogView.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<TextView>(R.id.tv_confirm).setOnClickListener {
            dialog.dismiss()
            positiveAction.invoke()
        }
        positiveString?.let {
            dialogView.findViewById<TextView>(R.id.tv_confirm).text = it
        }

    }

    fun show(activity: Activity, title: String, positiveAction: () -> Unit) {
        show(activity, title, "", positiveString = null, positiveAction)
    }
}