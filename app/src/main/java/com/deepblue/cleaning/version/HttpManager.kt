package com.deepblue.cleaning.version

import android.text.TextUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.IOException

class HttpManager {

    interface Listener {
        fun onSuccess(response: String)
        fun onFailure(e: Exception?)
    }

    private val client = OkHttpClient()

    companion object {
        private var httpManager: HttpManager? = null

        @Synchronized
        fun getInstance(): HttpManager {
            if (httpManager == null) {
                httpManager = HttpManager()
            }
            return httpManager!!
        }
    }

    fun get(url: String, token: String?, listener: Listener) {
        doAsync {
            try {
                val result = get(url, token)
                uiThread {
                    getResult(result, listener)
                }
            } catch (e: Exception) {
                uiThread {
                    listener.onFailure(e)
                }
            }
        }
    }

    fun post(url: String, body : RequestBody, token: String?, listener: Listener) {
        doAsync {
            try {
                val result = post(url, body, token)
                uiThread {
                    getResult(result, listener)
                }
            } catch (e: Exception) {
                uiThread {
                    listener.onFailure(e)
                }
            }
        }
    }

    private fun getResult(result: String?, listener: Listener) {
        if (TextUtils.isEmpty(result)) {
            listener.onFailure(NullPointerException())
            return
        }
//        KLog.d("HttpManager", "result: $result")
//        KLog.json("HttpManager", result)
        listener.onSuccess(result!!)
    }

    @Throws(IOException::class)
    fun get(url: String, token: String?): String? {
        val builder = Request.Builder()
            .url(url)
        return newCall(builder, token)
    }

    @Throws(IOException::class)
    fun post(url: String, body : RequestBody, token: String?): String? {
//        KLog.d("HttpManager", "url: $url")
//        KLog.d("HttpManager", "token: $token")
        val builder = Request.Builder()
            .url(url)
            .post(body)
        return newCall(builder, token)
    }

    @Throws(IOException::class)
    private fun newCall(builder: Request.Builder, token: String?): String? {
        if (!TextUtils.isEmpty(token)) {
            builder.addHeader("token", token!!)
        }
        val request = builder.build()
        client.newCall(request).execute().use { response -> return response.body?.string() }
    }
}