package com.deepblue.cleaning.req

import com.deepblue.library.planbmsg.Request

class SetRobotVoiceReq(volume: Int) : Request(2032, "设置机器人音量") {

    init {
        json = Data(volume)
    }

    /**
     * 数据区
     * @param io_states 控制端口列表
     */
    class Data(val volume: Int)
}