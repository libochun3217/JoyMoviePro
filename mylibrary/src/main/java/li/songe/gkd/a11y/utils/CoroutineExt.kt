package li.songe.gkd.a11y.utils

import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun CoroutineScope.launchTry(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    silent: Boolean = false,
    block: suspend CoroutineScope.() -> Unit,
) = launch(context, start) {
    try {
        block()
    } catch (e: CancellationException) {
        e.printStackTrace()
    } catch (e: Throwable) {
        e.printStackTrace()
        LogUtils.d(e)
        if (!silent) {
            toast(e.message ?: e.stackTraceToString())
        }
    }
}