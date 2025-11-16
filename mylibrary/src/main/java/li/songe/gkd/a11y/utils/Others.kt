package li.songe.gkd.a11y.utils

import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import com.blankj.utilcode.util.ToastUtils


// https://github.com/gkd-kit/gkd/issues/44
// java.lang.ClassNotFoundException:Didn't find class "android.app.IActivityTaskManager" on path: DexPathList
private val clazzMap = HashMap<String, Boolean>()
fun checkExistClass(className: String): Boolean = clazzMap[className] ?: try {
    Class.forName(className)
    true
} catch (_: Throwable) {
    false
}.apply {
    clazzMap[className] = this
}

// https://github.com/gkd-kit/gkd/issues/924
private val Drawable.safeDrawable: Drawable?
    get() = if (intrinsicHeight > 0 && intrinsicWidth > 0) {
        this
    } else {
        null
    }

private fun Char.isAsciiLetter(): Boolean {
    return this in 'a'..'z' || this in 'A'..'Z'
}

private fun Char.isAsciiVar(): Boolean {
    return this.isAsciiLetter() || this in '0'..'9' || this == '_'
}

private fun Char.isAsciiClassVar(): Boolean {
    return this.isAsciiVar() || this == '$'
}

// https://developer.android.com/build/configure-app-module?hl=zh-cn
fun String.isValidAppId(): Boolean {
    if (!contains('.')) return false
    if (!first().isAsciiLetter()) return false
    var i = 0
    while (i < length) {
        val c = get(i)
        if (c == '.') {
            i++
            if (getOrNull(i)?.isAsciiLetter() != true) {
                return false
            }
        } else if (!c.isAsciiVar()) {
            return false
        }
        i++
    }
    return true
}

fun String.isValidActivityId(): Boolean {
    if (isEmpty()) return false
    var i = 0
    while (i < length) {
        val c = get(i)
        if (c == '.') {
            i++
            if (getOrNull(i)?.isAsciiClassVar() == false) {
                return false
            }
        } else if (!c.isAsciiClassVar()) {
            return false
        }
        i++
    }
    return true
}

object AppListString {
    fun decode(text: String): Set<String> {
        return text.split('\n').filter { a -> a.isValidAppId() }.toHashSet()
    }

    fun encode(set: Set<String>, append: Boolean = false): String {
        val list = set.sorted()
        if (append) {
            return list.joinToString(separator = "\n\n", postfix = "\n\n") {
                val name = ""
                if (name != null) {
                    "$it\n# $name"
                } else {
                    it
                }
            }
        }
        return list.joinToString("\n")
    }
}

fun runMainPost(delayMillis: Long = 0L, r: Runnable) {
    if (delayMillis == 0L && Looper.getMainLooper() == Looper.myLooper()) {
        r.run()
        return
    }
    Handler(Looper.getMainLooper()).postDelayed(r, delayMillis)
}


fun toast(
    text: CharSequence,
) {
    if (text.length > 20) {
        ToastUtils.showLong(text)
    } else {
        ToastUtils.showShort(text)
    }
    appListenerFile.appendText("\n->bug \n$text")
}