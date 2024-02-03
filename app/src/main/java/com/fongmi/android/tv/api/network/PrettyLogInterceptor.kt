package com.fongmi.android.tv.api.network

import okhttp3.Interceptor
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.closeQuietly
import okio.Buffer
import java.nio.charset.StandardCharsets

/**
 * @author yangyoujun
 * Http请求日志聚合。
 * 聚合后的日志内容会通过log 函数传出，需要在调用处进行打印
 * 仅debug会添加，辅助接口日志调试
 */
class PrettyLogInterceptor(private val log: ((String) -> Unit)?) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (log == null) {
            return chain.proceed(chain.request())
        }

        val startTs = System.currentTimeMillis()

        var request = chain.request()
        var requestJson: String? = null

        request.body?.let {
            val buffer = Buffer()
            it.writeTo(buffer)

            requestJson = buffer.copy().readString(StandardCharsets.UTF_8)

            request = requestJson?.run {
                request.newBuilder()
                    .method(request.method, this.toRequestBody(it.contentType()))
                    .build()
            } ?: request.newBuilder().method(
                request.method,
                buffer.copy().readByteArray().toRequestBody(it.contentType())
            ).build()

            buffer.closeQuietly()
        }

        val response = chain.proceed(request)

        val elapsedTs = System.currentTimeMillis() - startTs

        val prettyLog = StringBuilder()
        // 1. basic info
        prettyLog.append(" \n").append(request.url)
            .appendLine()

        prettyLog.append(response.code).append("  ").append(request.method) // POST 200
            .append("  ${elapsedTs}ms  ").append(response.protocol) // 100ms h2
            .append("  ").append(response.handshake?.tlsVersion) // TLS

        if (response.message.isNotEmpty()) {
            prettyLog.append('\t').append(response.message)
        }

        prettyLog.appendLine()

        // 2.1 request - header
        prettyLog.appendLine().append("Request header: -->").appendLine()
        request.headers.forEach { header ->
            prettyLog.append("\t").append(header.first).append(": ")
                .append(header.second.run {
                    if (header.first == "extension-info") jsonFormat(this, 1) else this
                })
                .appendLine()
        }

        // 2.2 request - body
        if (requestJson?.isNotEmpty() == true) {
            prettyLog.append("Request body: ")
                .append(jsonFormat(requestJson!!))
                .appendLine()
        }

        prettyLog.appendLine() // 分割

        // 3.1 response - header
        prettyLog.append("Response header: <--").appendLine()
        response.headers.forEach { header ->
            prettyLog.append("\t").append(header.first).append(": ").append(header.second)
                .appendLine()
        }

        // 3.2 response - body
        val responseJson: String? = response.body?.string()
        if (responseJson?.isNotEmpty() == true) {
            prettyLog.append("Response body: ")
                .append(jsonFormat(responseJson))
                .appendLine()
        }

        prettyLog.appendLine() // 分割

        log.invoke(prettyLog.toString())

        return response.newBuilder().body(responseJson?.toResponseBody()).build()
    }

    /**
     * JSON格式化输出
     * @param json
     * @return
     */
    private fun jsonFormat(json: String, fromLevel: Int = 0): String {
        var level = 0

        // 判断当前是否在字符串中，如果在字符中，则不进入换行格式化
        var inStr = false

        //存放格式化的json字符串
        val jsonForMatStr = StringBuilder()

        for (c in json)//将字符串中的字符逐个按行输出, 获取s中的每个字符
        {

            //level大于0并且jsonForMatStr中的最后一个字符为\n,jsonForMatStr加入\t
            if (level > 0 && '\n' == jsonForMatStr[jsonForMatStr.length - 1]) {
                jsonForMatStr.append(getLevelStr(level + fromLevel))
            }
            //遇到"{"和"["要增加空格和换行，遇到"}"和"]"要减少空格，以对应，遇到","要换行

            when (c) {
                '"' -> jsonForMatStr.run {
                    append(c)
                    inStr = !inStr
                }

                '{', '[' -> {
                    jsonForMatStr.append(c).appendLine()
                    level++
                }

                ',' -> jsonForMatStr.append(c).takeIf { !inStr }?.appendLine()

                '}', ']' -> jsonForMatStr.run {
                    appendLine()
                    level--
                    append(getLevelStr(level + fromLevel))
                    append(c)
                }

                else -> jsonForMatStr.append(c)
            }
        }
        return jsonForMatStr.toString()
    }

    /**
     * 日志等级替换成制表符
     * @param level
     * @return
     */
    private fun getLevelStr(level: Int): String {
        val levelStr = StringBuilder()
        for (i in 0 until level) {
            levelStr.append("\t")
        }
        return levelStr.toString()
    }
}