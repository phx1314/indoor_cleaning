package com.deepblue.cleaning.version

/**
 * 响应报文
 */
open class Response: Request() {

    companion object {
        const val SUCCESS_CODE = "1111"
    }

    var code = ""
    var msg = ""
    var data = Any()
}