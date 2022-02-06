package com.deepblue.cleaning

import android.media.MediaPlayer
import com.deepblue.library.planbmsg.bean.Map
import com.deepblue.library.planbmsg.bean.MapPoint
import com.deepblue.library.planbmsg.bean.RobotLoc
import com.deepblue.library.planbmsg.bean.UserInfo
import com.mdx.framework.Frame

object Const {

    enum class INDOOR_MODEL {
        stone_model, common_model
    }

    private const val URL_CLOUD_PRODUCT = "https://robot.deepblueai.com/robotapi/robotos"
    const val URL_CLOUD = URL_CLOUD_PRODUCT

    const val SHARED_FILE: String = "indoor_sp"
    const val SP_MODE: String = "sp_mode"
    const val SP_USER: String = "sp_user"
    const val SP_NO_PROMPT_DATE: String = "sp_no_prompt_date"
    const val SP_NEW_ERRORINFO: String = "sp_new_errorinfo"

    //机器人有故障
    @JvmField
    var systemError: Boolean = false

    //机器人时间
    @JvmField
    var systemTime: Long = 0

    //机器人是否联网
    @JvmField
    var system4G: Boolean = false

    //机器人电量
    @JvmField
    var systemPower: Int = 0

    //机器人状态
    @JvmField
    var robotPlayStatus = 0

    //机器人自动手动
    var robotStatus = "auto"

    //机器人当前位姿
    @JvmField
    var robotLoc: RobotLoc? = null

    //机器人类型
    @JvmField
    var type = "2" //10是大洗地机  2小浣熊

    var fieldModel = INDOOR_MODEL.common_model.toString()

    var user: UserInfo? = null

    var map: Map? = null
    var newMap: Map? = null
    var min_battery_level_to_back = if (type == "2") 10 else 20
    var cleanMode = "auto"
    var isDdXs = true

    val scan_points = ArrayList<MapPoint>()


}