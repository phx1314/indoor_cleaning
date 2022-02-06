package com.deepblue.cleaning.version


/**
 * 设备激活
 */
class HandleDeviceActiveReq(val deviceMac: String="123456", val versionNum: String = "V1.0") : Request() {

    init {
        path = "/robotCommunication/handleDeviceActive"
    }
}