package com.deepblue.cleaning.version

import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.annotation.JSONField
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.net.URLEncoder

/**
 * 请求报文
 */
open class Request {

    //接口路径
    @JSONField(serialize = false)
    var path = ""

    fun toRequestBody(): RequestBody {
        val builder = FormBody.Builder()
        val json = JSON.toJSONString(this)
        val result = JSONObject(json)
        val it: Iterator<String> = result.keys()
        while (it.hasNext()) { //遍历JSONObject
            val key = it.next()
            if (TextUtils.isEmpty(key)) {
                continue
            }
            val value = result.getString(key)
            if (TextUtils.isEmpty(value)) {
                continue
            }
//            KLog.d("toRequestBody", "$key = $value")
            builder.add(key, value)
        }
        return builder.build()
    }

    override fun toString(): String {
        return JSON.toJSONString(this)
    }
}