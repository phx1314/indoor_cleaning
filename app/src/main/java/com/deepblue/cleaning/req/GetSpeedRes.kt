package com.deepblue.cleaning.req

import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.Response
import com.deepblue.library.planbmsg.bean.RobotBattery


class GetSpeedRes : Response() {

    init {
        json = RobotSpeed()
    }

    fun getJson(): RobotSpeed? {
        return JsonUtils.fromJson(json.toString(), RobotSpeed::class.java)
    }
}