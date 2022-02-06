package com.deepblue.cleaning.websocket

import android.text.TextUtils
import android.util.Log
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.activity.ShutdownActivity
import com.deepblue.cleaning.cleanview.BToast
import com.deepblue.library.planbmsg.JsonUtils
import com.mdx.framework.Frame
import okhttp3.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.runOnUiThread
import java.util.concurrent.TimeUnit

class WebSocketClient3(url: String) {
    private var mWebSocket: WebSocket? = null
    private var client: OkHttpClient? = null

    @JvmField
    var host: String = ""
    private var socketMessageCallback: SocketMessageCallback? = null

    var connectStatus = -1

    init {
        this.host = url
    }

    private fun connect(url: String) {
        try {
            if (connectStatus != CONNECT_CONNECTING) {
                connectStatus = CONNECT_CONNECTING
                host = url
                val request = Request.Builder().url(url).build()
                client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
                val webSocketListener = object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        super.onOpen(webSocket, response)
                        connectStatus = CONNECT_SUCCESS
                        socketMessageCallback?.onSocketStatus(CONNECT_SUCCESS)
                    }

                    override fun onFailure(
                        webSocket: WebSocket,
                        t: Throwable,
                        response: Response?
                    ) {
                        super.onFailure(webSocket, t, response)
                        t.printStackTrace()
                        Log.e("web", "onFailure")
                        if (connectStatus != CONNECT_ERROR) {
                            connectStatus = CONNECT_ERROR
                            socketMessageCallback?.onSocketStatus(CONNECT_ERROR)
                        }
//                        mWebSocket?.close(1000, "null")
//                        mWebSocket = null
                    }


                    override fun onMessage(webSocket: WebSocket, text: String) {
                        Log.e("web", "back" + text)
                        if (text.isNotEmpty() && socketMessageCallback != null) {
                            Frame.CONTEXT.runOnUiThread {
                                try {
                                    val res = JsonUtils.fromJson(text, com.deepblue.library.planbmsg.Response::class.java)
                                    Log.w(res?.type.toString(), res?.json.toString())
                                    res?.let {
//                                        Frame.HANDLES.sentAll(it.type, text)
                                        if (Frame.HANDLES.HANDLES.size > 0) Frame.HANDLES.sentAll(Frame.HANDLES.HANDLES[Frame.HANDLES.HANDLES.size - 1].id, it.type, text)
                                    }
                                    socketMessageCallback?.onMessage(text)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                            }
                        }
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        super.onClosed(webSocket, code, reason)
                        Log.e("web", "onClosed")
                        if (connectStatus != CONNECT_CLOSE) {
                            connectStatus = CONNECT_CLOSE
                            socketMessageCallback?.onSocketStatus(CONNECT_CLOSE)
                        }
                        mWebSocket?.close(1000, "null")
                        mWebSocket = null
                    }

                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                        super.onClosing(webSocket, code, reason)
                        if (connectStatus != CONNECT_CLOSE) {
                            connectStatus = CONNECT_CLOSE
                            socketMessageCallback?.onSocketStatus(CONNECT_CLOSE)
                        }
                    }
                }
                client?.dispatcher?.cancelAll()
                mWebSocket = client?.newWebSocket(request, webSocketListener)
            }
        } catch (e: Exception) {
            connectStatus = -1
            e.printStackTrace()
        }
    }

    fun sendMessage(message: String) {
        if (mWebSocket != null && connectStatus == CONNECT_SUCCESS) {
            Log.e("web", "send$message")
            mWebSocket?.send(message)
        } else {
            if (connectStatus != CONNECT_CONNECTING) {
                Log.e("web", "websocket重连中...")
                connect(host)
            }
        }
    }


    fun isConnected(): Boolean {
        return connectStatus == CONNECT_SUCCESS
    }


    fun destroy() {
        mWebSocket?.close(1000, "null")
        mWebSocket = null
        webSocketClient?.connectStatus = -1
        webSocketClient = null
        client = null
        host = ""
    }

    fun setSocketCallBack(socketMessageCallback: SocketMessageCallback?) {
        webSocketClient?.socketMessageCallback = socketMessageCallback
    }

    fun sendMessage(message: String, socketMessageCallback: SocketMessageCallback?) {//该方法可以解决a页面调用b页面接口刷新界面的场景，但是无限循环的接口需要单独处理
        webSocketClient?.socketMessageCallback = socketMessageCallback
        if (mWebSocket != null && connectStatus == CONNECT_SUCCESS) {
            Log.e("web", "send$message")
            mWebSocket?.send(message)
        } else {
            if (connectStatus != CONNECT_CONNECTING) {
                Log.e("web", "websocket重连中...")
                connect(host)
            }
        }
    }

    companion object {

        private var webSocketClient: WebSocketClient3? = null
        const val CONNECT_CONNECTING = 0//正在连接
        const val CONNECT_SUCCESS = 1//连接成功
        const val CONNECT_ERROR = 2//连接失败
        const val CONNECT_CLOSE = 3//连接断开

        @Synchronized
        @JvmStatic
        fun getInstance(url: String): WebSocketClient3 {
            if (webSocketClient == null) {
                webSocketClient = WebSocketClient3(url)
            }
            return webSocketClient!!
        }

    }
}
