package li.songe.gkd.a11y

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import li.songe.gkd.a11y.utils.appListenerFile
import li.songe.gkd.a11y.utils.appendTime
import java.lang.Exception

object QListener {
    private var lastRead = System.currentTimeMillis()
    private val currentMessageList = ArrayList<String>()
    private val allMessageList = ArrayList<String>()
    private val uploadMessageList = ArrayList<String>()
    private var defaultSleep = 1000 * 5
    private var sleep = defaultSleep
    private var uploading = false
    private val TAG = "QListener"


    fun onAccessibilityEvent(
        event: AccessibilityEvent?,
        rootInActiveWindow: AccessibilityNodeInfo?
    ) {
        if ((System.currentTimeMillis() - lastRead) < sleep) return

        val app = event?.packageName.toString()
        if (app != "com.tencent.mobileqq") return
        if (uploadMessageList.size > 100 && !uploading) {
            allMessageList.addAll(uploadMessageList)
            uploadMessage()
            if (allMessageList.size > 100 * 1000) allMessageList.clear()
        }
        rootInActiveWindow?.let {
            lastRead = System.currentTimeMillis()
            Log.d(TAG, "qq start")
            findAll(it, 0)
            if (currentMessageList.contains("加号")) {
                sleep = defaultSleep
                uploadMessageList.addAll(currentMessageList)
                currentMessageList.clear()
            } else {
                sleep = defaultSleep * 2
            }
        }
    }

    private fun findAll(node: AccessibilityNodeInfo, level: Int) {
        if (level > 25) {
            return
        }
        for (i in 0 until node.childCount) {
            val node1 = node.getChild(i) ?: return
            var className: String? = null
            try {
                className = node1.className.toString() + ""
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (className == "android.widget.TextView") {
                val message = node1.text?.toString() ?: ""
                addMessage(message)
                Log.d(TAG, message)
            } else if (className == "android.widget.ImageView") {
                val cuser = node1.contentDescription?.toString() ?: ""
                if (cuser == "加号") {
                    currentMessageList.add(cuser)
                    Log.d(TAG, cuser)
                }
            } else if (node1.childCount > 0) {
                findAll(node1, level + 1)
            }
        }
    }

    private fun uploadMessage() {
        uploading = true
        var liveMessage = "**********QQ**********"
        uploadMessageList.map { liveMessage = "$liveMessage\n$it" }
        uploadMessageList.clear()
        uploading = false
        appListenerFile.appendText(liveMessage)
        appListenerFile.appendTime()
    }

    private fun addMessage(message: String) {
        currentMessageList.add(message)
    }
}