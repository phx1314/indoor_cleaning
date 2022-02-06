package com.deepblue.cleaning.req

import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.Response


class GetRobotDeviceRes : Response() {

    init {
//        json = Data()
    }

    fun getJson(): Data? {
        return if (json != null) JsonUtils.fromJson(json.toString(), Data::class.java) else null
    }

    class Data {
        var hardwares_status = ArrayList<DeviceState>()


    }
}