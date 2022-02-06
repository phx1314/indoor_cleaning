package com.deepblue.cleaning.req

import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.Response


class GetRobotRes : Response() {

    init {
//        json = Data()
    }

    fun getJson(): Array<KeyState>? {
        return if (json != null) JsonUtils.fromJson(json.toString(), Array<KeyState>::class.java) else null
    }

//    class Data {
//        var key_value = ArrayList<KeyState>()
//
//
//    }
}