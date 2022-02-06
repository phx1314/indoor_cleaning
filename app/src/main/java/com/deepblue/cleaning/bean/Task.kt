package com.deepblue.cleaning.bean

import com.deepblue.library.planbmsg.bean.MapPoint
import com.deepblue.library.planbmsg.bean.MapRange

class Task {
    var range : MapRange? = null
    var checked: Boolean = false
    var donePar = 0
    var travelPoints : ArrayList<MapPoint>? = null
}