package com.deepblue.cleaning.req

import com.deepblue.library.planbmsg.Request

class GetRobotReq(key: List<String>) : Request(2031, "获取机器人信息-通用") {

    init {
        json = Data(key)
    }

    /**
     * 数据区
     * @param io_states 控制端口列表
     */
    class Data(val key: List<String>)
}