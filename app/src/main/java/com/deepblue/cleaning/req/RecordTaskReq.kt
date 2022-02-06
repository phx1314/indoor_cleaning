package com.deepblue.cleaning.req

import com.deepblue.library.planbmsg.Request

class RecordTaskReq : Request(4008, "录制任务") {//record_mode 1路径 2区域

    companion object {
        const val START = 1
        const val STOP = 2
    }

    fun start(name: String, map_id: Int, record_mode: Int): String {
        number = START
        json = Data("start", name, map_id, record_mode)
        return toString()
    }

    fun stop(name: String, map_id: Int, record_mode: Int): String {
        number = STOP
        json = Data("stop", name, map_id, record_mode)
        return toString()
    }


    /**
     * 数据区
     * @param operate start-开始,stop-结束
     * @param name 任务名称
     */
    class Data(val operate: String, val name: String? = null, val map_id: Int? = null, val record_mode: Int = 1)
}