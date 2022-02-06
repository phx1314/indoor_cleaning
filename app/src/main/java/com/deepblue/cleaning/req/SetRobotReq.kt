package com.deepblue.cleaning.req

import com.deepblue.library.planbmsg.Request

class SetRobotReq(io_states: ArrayList<KeyState>) : Request(2030, "设置机器人信息-通用") {

    init {
        json = Data(io_states)
    }

    /**
     * 数据区
     * @param io_states 控制端口列表
     */
    class Data(val key_value: ArrayList<KeyState>)
}