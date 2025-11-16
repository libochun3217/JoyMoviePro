package li.songe.gkd.a11y

import android.content.Context.WINDOW_SERVICE
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import li.songe.gkd.service.A11yService


val onA11yFeatInit = fun (service:A11yService){
    service.run {
        useAttachState()
        useAliveOverlayView()
        onA11yEvent { onA11yFeatEvent(it) }
        onDestroyed {
        }
    }
}
private fun A11yService.useAttachState() {
}

private fun onA11yFeatEvent(event: AccessibilityEvent) = event.run {
    Listener.onAccessibilityEvent(event)
}


private fun AccessibilityEvent.getEventAttr(name: String): Any? = when (name) {
    "name" -> className
    "desc" -> contentDescription
    "text" -> text
    else -> null
}

private fun A11yService.useAliveOverlayView() {
    val context = this
    var aliveView: View? = null
    val wm by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    fun removeA11View() {
        if (aliveView != null) {
            wm.removeView(aliveView)
            aliveView = null
        }
    }

    fun addA11View() {
        removeA11View()
        val tempView = View(context)
        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags =
                flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            gravity = Gravity.START or Gravity.TOP
            width = 1
            height = 1
            packageName = context.packageName
        }
        wm.addView(tempView, lp)
    }
    onA11yConnected { addA11View() }
    onDestroyed { removeA11View() }
}