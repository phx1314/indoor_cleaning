package com.deepblue.cleaning.req

import com.deepblue.library.planbmsg.Request

class DeleteTaskReportReq(start_time: List<String>) : Request(4012, "删除任务报告") {

    init {
        json = Data(start_time)
    }

    /**
     * 数据区
     * @param io_states 控制端口列表
     */
    class Data(val start_time: List<String>)
}