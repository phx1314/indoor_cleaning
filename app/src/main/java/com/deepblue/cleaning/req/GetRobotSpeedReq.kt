package com.deepblue.cleaning.req

import com.deepblue.library.planbmsg.Request

class GetRobotSpeedReq(operate: String) : Request(1011, "查询速度数据") {

    init {
        json = Data(operate)
    }

    /**
     * 数据区
     * @param io_states 控制端口列表
     */
    class Data(val operate: String)
}