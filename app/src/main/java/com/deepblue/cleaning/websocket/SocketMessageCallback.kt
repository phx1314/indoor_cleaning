package com.deepblue.cleaning.websocket

interface SocketMessageCallback {

    fun onMessage(message: String): Int

    fun onSocketStatus(status: Int)
}