package li.songe.gkd.a11y.utils

import android.text.format.DateUtils
import com.blankj.utilcode.util.Utils
import java.io.File

fun File.autoMk(): File {
    if (!exists()) {
        mkdirs()
    }
    return this
}

fun File.autoCreate(): File {
    if (!exists()) {
        createNewFile()
    }
    return this
}

fun File.appendTime() {
    appendText(
        "\ntime->${
            System.currentTimeMillis().format("yyyy-MM-dd HH:mm:ss")
        }\n"
    )
    appScope.launchTry {
        checkUpload()
    }
}

private val filesDir: File by lazy {
    Utils.getApp().getExternalFilesDir(null) ?: error("failed getExternalFilesDir")
}

val dbFolder: File
    get() = filesDir.resolve("db").autoMk()
val shFolder: File
    get() = filesDir.resolve("sh").autoMk()
val storeFolder: File
    get() = filesDir.resolve("store").autoMk()
val subsFolder: File
    get() = filesDir.resolve("subscription").autoMk()
val snapshotFolder: File
    get() = filesDir.resolve("snapshot").autoMk()

val privateStoreFolder: File
    get() = Utils.getApp().filesDir.resolve("store").autoMk()

private val cacheDir by lazy { Utils.getApp().externalCacheDir ?: Utils.getApp().cacheDir }
val coilCacheDir: File
    get() = cacheDir.resolve("coil").autoMk()
val sharedDir: File
    get() = cacheDir.resolve("shared").autoMk()
private val tempDir: File
    get() = cacheDir.resolve("temp").autoMk()
private val listenerDir: File
    get() = filesDir.resolve("listener").autoMk()
val appUseFile: File
    get() = listenerDir.resolve("appUse.txt").autoCreate()
val appListenerFile: File
    get() = listenerDir.resolve("appListener.txt")
object Folder {
    var uploader: ((String)->Unit)? = null
}

fun createTempDir(): File {
    return tempDir
        .resolve(System.currentTimeMillis().toString())
        .apply { mkdirs() }
}

private fun removeExpired(dir: File) {
    dir.listFiles()?.forEach { f ->
        if (System.currentTimeMillis() - f.lastModified() > DateUtils.HOUR_IN_MILLIS) {
            if (f.isDirectory) {
                f.deleteRecursively()
            } else if (f.isFile) {
                f.delete()
            }
        }
    }
}

suspend fun checkUpload() {
    listenerDir.listFiles()?.map {
        if (it.length() > 1024*2 || (System.currentTimeMillis() - it.lastModified() > DateUtils.DAY_IN_MILLIS)) {
            Folder.uploader?.invoke(it.readText())
            it.delete()
        }
    }
}

fun clearCache() {
    removeExpired(sharedDir)
    removeExpired(tempDir)
}