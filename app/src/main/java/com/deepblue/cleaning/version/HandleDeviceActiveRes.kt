package com.deepblue.cleaning.version

import com.deepblue.library.planbmsg.JsonUtils

/**
 * 设备激活
 */
class HandleDeviceActiveRes: Response() {

    init {
        data = Device()
    }

    fun getData(): Device? {
        return JsonUtils.fromJson(data.toString(), Device::class.java)
    }
}