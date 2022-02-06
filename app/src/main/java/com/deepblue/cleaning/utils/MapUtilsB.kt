package com.deepblue.cleaning.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.util.SparseArray
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.deepblue.cleaning.R
import com.deepblue.cleaning.activity.BaseActivity
import com.deepblue.cleaning.cleanview.TransformativeImageView2
import com.deepblue.library.planbmsg.bean.*
import com.deepblue.library.planbmsg.bean.Map
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.ref.SoftReference
import java.text.NumberFormat
import kotlin.math.max
import kotlin.math.sqrt


object MapUtilsB {
    private val hmBitmap = SparseArray<Bitmap>()
    private val InDistance = 2.0
    private val Has_Mask = true
    private val BitmapConfig = Bitmap.Config.RGB_565
    private val Flags = Base64.NO_WRAP
    private const val TextOffsetX1 = 5
    private const val TextOffsetY1 = 30
    private const val LineWidth1 = 5F
    private const val PathWidth1 = 2F
    private const val LaserWidth1 = 5F
    private const val RulerWidth1 = 10F
    private const val TextSize1 = 30F
    private const val CircleRadius1 = 18F
    private const val ForbiddenLineWidth = 3F

    private const val MAX_SCALE = 0.2F

    private fun getTextOffsetX(scale: Float): Float {
        return TextOffsetX1 / scale
    }

    private fun getTextOffsetY(scale: Float): Float {
        return TextOffsetY1 / scale
    }

    private fun getLineWidth(scale: Float): Float {
        return LineWidth1 / scale
    }

    private fun getPathWidth(scale: Float): Float {
        return PathWidth1 / scale
    }

    private fun getLaserWidth(scale: Float): Float {
        return LaserWidth1 / scale
    }

    private fun getRulerWidth(scale: Float): Float {
        return RulerWidth1 / scale
    }

    private fun getTextSize(scale: Float): Float {
        return max(TextSize1 / scale, TextSize1)
    }

    private fun getCircleRadius(scale: Float): Float {
        return CircleRadius1 / scale
    }

    private fun getOffset(scale: Float, value: Int): Int {
        return max(1F, value / scale).toInt()
    }

    fun distance(point1: MapPoint, point2: MapPoint): Double {
        val dx = point2.x - point1.x
        val dy = point2.y - point1.y
        return sqrt(dx * dx + dy * dy)
    }

    fun isInDistance(
        scale: Float,
        point1: MapPoint,
        point2: MapPoint,
        distance: Double = InDistance
    ): Boolean {
        val d = distance(point1, point2)
        return d <= distance / scale
    }

    /**
     * 地图坐标转Canvas坐标
     */
    private fun mapToCanvas(bitmap: Bitmap, map: Map, mapPoint: MapPoint): MapPoint {

        val minPoint = map.map_info.min_pos
        val maxPoint = map.map_info.max_pos
        val w = bitmap.width
        val h = bitmap.height

        val pos = MapPoint()
        pos.x = w * (mapPoint.x - minPoint.x) / (maxPoint.x - minPoint.x)
        pos.y = h * (maxPoint.y - mapPoint.y) / (maxPoint.y - minPoint.y)
        return pos
    }

    private fun mapLaserCanvas(bitmap: Bitmap, mapPoint: MapPoint): MapPoint {

        val w = bitmap.width
        val h = bitmap.height

        val pos = MapPoint()
        pos.x = w * (mapPoint.x) / 600
        pos.y = h * (600 - mapPoint.y) / 600
        return pos
    }

    /**
     * 屏幕点击坐标转Canvas坐标
     */
    fun pointerToCanvas(point: MapPoint, imageView: ImageView): MapPoint {
        val imageMatrix = imageView.imageMatrix

        // 获取触摸点的坐标 x, y
        val x = point.x.toFloat()
        val y = point.y.toFloat()
        // 目标点的坐标
        val dst = FloatArray(2)
        // 获取到ImageView的matrix
        // 创建一个逆矩阵
        val inverseMatrix = Matrix()
        // 求逆，逆矩阵被赋值
        imageMatrix.invert(inverseMatrix)
        // 通过逆矩阵映射得到目标点 dst 的值
        inverseMatrix.mapPoints(dst, floatArrayOf(x, y))

        val pos = MapPoint()
        pos.x = dst[0].toDouble()
        pos.y = dst[1].toDouble()
        return pos
    }


    fun pToCanvas(imageView: TransformativeImageView2, point: MapPoint): MapPoint {
        val imageMatrix = imageView.imageMatrix

        // 获取触摸点的坐标 x, y
        val x = point.x.toFloat()
        val y = point.y.toFloat()
        // 目标点的坐标
        val dst = FloatArray(2)
        // 获取到ImageView的matrix
        // 创建一个逆矩阵
        val inverseMatrix = Matrix()
        // 求逆，逆矩阵被赋值
        imageMatrix.invert(inverseMatrix)
        // 通过逆矩阵映射得到目标点 dst 的值
        inverseMatrix.mapPoints(dst, floatArrayOf(x, y))

        val pos = MapPoint()
        pos.x = dst[0].toDouble()
        pos.y = dst[1].toDouble()
        return pos
    }


    fun bitmapToMap(point: MapPoint, imageView: ImageView, map: Map): WayPoint? {
        val dstX = point.x
        val dstY = point.y
        // 获取图片的大小
        val drawWidth = imageView.drawable.bounds.width()
        val drawHeight = imageView.drawable.bounds.height()
        val deviceX = dstX / drawWidth
        val deviceY = dstY / drawHeight
        // 判断dstX, dstY在Bitmap上的位置即可
        if (!(deviceX < 0 || deviceX > 1 || deviceY < 0 || deviceY > 1)) {

            val pos = WayPoint()
            pos.x =
                map.map_info.min_pos.x + (map.map_info.max_pos.x - map.map_info.min_pos.x) * deviceX
            pos.y =
                map.map_info.max_pos.y - (map.map_info.max_pos.y - map.map_info.min_pos.y) * deviceY
            pos.angle = 0.0
            pos.map_id = map.map_info.map_id
            pos.real = 1
            return pos
        }
        return null
    }

    /**
     * 屏幕点击坐标转地图坐标
     */
    fun pointerToMap(point: MapPoint, imageView: ImageView, map: Map): MapPoint? {

        if (imageView.drawable == null) {
            return null
        }

        val posCanvas = pointerToCanvas(point, imageView)

        val dstX = posCanvas.x
        val dstY = posCanvas.y
        // 获取图片的大小
        val drawWidth = imageView.drawable.bounds.width()
        val drawHeight = imageView.drawable.bounds.height()
        val deviceX = dstX / drawWidth
        val deviceY = dstY / drawHeight
        // 判断dstX, dstY在Bitmap上的位置即可
        if (!(deviceX < 0 || deviceX > 1 || deviceY < 0 || deviceY > 1)) {
            val pos = MapPoint()
            pos.x =
                map.map_info.min_pos.x + (map.map_info.max_pos.x - map.map_info.min_pos.x) * deviceX
            pos.y =
                map.map_info.max_pos.y - (map.map_info.max_pos.y - map.map_info.min_pos.y) * deviceY
            return pos
        }
        return null
    }

    fun pointerToMap(pointF: PointF, imageView: ImageView, map: Map): MapPoint? {
        val point = MapPoint()
        point.x = pointF.x.toDouble()
        point.y = pointF.y.toDouble()
        return pointerToMap(point, imageView, map)
    }

    private fun byteToBitmap(imgByte: ByteArray, sampleSize: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inSampleSize = sampleSize
        val input = ByteArrayInputStream(imgByte)
        val softRef = SoftReference(
            BitmapFactory.decodeStream(
                input, null, options
            )
        )
        val bitmap = softRef.get()
        try {
            input.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bitmap
    }

    fun mapToBitmap(map: Map, width: Int = 0, height: Int = 0, isRound: Boolean = false): Bitmap? {
        val newBitmap: Bitmap
        try {
            val bitmapArray = Base64.decode(map.map_info.picture, Flags)
            val distanceWidth = map.map_info.max_pos.x - map.map_info.min_pos.x
            val distanceHeight = map.map_info.max_pos.y - map.map_info.min_pos.y
            if (map.resolution == 0.0) {
                map.resolution = map.map_info.resolution
            }
            val distance = max(distanceWidth, distanceHeight) / map.resolution
            val bitmap: Bitmap
            bitmap = if (distance < 1024) {
                BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.size)
            } else {
                val sampleSize: Int = (distance / 1024).toInt()
                byteToBitmap(bitmapArray, sampleSize) ?: return null
            }

            if (width > 0 && height > 0) {
                val scaleWidth = width.toFloat() / bitmap.width
                val scaleHeight = height.toFloat() / bitmap.height
                //                m.postScale(scaleWidth, -scaleHeight)
                val m = Matrix()
                m.setScale(scaleWidth, scaleHeight)
                try {
                    newBitmap =
                        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
                } catch (e: Exception) {
                    return null
                }
            } else {
                //                m.postScale(1f, -1f)
                newBitmap = bitmap.copy(BitmapConfig, true)
            }

//            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)

            if (Has_Mask) {
                val canvas = Canvas(newBitmap)

                val paint = Paint(Paint.ANTI_ALIAS_FLAG)//消除锯
                paint.style = Paint.Style.FILL_AND_STROKE
//                paint.setAlpha(0x40); //设置透明程度
                paint.color = Color.parseColor("#00000000")
//                if (isRound)
//                    canvas.drawRoundRect(
//                        0f,
//                        0f,
//                        newBitmap.width.toFloat(),
//                        newBitmap.height.toFloat(), 20f, 20f,
//                        paint
//                    ) else
                canvas.drawRect(
                    0f,
                    0f,
                    newBitmap.width.toFloat(),
                    newBitmap.height.toFloat(),
                    paint
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return newBitmap
    }

    fun mapToSmallBitmap(map: Map, width: Int = 0, height: Int = 0): Bitmap? {
        val newBitmap: Bitmap
        try {
            val bitmapArray = Base64.decode(map.map_info.picture, Flags)
            val distanceWidth = map.map_info.max_pos.x - map.map_info.min_pos.x
            val distanceHeight = map.map_info.max_pos.y - map.map_info.min_pos.y
            if (map.resolution == 0.0) {
                map.resolution = map.map_info.resolution
            }
            val distance = max(distanceWidth, distanceHeight) / map.resolution
            val bitmap: Bitmap
            bitmap = if (distance < 480) {
                BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.size)
            } else {
                val sampleSize: Int = (distance / 480).toInt()
                byteToBitmap(bitmapArray, sampleSize) ?: return null
            }

            if (width > 0 && height > 0) {
                val scaleWidth = width.toFloat() / bitmap.width
                val scaleHeight = height.toFloat() / bitmap.height
                //                m.postScale(scaleWidth, -scaleHeight)
                val m = Matrix()
                m.setScale(scaleWidth, scaleHeight)
                try {
                    newBitmap =
                        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
                } catch (e: Exception) {
                    return null
                }
            } else {
                //                m.postScale(1f, -1f)
                newBitmap = bitmap.copy(BitmapConfig, true)
            }

//            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)

            if (Has_Mask) {
                val canvas = Canvas(newBitmap)

                val paint = Paint(Paint.ANTI_ALIAS_FLAG)//消除锯
                paint.style = Paint.Style.FILL_AND_STROKE
//                paint.setAlpha(0x40); //设置透明程度
                paint.color = Color.parseColor("#00000000")
//                if (isRound)
//                    canvas.drawRoundRect(
//                        0f,
//                        0f,
//                        newBitmap.width.toFloat(),
//                        newBitmap.height.toFloat(), 20f, 20f,
//                        paint
//                    ) else
                canvas.drawRect(
                    0f,
                    0f,
                    newBitmap.width.toFloat(),
                    newBitmap.height.toFloat(),
                    paint
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return newBitmap
    }

    fun mapToBitmap2(map: Map, width: Int = 0, height: Int = 0, isRound: Boolean = false): Bitmap? {
        val newBitmap: Bitmap
        try {
            val bitmapArray = Base64.decode(map.map_info.picture, Flags)
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.size)
            if (width > 0 && height > 0) {
                val scaleWidth = width.toFloat() / bitmap.width
                val scaleHeight = height.toFloat() / bitmap.height
                //                m.postScale(scaleWidth, -scaleHeight)
                val m = Matrix()
                m.setScale(scaleWidth, scaleHeight)
                try {
                    newBitmap =
                        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
                } catch (e: Exception) {
                    return null
                }
            } else {
                //                m.postScale(1f, -1f)
                newBitmap = bitmap.copy(BitmapConfig, true)
            }

//            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)

            if (Has_Mask) {
                val canvas = Canvas(newBitmap)

                val paint = Paint(Paint.ANTI_ALIAS_FLAG)//消除锯
                paint.style = Paint.Style.FILL_AND_STROKE
//                paint.setAlpha(0x40); //设置透明程度
                paint.color = Color.parseColor("#00000000")
//                if (isRound)
//                    canvas.drawRoundRect(
//                        0f,
//                        0f,
//                        newBitmap.width.toFloat(),
//                        newBitmap.height.toFloat(), 20f, 20f,
//                        paint
//                    ) else
                canvas.drawRect(
                    0f,
                    0f,
                    newBitmap.width.toFloat(),
                    newBitmap.height.toFloat(),
                    paint
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return newBitmap
    }

    fun getTransparentBitmap(sourceImg: Bitmap, number: Int): Bitmap? {
        var sourceImg = sourceImg
        var number = number
        val argb = IntArray(sourceImg.width * sourceImg.height)
        sourceImg.getPixels(
            argb,
            0,
            sourceImg.width,
            0,
            0,
            sourceImg.width,
            sourceImg.height
        ) // 获得图片的ARGB值
        number = number * 255 / 100
        for (i in argb.indices) {
            argb[i] = number shl 24 or (argb[i] and 0x00FFFFFF)
        }
        sourceImg =
            Bitmap.createBitmap(argb, sourceImg.width, sourceImg.height, Bitmap.Config.ARGB_8888)
        return sourceImg
    }

    fun rotateBitmap(origin: Bitmap?, alpha: Float): Bitmap? {
        if (origin == null) {
            return null
        }
        val width = origin.width
        val height = origin.height
        val matrix = Matrix()
        matrix.setRotate(
            alpha - 90, (origin.getWidth() / 2).toFloat(),
            (origin.getHeight() / 2).toFloat()
        );

        // 围绕原地进行旋转
        val newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false)
        if (newBM == origin) {
            return newBM
        }
        origin.recycle()
        return newBM
    }

//    fun getLaser(mContext: Context, robot: RobotLoc, lasers: ArrayList<MapPoint>?): Bitmap? {
//        var newBitmap: Bitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888);
//        val canvas = Canvas(newBitmap)
//        if (lasers != null && lasers.isNotEmpty()) {
//            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
//            paint.color = ResourcesUtils.getColor(mContext, R.color.forbidden_line)
//            paint.strokeCap = Paint.Cap.ROUND
//            paint.style = Paint.Style.STROKE
//            paint.strokeWidth = 3F
//            //激光
//            for (i in 0 until lasers.size) {
//                val bitmapPoint = lasers[i]
//                val xx = (bitmapPoint.x.toFloat() - robot.x.toFloat()) * 50 + 300
//                val yy = (bitmapPoint.y.toFloat() - robot.y.toFloat()) * 50 + 300
//                canvas.drawPoint(xx, 600 - yy, paint)
//            }
//        }
//        val ss = rotateBitmap(newBitmap, robot.angle.toFloat())
//
//        return Bitmap.createBitmap(ss, 0, 0, ss!!.width, ss.height / 2, null, false);

//    }


//    fun getRobotLaser(mContext: Context, robot: RobotLoc, lasers: ArrayList<MapPoint>?): Bitmap? {
//        var newBitmap: Bitmap = Bitmap.createBitmap(300, 200, Bitmap.Config.ARGB_8888);
//        val canvas = Canvas(newBitmap)
//
//        if (lasers != null && lasers.isNotEmpty()) {
//            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
//            paint.color = ResourcesUtils.getColor(mContext, R.color.forbidden_line)
//            paint.strokeCap = Paint.Cap.ROUND
//            paint.style = Paint.Style.STROKE
//            paint.strokeWidth = 2F
//            //激光
//            try {
//                for (i in 0 until lasers.size) {
//                    val bitmapPoint = lasers[i]
//                    val x0 =
//                        (bitmapPoint.x.toFloat() - robot.x.toFloat()) * java.lang.Math.cos(robot.angle) - (bitmapPoint.y.toFloat() - robot.y.toFloat()) * java.lang.Math.sin(
//                            robot.angle
//                        ).toFloat() + robot.x.toFloat()
//                    val y0 =
//                        (bitmapPoint.x.toFloat() - robot.x.toFloat()) * java.lang.Math.sin(robot.angle) + (bitmapPoint.y.toFloat() - robot.y.toFloat()) * java.lang.Math.cos(
//                            robot.angle
//                        ).toFloat() + robot.y.toFloat()
//
//                    canvas.drawPoint(
//                        (x0 - robot.x.toFloat()).toFloat() + 150,
//                        (y0 - robot.y.toFloat()).toFloat(), paint
//                    )
//                }
//            } catch (e: Exception) {
//            }
//        }
//        return newBitmap
//    }


    fun base64ToBitmap(string: String): Bitmap? {
        val newBitmap: Bitmap
        try {
            val bitmapArray = Base64.decode(string, Flags)
            val bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.size)
//            val m = Matrix()
//            m.postScale(1f, -1f)//上下翻转
//            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
            newBitmap = bitmap.copy(BitmapConfig, true)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return newBitmap
    }

//    fun bitmapToServer(bitmap: Bitmap): Bitmap? {
//        val newBitmap: Bitmap
//        try {
//            val m = Matrix()
//            m.postScale(1f, -1f)//上下翻转
//            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return null
//        }
//
//        return newBitmap
//    }

    fun bitmapToBase64(bitmap: Bitmap?): String? {

        var result: String? = null
        var baos: ByteArrayOutputStream? = null
        try {
            if (bitmap != null) {
                baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)

                baos.flush()
                baos.close()

                val bitmapBytes = baos.toByteArray()
                result = Base64.encodeToString(bitmapBytes, Flags)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (baos != null) {
                    baos.flush()
                    baos.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return result
    }

    fun mapAddRobot(
        mContext: Context,
        bitmap: Bitmap,
        map: Map,
        robot: RobotLoc?,
        lasers: ArrayList<MapPoint>? = null
    ): Bitmap {

        val newBitmap = bitmap.copy(BitmapConfig, true)
        val canvas = Canvas(newBitmap)

        if (lasers != null && lasers.isNotEmpty()) {
            //激光
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = Color.parseColor("#0647D6")
            paint.strokeCap = Paint.Cap.ROUND
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            try {
                for (i in 0 until lasers.size) {
                    val bitmapPoint = mapToCanvas(bitmap, map, lasers[i])
                    canvas.drawPoint(bitmapPoint.x.toFloat(), bitmapPoint.y.toFloat(), paint)
                }
            } catch (e: Exception) {
            }
        }

        //机器人
        if (robot != null) {
            var image =
                BitmapFactory.decodeResource(mContext.resources, R.drawable.gro_ic_robot)
//        val rotate = Math.toDegrees(robot.angle).toFloat()
            val rotate = robot!!.angle.toFloat()
            val matrix = Matrix()
            matrix.postRotate(90 - rotate)
            try {
                image = Bitmap.createBitmap(
                    image, 0, 0, image.width,
                    image.height, matrix, true
                )
            } catch (e: Exception) {
            }
            if (image != null) {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                val mapPoints = mapToCanvas(bitmap, map, robot)
                canvas.drawBitmap(
                    image,
                    (mapPoints.x - image.width / 2).toFloat(),
                    (mapPoints.y - image.width / 2).toFloat(),
                    paint
                )
            }
        }
        return newBitmap
    }

    private fun paintBrushes(
        scale: Float,
        canvas: Canvas,
        brushes: ArrayList<ArrayList<Map.Brush>>?
    ) {

        if (brushes == null || brushes.isEmpty()) {
            return
        }
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        for (i in 0 until brushes.size) {
            val brush = brushes[i]
            if (brush.isNotEmpty()) {
                if (brush[0].radius <= 0F) {
                    continue
                }
                val path = Path()
                path.moveTo(brush[0].x, brush[0].y)
                paint.color = brush[0].color
                if (scale > 0) {
                    paint.strokeWidth = brush[0].radius / scale
                } else {
                    paint.strokeWidth = brush[0].radius
                }
                for (j in 1 until brush.size) {
//                    if (scale > 0) {
//                        path.lineTo(brush[j].x / scale, brush[j].y / scale)
//                    } else {
//                        path.lineTo(brush[j].x, brush[j].y)
//                    }
                    path.lineTo(brush[j].x, brush[j].y)
                }
                canvas.drawPath(path, paint)
            }
        }
    }

    fun brushesToBitmap(
        bitmap: Bitmap,
        brushes: ArrayList<ArrayList<Map.Brush>>?,
        scale: Float
    ): Bitmap {
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(newBitmap)
        paintBrushes(scale, canvas, brushes)
        return newBitmap
    }

//    fun checkMapName(
//        activity: BaseActivity,
//        mapName: String,
//        maps: ArrayList<Map>,
//        showAlert: Boolean = true
//    ): Boolean {
//        var found = false
//        if (maps.isEmpty()) {
//            found = false
//        } else {
//            for (i in 0 until maps.size) {
//                if (mapName == maps[i].map_info.map_name) {
//                    found = true
//                    break
//                }
//            }
//        }
//        if (found && showAlert) {
//            activity.showAlert(
//                activity.getString(R.string.same_map_name_title),
//                activity.getString(R.string.same_map_name_message),
//                android.R.string.ok,
//                object : AlertDialog.DialogButtonListener {
//                    override fun cancel() {
//
//                    }
//
//                    override fun ensure(input: String?): Boolean {
//                        return true
//                    }
//                }
//            )
//        }
//        return found
//    }

    fun getChargePointName(map: Map?): Int {
        if (map?.points?.isEmpty() == true) {
            return 0
        }
        val wayPoints = map?.points ?: return 0
        for (i in 0 until wayPoints.size) {
            val wapPoint = wayPoints[i]
            val types = wapPoint.type
            if (WayPoint.Type_Charge in types) {
                return wapPoint.point_id
            }
        }
        return 0
    }

    fun findPointFromPoints(id: Int, points: ArrayList<WayPoint>): WayPoint? {
        if (points.isEmpty()) {
            return null
        }
        for (i in 0 until points.size) {
            val point = points[i]
            if (point.point_id == id) {
                return point
            }
        }
        return null
    }

    fun findRangePointFromPoints(id: Int, points: ArrayList<WayPoint>): RangePoint? {
        if (points.isEmpty()) {
            return null
        }
        for (i in 0 until points.size) {
            val point = points[i]
            if (point.point_id == id) {
                val rangePoint = RangePoint()
                rangePoint.point_id = point.point_id
                rangePoint.x = point.x
                rangePoint.y = point.y
                rangePoint.angle = point.angle
                rangePoint.coordinates_type = point.coordinates_type
                rangePoint.type = RangePoint.TYPE_UNMODIFY
                rangePoint.name = point.name
                return rangePoint
            }
        }
        return null
    }


    private fun drawNinepatch(context: Context, canvas: Canvas, id: Int, rect: Rect) {
        val bmp = BitmapFactory.decodeResource(context.resources, id)

        val patch = NinePatch(bmp, bmp.ninePatchChunk, null)
        patch.draw(canvas, rect)
    }


    /**
     * 绘制地图圆/椭圆
     */
    fun paintMapCircle(
        scale: Float,
        canvas: Canvas,
        bitmap: Bitmap,
        circle: MapRange,
        map: Map,
        paint: Paint,
        paintText: Paint
    ) {

        if (circle.point_info.isEmpty()) {
            return
        }

        val center = mapToCanvas(
            bitmap,
            map,
            circle.point_info[0]
        )

        when {
            circle.point_info.size == 2 -> {
                //圆
                val radius = mapToCanvas(
                    bitmap,
                    map,
                    circle.point_info[1]
                )
                val distance = distance(center, radius).toFloat()
                val path = Path()
                path.addCircle(
                    center.x.toFloat(),
                    center.y.toFloat(),
                    distance,
                    Path.Direction.CW
                )
                canvas.drawPath(path, paint)
            }
            circle.point_info.size == 3 -> {
                //椭圆
                val leftPoint = mapToCanvas(
                    bitmap,
                    map,
                    circle.point_info[1]
                )
                val bottomPoint = mapToCanvas(
                    bitmap,
                    map,
                    circle.point_info[2]
                )
                val distanceLeft = distance(center, leftPoint)
                val distanceBottom = distance(center, bottomPoint)
                val left = center.x - distanceLeft
                val top = center.y - distanceBottom
                val right = center.x + distanceLeft
                val bottom = center.y + distanceBottom
                val path = Path()
                path.addOval(
                    left.toFloat(),
                    top.toFloat(),
                    right.toFloat(),
                    bottom.toFloat(),
                    Path.Direction.CW
                )
                canvas.drawPath(path, paint)
            }
            else -> return
        }
        if (!TextUtils.isEmpty(circle.name)) {
            canvas.drawText(
                circle.name,
                (center.x + getTextOffsetX(scale)).toFloat(),
                (center.y + getTextOffsetY(scale)).toFloat(),
                paintText
            )
        }
    }

    /**
     * 绘制多边形
     */
    fun paintMapPolygonS(
        isPolygon: Boolean,
        scale: Float,
        canvas: Canvas,
        bitmap: Bitmap,
        polygon: MapRange,
        map: Map,
        paint: Paint,
        paintText: Paint
    ) {

        var firstPoint: MapPoint? = null
        val path = Path()
        for (k in 0 until polygon.point_info.size) {
            val mapPoints = mapToCanvas(
                bitmap,
                map,
                polygon.point_info[k]
            )
            if (k == 0) {
                firstPoint = mapPoints
                path.moveTo(mapPoints.x.toFloat(), mapPoints.y.toFloat())
            } else {
                path.lineTo(mapPoints.x.toFloat(), mapPoints.y.toFloat())
                if (isPolygon && k == polygon.point_info.size - 1) {
                    path.lineTo(firstPoint!!.x.toFloat(), firstPoint.y.toFloat())
                }
            }
        }
        canvas.drawPath(path, paint)
        if (firstPoint != null && !TextUtils.isEmpty(polygon.name)) {
            val text = polygon.name
            canvas.drawText(
                text,
                (firstPoint.x + getTextOffsetX(scale)).toFloat(),
                (firstPoint.y + getTextOffsetY(scale)).toFloat(),
                paintText
            )
        }

        if (polygon.isFocused) {
            for (k in 0 until polygon.point_info.size) {
                val mapPoints = mapToCanvas(
                    bitmap,
                    map,
                    polygon.point_info[k]
                )
                drawCircle(scale, canvas, mapPoints, paint)
            }
        }
    }

    /**
     * 绘制多边形
     */
    fun paintMapPolygon(
        isPolygon: Boolean,
        canvas: Canvas,
        bitmap: Bitmap,
        mMapPoints: List<MapPoint>,
        map: Map,
        paint: Paint,  offDistance: Int = 0
    ) {
        var firstPoint: MapPoint? = null
        val path = Path()
        for (k in mMapPoints.indices) {
            val mapPoints = mapToCanvas(
                bitmap,
                map,
                mMapPoints[k]
            )
            if (k == 0) {
                firstPoint = mapPoints
                path.moveTo(mapPoints.x.toFloat()+offDistance, mapPoints.y.toFloat()+offDistance)
            } else {
                path.lineTo(mapPoints.x.toFloat()+offDistance, mapPoints.y.toFloat()+offDistance)
                if (isPolygon && k == mMapPoints.size - 1) {
                    path.lineTo(firstPoint!!.x.toFloat()+offDistance, firstPoint.y.toFloat()+offDistance)
                }
            }
        }
        canvas.drawPath(path, paint)
    }

    /**
     * 绘制多边形
     */
    fun paintMapPolygon(
        isPolygon: Boolean,
        scale: Float,
        canvas: Canvas,
        bitmap: Bitmap,
        polygon: MapRange,
        map: Map,
        paint: Paint,
        paintText: Paint
    ) {

        var firstPoint: MapPoint? = null
        val path = Path()
        for (k in 0 until polygon.points.size) {
            val mapPoints = mapToCanvas(
                bitmap,
                map,
                polygon.points[k]
            )
            if (k == 0) {
                firstPoint = mapPoints
                path.moveTo(mapPoints.x.toFloat(), mapPoints.y.toFloat())
            } else {
                path.lineTo(mapPoints.x.toFloat(), mapPoints.y.toFloat())
                if (isPolygon && k == polygon.points.size - 1) {
                    path.lineTo(firstPoint!!.x.toFloat(), firstPoint.y.toFloat())
                }
            }
        }
        canvas.drawPath(path, paint)
        if (firstPoint != null && !TextUtils.isEmpty(polygon.name)) {
            val text = polygon.name
            canvas.drawText(
                text,
                (firstPoint.x + getTextOffsetX(scale)).toFloat(),
                (firstPoint.y + getTextOffsetY(scale)).toFloat(),
                paintText
            )
        }

        if (polygon.isFocused) {
            for (k in 0 until polygon.points.size) {
                val mapPoints = mapToCanvas(
                    bitmap,
                    map,
                    polygon.points[k]
                )
                drawCircle(scale, canvas, mapPoints, paint)
            }
        }
    }

    /**
     * 绘制点
     */
    fun paintPoint(
        point: MapPoint,
        canvas: Canvas,
        bitmap: Bitmap,
        map: Map,
        paint: Paint, text: String = "", radius: Float = 5f, isFocused: Boolean = false, offDistance: Int = 0
    ) {
        val mapPoint = mapToCanvas(
            bitmap,
            map,
            point
        )
//        paint.strokeWidth=20f
//        canvas.drawPoint(mapPoint.x.toFloat(), mapPoint.y.toFloat(), paint)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(mapPoint.x.toFloat() + offDistance, mapPoint.y.toFloat() + offDistance, radius, paint)

        if (!TextUtils.isEmpty(text)) {
            val paintPoint = Paint()
            paintPoint.style = Paint.Style.FILL
            paintPoint.isAntiAlias = true
            paintPoint.color = Color.BLACK
            canvas.drawText(
                text,
                (mapPoint.x + getTextOffsetX(5f)).toFloat(),
                (mapPoint.y + getTextOffsetX(5f)).toFloat(),
                paintPoint
            )
        }
    }

    /**
     * 多点绘制线
     */
    fun paintLine(
        points: List<MapPoint>,
        canvas: Canvas,
        bitmap: Bitmap,
        map: Map,
        paint: Paint, offDistance: Int = 0
    ) {
        for (i in 0 until points.size - 1) {
            val mapPoint1 = mapToCanvas(
                bitmap,
                map,
                points[i]
            )
            val mapPoint2 = mapToCanvas(
                bitmap,
                map,
                points[i + 1]
            )
            canvas.drawLine(
                mapPoint1.x.toFloat() + offDistance,
                mapPoint1.y.toFloat() + offDistance,
                mapPoint2.x.toFloat() + offDistance,
                mapPoint2.y.toFloat() + offDistance,
                paint
            )
        }
    }

    /**
     * 绘制图片
     */
    fun paintBitmap(
        point: MapPoint,
        canvas: Canvas,
        bitmap: Bitmap,
        map: Map,
        paint: Paint, res: Int, angle: Float, scale: Float = 1f, act: Context
    ) {

        try {
            val m = Matrix()
            m.setRotate(
                angle + 90
            )
            Log.i("角度", angle.toString())
            var b = getBitmap(act, res)
            m.postScale(scale, scale)
            val b2 = Bitmap.createBitmap(
                b, 0, 0, b.width,
                b.height, m, true
            )
            val mapPoint = mapToCanvas(
                bitmap,
                map,
                point
            )
            canvas.drawBitmap(
                b2,
                mapPoint.x.toFloat()-  b.width / 2,
                mapPoint.y.toFloat()-  b.width / 2,
                paint
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBitmap(context: Context, vectorDrawableId: Int): Bitmap {
        var bitmap: Bitmap? = null
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val vectorDrawable: Drawable? = context.getDrawable(vectorDrawableId)
            bitmap = Bitmap.createBitmap(
                vectorDrawable!!.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            vectorDrawable.draw(canvas)
        } else {
            bitmap = BitmapFactory.decodeResource(context.resources, vectorDrawableId)
        }
        return bitmap
    }

    fun drawCircle(
        scale: Float,
        canvas: Canvas,
        point: MapPoint,
        paint: Paint,
        radius: Float = getCircleRadius(scale)
    ) {
        paint.style = Paint.Style.FILL
        val dx = point.x.toFloat()
        val dy = point.y.toFloat()
        canvas.drawCircle(dx, dy, radius, paint)
    }

//    fun getColor(context: Context, rangeType: Int, workType: Int): Int {
//        val defValue = R.color.default_area
//        val type = "range${rangeType}work$workType"
//        return ResourcesUtils.getColorId(context, type, defValue)
//    }

//    private fun paintMapRange(
//        scale: Float,
//        context: Context,
//        canvas: Canvas,
//        range: MapRange,
//        bitmap: Bitmap,
//        map: Map,
//        paint: Paint,
//        paintText: Paint
//    ) {
//
//        val mapActivity = context as MapActivity
//        paint.style = Paint.Style.FILL_AND_STROKE
//        paint.strokeWidth = getLineWidth(scale)
//        paint.pathEffect = null
//
//        val colorId = getColor(context, range.range_type, range.work_type)
//        paint.color = ResourcesUtils.getColor(context, colorId)
//        if (range.isFocused) {
//            paint.color = ResourcesUtils.getColor(context, R.color.focused_area)
//        }
//        val signKeys = context.resources.getStringArray(R.array.sign_key)
//
//        when (range.graph_type) {
//            MapRange.Graph_Polygon -> {
//                //多边形/折线
//                val isPolygon = range.range_type == MapRange.Range_Area
//
//                if (Const.isDisplayToolShow(context as BaseActivity)) {
//                    if (isPolygon && range.work_type != MapRange.Work_Forbidden && !mapActivity.isAreaShow(
//                            signKeys[MapActivity.DisplayArea]
//                        )
//                    ) {
//                        return
//                    }
//
//                    if (range.work_type == MapRange.Work_Forbidden && !mapActivity.isAreaShow(
//                            signKeys[MapActivity.DisplayWall]
//                        )
//                    ) {
//                        return
//                    }
//
//                    if (!isPolygon && range.work_type != MapRange.Work_Forbidden && !mapActivity.isAreaShow(
//                            signKeys[MapActivity.DisplayLine]
//                        )
//                    ) {
//                        return
//                    }
//                }
//
//                if (isPolygon/* && range.work_type == MapRange.Work_Forbidden*/) {
//                    //内部填充颜色
//                    paintMapPolygon(isPolygon, scale, canvas, bitmap, range, map, paint, paintText)
//                }
//
//                paint.style = Paint.Style.STROKE
//                if (range.work_type == MapRange.Work_Forbidden) {
//                    paint.strokeWidth = ForbiddenLineWidth
//                    paint.color = ResourcesUtils.getColor(context, R.color.forbidden_line)
//                    paint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 2f)
//                } else {
//                    paint.pathEffect = null
//                }
//                //边线
//                paintMapPolygon(isPolygon, scale, canvas, bitmap, range, map, paint, paintText)
//            }
//            MapRange.Graph_Circle -> {
//                //椭圆/圆
//                if (range.work_type != MapRange.Work_Forbidden) {
//                    paintMapCircle(scale, canvas, bitmap, range, map, paint, paintText)
//                }
//
//                if (range.work_type == MapRange.Work_Forbidden) {
//                    paint.color = ResourcesUtils.getColor(context, R.color.forbidden_frame)
//                    paint.strokeWidth = ForbiddenLineWidth
//                    paint.style = Paint.Style.STROKE
//                    paint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 2f)
//                    paintMapCircle(scale, canvas, bitmap, range, map, paint, paintText)
//                } else {
//                    paint.pathEffect = null
//                }
//            }
//            MapRange.Graph_Bezier -> {
//                if (Const.isDisplayToolShow(context as BaseActivity) && !mapActivity.isAreaShow(
//                        signKeys[MapActivity.DisplayLine]
//                    )
//                ) {
//                    return
//                }
//                //贝塞尔曲线
//                if (range.point_info.size >= 4) {
//                    var startIndex = 0
//                    while (startIndex + 3 < range.point_info.size) {
//                        paintMapBezier(
//                            scale,
//                            context,
//                            canvas,
//                            bitmap,
//                            range,
//                            startIndex,
//                            map,
//                            paint,
//                            paintText
//                        )
//                        startIndex += 3
//                    }
//                }
//            }
//            MapRange.Graph_Rect -> {
//                if (Const.isDisplayToolShow(context as BaseActivity) && !mapActivity.isAreaShow(
//                        signKeys[MapActivity.DisplayArea]
//                    )
//                ) {
//                    return
//                }
//                //矩形
//                paint.style = Paint.Style.STROKE
//                paintMapPolygon(true, scale, canvas, bitmap, range, map, paint, paintText)
//            }
//        }
//
//        if (range.isFocused) {
//            paintTaskPoints(
//                context,
//                canvas,
//                bitmap,
//                range.points,
//                map,
//                paint
//            )
//        }
//    }

//    private fun paintTaskPoints(
//        context: Context,
//        canvas: Canvas,
//        bitmap: Bitmap,
//        points: ArrayList<MapPoint>,
//        map: Map,
//        paint: Paint
//    ) {
//
//        if (points.isEmpty()) {
//            return
//        }
//
//        paint.style = Paint.Style.STROKE
//        paint.strokeWidth = 1F
//        paint.color = ResourcesUtils.getColor(context, R.color.focused_line)
//
//        val path = Path()
//        for (k in 0 until points.size) {
//            val mapPoints = mapToCanvas(
//                bitmap,
//                map,
//                points[k]
//            )
//            if (k == 0) {
//                path.moveTo(mapPoints.x.toFloat(), mapPoints.y.toFloat())
//            } else {
//                path.lineTo(mapPoints.x.toFloat(), mapPoints.y.toFloat())
//            }
//        }
//        canvas.drawPath(path, paint)
//    }

    /**
     * 绘制任务多边形/折线
     */
    public fun paintTaskPolygon(
        isPolygon: Boolean,
        scale: Float,
        canvas: Canvas,
        bitmap: Bitmap,
        polygon: TaskRange,
        map: Map,
        paint: Paint,
        paintText: Paint
    ) {

        if (isPolygon) {
            paint.style = Paint.Style.FILL_AND_STROKE
        } else {
            paint.style = Paint.Style.STROKE
        }
        paint.strokeWidth = getLineWidth(scale)
        if (polygon.isFocused) {
            if (isPolygon) {
                paint.color = Color.BLUE
            } else {
                paint.color = Color.GREEN
            }
        } else {
            if (isPolygon) {
                paint.color = Color.BLUE
            } else {
                paint.color = Color.GREEN
            }
        }

        var firstPoint: MapPoint? = null
        val path = Path()
        for (k in 0 until polygon.points.size) {
            val mapPoints = mapToCanvas(
                bitmap,
                map,
                polygon.points[k]
            )
            if (k == 0) {
                firstPoint = mapPoints
                path.moveTo(mapPoints.x.toFloat(), mapPoints.y.toFloat())
            } else {
                path.lineTo(mapPoints.x.toFloat(), mapPoints.y.toFloat())
                if (isPolygon && k == polygon.points.size - 1) {
                    path.lineTo(firstPoint!!.x.toFloat(), firstPoint.y.toFloat())
                }
            }
        }
        canvas.drawPath(path, paint)
        if (firstPoint != null && !TextUtils.isEmpty(polygon.task_range_name)) {
            val text = polygon.task_range_name
            canvas.drawText(
                text,
                (firstPoint.x + getTextOffsetX(scale)).toFloat(),
                (firstPoint.y + getTextOffsetY(scale)).toFloat(),
                paintText
            )
        }
    }


    /**
     * 位点移动后同步range里的位点数据
     */
    fun onChangeOver(wayPoint: WayPoint, map: Map): Boolean {
        wayPoint.isChanged = true
        for (i in 0 until map.ranges.size) {
            val range = map.ranges[i]
            if (range.range_id == 0) {
                continue
            }
            for (j in 0 until range.point_info.size) {
                val rangePoint = range.point_info[j]
                if (rangePoint.point_id == wayPoint.point_id) {
                    if (rangePoint.x != wayPoint.x || rangePoint.y != wayPoint.y || rangePoint.angle != wayPoint.angle) {
                        rangePoint.type =
                            if (rangePoint.point_id == 0) RangePoint.TYPE_NEW else RangePoint.TYPE_MODIFY
                        if (rangePoint.type == RangePoint.TYPE_MODIFY) {
                            range.isChanged = true
                        }
                    }
                    rangePoint.x = wayPoint.x
                    rangePoint.y = wayPoint.y
                    rangePoint.angle = wayPoint.angle
                }
            }
        }
        return true
    }

    /**
     * 绘制地图
     */
//    fun elementToBitmap(
//        mContext: Context,
//        scale: Float,
//        bitmap: Bitmap,
//        map: Map
//    ): Bitmap {
//
//        val newBitmap = bitmap.copy(BitmapConfig, true)
//        val canvas = Canvas(newBitmap)
//
//        val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
//        paintText.textSize = getTextSize(scale)
//        paintText.color = Color.WHITE
//        paintText.style = Paint.Style.FILL
//        paintText.strokeCap = Paint.Cap.ROUND
//
//        paintBrushes(scale, canvas, map.brushes)
//
//        //gro_view_tool_top_display显示机器人/虚拟墙/点/线/区域
//        val signKeys = mContext.resources.getStringArray(R.array.sign_key)
//
//        val paint = Paint(Paint.ANTI_ALIAS_FLAG)//消除锯
//        paint.style = Paint.Style.FILL_AND_STROKE
//        paint.strokeWidth = getLineWidth(scale)
//        if (map.ranges.isNotEmpty()) {
//            paint.strokeCap = Paint.Cap.ROUND
//            if (map.ranges.isNotEmpty()) {
//                //先绘制range_type为1的区域
//                for (i in 0 until map.ranges.size) {
//                    val range = map.ranges[i]
//
//                    if (range.range_type != MapRange.Range_Area) {
//                        continue
//                    }
//
//                    paintMapRange(
//                        scale,
//                        mContext,
//                        canvas,
//                        range,
//                        bitmap,
//                        map,
//                        paint,
//                        paintText
//                    )
//                }
//
//                //再绘制range_type为2的路径
//                for (i in 0 until map.ranges.size) {
//                    val range = map.ranges[i]
//
//                    if (range.range_type != MapRange.Range_Path) {
//                        continue
//                    }
//
//                    paintMapRange(
//                        scale,
//                        mContext,
//                        canvas,
//                        range,
//                        bitmap,
//                        map,
//                        paint,
//                        paintText
//                    )
//                }
//            }
//        }
//
//        val mapActivity = mContext as MapActivity
//        if (!Const.isDisplayToolShow(mContext as BaseActivity) || mapActivity.isAreaShow(signKeys[MapActivity.DisplayPoint])) {
//            if (map.points.isNotEmpty()) {
//                //位点
//                for (i in 0 until map.points.size) {
//                    val wayPoint = map.points[i]
//                    if (wayPoint.real != WayPoint.Real_Point) {
//                        continue
//                    }
//                    paintMapWayPoint(
//                        scale,
//                        mContext,
//                        canvas,
//                        bitmap,
//                        wayPoint,
//                        map,
//                        paint,
//                        paintText
//                    )
//                }
//            }
//        }
//
//        if (map.polygonPoses.isNotEmpty()) {
//            //多边形临时点
//            paint.color = ResourcesUtils.getColor(mContext, R.color.focused_line)
//            paint.style = Paint.Style.STROKE
//            paint.pathEffect = null
//            val path = Path()
//            for (k in 0 until map.polygonPoses.size) {
//                val mapPoints = mapToCanvas(
//                    bitmap,
//                    map,
//                    map.polygonPoses[k]
//                )
//                if (k == 0) {
//                    path.moveTo(mapPoints.x.toFloat(), mapPoints.y.toFloat())
//                } else {
//                    path.lineTo(mapPoints.x.toFloat(), mapPoints.y.toFloat())
//                }
//            }
//            canvas.drawPath(path, paint)
//
//            for (k in map.polygonPoses.size - 1 downTo 0) {
//                val point = mapToCanvas(
//                    bitmap,
//                    map,
//                    map.polygonPoses[k]
//                )
//                drawCircle(scale, canvas, point, paint)
//                if (k == 0) {
//                    drawStartPointText(scale, mContext, canvas, point)
//                }
//            }
//        }
//
//        if (map.recordPoints.isNotEmpty()) {
//            //录制路径
//            paint.color = ResourcesUtils.getColor(mContext, R.color.draw_route)
//            paint.style = Paint.Style.STROKE
//            paint.strokeWidth = getPathWidth(scale)
//            paint.pathEffect = null
//            val path = Path()
//            for (k in 0 until map.recordPoints.size) {
//                val mapPoints = mapToCanvas(
//                    bitmap,
//                    map,
//                    map.recordPoints[k]
//                )
//                if (k == 0) {
//                    path.moveTo(mapPoints.x.toFloat(), mapPoints.y.toFloat())
//                } else {
//                    path.lineTo(mapPoints.x.toFloat(), mapPoints.y.toFloat())
//                }
//            }
//            canvas.drawPath(path, paint)
//        }
//
//        if (map.relocationPos != null) {
//            val wayPoint = WayPoint()
//            wayPoint.x = map.relocationPos!!.x
//            wayPoint.y = map.relocationPos!!.y
//            wayPoint.angle = map.relocationPos!!.angle
//            wayPoint.type.add(WayPoint.Type_Navigation)
//            paintMapWayPoint(scale, mContext, canvas, bitmap, wayPoint, map, paint, paintText)
//        }
//
//        if (map.rulers.isNotEmpty()) {
//            //测距尺
//            paint.color = Color.YELLOW
//            paint.strokeWidth = getRulerWidth(scale)
//            for (i in 1 until map.rulers.size) {
//                val pos0 = map.rulers[i - 1]
//                val pos1 = map.rulers[i]
//
//                val point0 = mapToCanvas(
//                    bitmap,
//                    map,
//                    pos0
//                )
//                val point1 = mapToCanvas(
//                    bitmap,
//                    map,
//                    pos1
//                )
//                canvas.e(
//                    point0.x.toFloat(),
//                    point0.y.toFloat(),
//                    point1.x.toFloat(),
//                    point1.y.toFloat(),
//                    paint
//                )
//            }
//
//            var distance = 0.0
//            for (i in 0 until map.rulers.size) {
//                val pos = map.rulers[i]
//                val point = mapToCanvas(
//                    bitmap,
//                    map,
//                    pos
//                )
//                var text = mContext.getString(R.string.ruler_start)
//                if (i > 0) {
//                    val d = distance(pos, map.rulers[i - 1])
//                    distance += d
//                    val unitId =
//                        if (distance > 1000) R.string.ruler_distance_km else R.string.ruler_distance_m
//                    var de = distance
//                    if (de > 1000) {
//                        de /= 1000
//                    }
//                    text = mContext.getString(unitId, de)
//                }
//                paint.color = ResourcesUtils.getColor(mContext, R.color.msgframe)
//                drawCircle(scale, canvas, point, paint)
//                paint.color = Color.YELLOW
//                drawCircle(scale, canvas, point, paint, (CircleRadius1 - 2) / scale)
//                if (i == 0) {
//                    drawStartPointText(scale, mContext, canvas, point)
//                } else if (i == map.rulers.size - 1) {
//                    drawNinepatchText(mContext, canvas, text, point, paintText)
//                }
//            }
//        }
//
//        return newBitmap
//    }

    fun hasFocusedElement(map: Map?): Boolean {
        if (map == null) {
            return false
        }

        val wayPoints = map.points
        for (i in wayPoints.size - 1 downTo 0) {
            val wayPoint = wayPoints[i]
            if (wayPoint.isFocused) {
                return true
            }
        }

        val ranges = map.ranges
        for (i in ranges.size - 1 downTo 0) {
            val range = ranges[i]
            if (range.isFocused) {
                return true
            }
        }
        return false
    }

    fun hasElement(map: Map?): Boolean {
        if (map == null) {
            return false
        }
        if (map.points.isNotEmpty()) {
            return true
        }
        if (map.ranges.isNotEmpty()) {
            return true
        }
        return false
    }

    fun isInDistancePixel(
        map: Map,
        bitmap: Bitmap,
        point1: MapPoint,
        point2: MapPoint,
        distance: Double = 20.0
    ): Boolean {
        val pos1 = mapToCanvas(bitmap, map, point1)
        val pos2 = mapToCanvas(bitmap, map, point2)
        val d = distance(pos1, pos2)
        return d <= distance
    }


    fun showElementMap(
        context: Context,
        map: Map,
        ranges: ArrayList<MapRange>,
        obstacleRange: ArrayList<MapRange>
    ): Bitmap? {
        val newBitmap: Bitmap
        try {
            val bitmapArray = Base64.decode(map.map_info.picture, Flags)
            val distanceWidth = map.map_info.max_pos.x - map.map_info.min_pos.x
            val distanceHeight = map.map_info.max_pos.y - map.map_info.min_pos.y
            if (map.resolution == 0.0) {
                map.resolution = map.map_info.resolution
            }
            val distance = max(distanceWidth, distanceHeight) / map.resolution
            val bitmap: Bitmap
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.size)

            //fix me 這個地方是大地图缩放处理，但是因为缩放的原因会导致橡皮擦那边的地图点击回弹，因为那边的没有缩放
//                if (distance < 1024) {
//                BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.size)
//            } else {
//                val sampleSize: Int = (distance / 1024).toInt()
//                byteToBitmap(bitmapArray, sampleSize) ?: return null
//            }
            newBitmap = bitmap.copy(BitmapConfig, true)
            val canvas = Canvas(newBitmap)
            var image = getBitmapFromVectorDrawable(context, R.drawable.gro_ic_waypoint_nav)
            var imagesel =
                getBitmapFromVectorDrawable(context, R.drawable.gro_ic_waypoint_nav_focused)


            val paint = Paint(Paint.ANTI_ALIAS_FLAG)//消除锯
            paint.style = Paint.Style.STROKE
            //测距尺

            val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
            paintText.textSize = 12f
            paintText.color = Color.WHITE
            paintText.style = Paint.Style.FILL
            paintText.strokeCap = Paint.Cap.ROUND
            for (point in map.points) {
                if (WayPoint.Type_Relocation in point.type || WayPoint.Type_Charge in point.type ||
                    WayPoint.Type_EWater in point.type || WayPoint.Type_HWater in point.type
                ) {
                    val bitmapPoint = mapToCanvas(bitmap, map, point)
                    val s = max(1.toFloat(), MAX_SCALE)
                    val rotate = point.angle.toFloat()
                    val matrix = Matrix()
                    matrix.setScale(s, s)
                    matrix.postRotate(360 - rotate)

                    if (point.isFocused) {
                        try {
                            val img_sel = Bitmap.createBitmap(
                                imagesel, 0, 0, imagesel.width,
                                imagesel.height, matrix, true
                            )
                            canvas.drawBitmap(
                                img_sel,
                                (bitmapPoint.x- image.width / 2).toFloat(),
                                (bitmapPoint.y- image.width / 2).toFloat(),
                                paint
                            )
                            drawWayPointText(context, canvas, point.name, bitmapPoint, paintText)
                        } catch (e: Exception) {
                        }

                    } else {
                        try {
                            val img_nor = Bitmap.createBitmap(
                                image, 0, 0, image.width,
                                image.height, matrix, true
                            )
                            canvas.drawBitmap(
                                img_nor,
                                (bitmapPoint.x- image.width / 2).toFloat(),
                                (bitmapPoint.y- image.width / 2).toFloat(),
                                paint
                            )
                        } catch (e: Exception) {
                        }

                    }

                }

            }

            if (ranges != null && ranges.size > 0) {
                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)//消除锯
                linePaint.style = Paint.Style.STROKE
                //测距尺
                linePaint.color = Color.parseColor("#D0021B")
                linePaint.strokeWidth = 2F
                linePaint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 2f)

                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.color = Color.BLUE
                paint.style = Paint.Style.FILL

                for (range in ranges) {
                    for (index in range.points.indices) {
                        if (range.isFocused) {
                            paint.color = Color.GREEN
                            val mapPoints = mapToCanvas(bitmap, map, range.points[index])
                            canvas.drawCircle(
                                mapPoints.x.toFloat(),
                                mapPoints.y.toFloat(),
                                4F,
                                paint
                            )
                        } else {
                            paint.color = Color.BLUE
                            val mapPoints = mapToCanvas(bitmap, map, range.points[index])
                            canvas.drawCircle(
                                mapPoints.x.toFloat(),
                                mapPoints.y.toFloat(),
                                2F,
                                paint
                            )
                        }

                        if (index > 0) {
                            val pos0 = mapToCanvas(bitmap, map, range.points[index - 1])
                            val pos1 = mapToCanvas(bitmap, map, range.points[index])
                            canvas.drawLine(
                                pos0.x.toFloat(),
                                pos0.y.toFloat(),
                                pos1.x.toFloat(),
                                pos1.y.toFloat(),
                                linePaint
                            )
                        }
                    }
                }

            }

            if (obstacleRange != null && obstacleRange.size > 0) {
                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)//消除锯
                linePaint.style = Paint.Style.STROKE
                //测距尺
                linePaint.color = Color.parseColor("#D0021B")
                linePaint.strokeWidth = 2F
                linePaint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 2f)

                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.color = Color.BLUE
                paint.style = Paint.Style.FILL

                for (obstacle in obstacleRange) {
                    for (index in obstacle.points.indices) {
                        if (obstacle.isFocused) {
                            paint.color = Color.GREEN
                            val mapPoints = mapToCanvas(bitmap, map, obstacle.points[index])
                            canvas.drawCircle(
                                mapPoints.x.toFloat(),
                                mapPoints.y.toFloat(),
                                4F,
                                paint
                            )
                        } else {
                            paint.color = Color.BLUE
                            val mapPoints = mapToCanvas(bitmap, map, obstacle.points[index])
                            canvas.drawCircle(
                                mapPoints.x.toFloat(),
                                mapPoints.y.toFloat(),
                                2F,
                                paint
                            )
                        }

                        if (index > 0) {
                            val pos0 = mapToCanvas(bitmap, map, obstacle.points[index - 1])
                            val pos1 = mapToCanvas(bitmap, map, obstacle.points[index])

                            canvas.drawLine(
                                pos0.x.toFloat(),
                                pos0.y.toFloat(),
                                pos1.x.toFloat(),
                                pos1.y.toFloat(),
                                linePaint
                            )
                            if (index == obstacle.points.size - 1 && !obstacle.isopen) {
                                val pos2 = mapToCanvas(bitmap, map, obstacle.points[0])
                                canvas.drawLine(
                                    pos1.x.toFloat(),
                                    pos1.y.toFloat(),
                                    pos2.x.toFloat(),
                                    pos2.y.toFloat(),
                                    linePaint
                                )
                            }
                        }
                    }
                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return newBitmap
    }



    private fun drawWayPointText(
        context: Context,
        canvas: Canvas,
        text: String,
        point: MapPoint,
        paintText: Paint
    ) {

        if (TextUtils.isEmpty(text)) {
            return
        }

        var position = 0//0上，1下，2左，3右

        val bounds = Rect()
        val textBounds = Rect()
        paintText.getTextBounds(text, 0, text.length, bounds)
        paintText.getTextBounds(text, 0, text.length, textBounds)

        val triH = 20
        var tx = 0F
        var ty = 0F
        var imgResId = 0
        var x = 0F
        var y = 0F

        if (point.y - bounds.height() - 100 - triH - bounds.height() < 0) {
            position = 1
        }
        if (point.x - bounds.width() / 2 - 30 < 0) {
            position = 3
        } else if (point.x + bounds.width() / 2 + 30 > canvas.width) {
            position = 2
        }

        when (position) {
            0 -> {
                //上
                bounds.left -= 10
                bounds.top -= 10
                bounds.right += 10
                bounds.bottom += 10
                val dx = -bounds.width() / 2 + 30
                val dy = -bounds.height() + 6
                imgResId = R.drawable.gro_wpbg
                x = (point.x + dx).toFloat() - 20
                y = (point.y + dy).toFloat()
                tx = (point.x - textBounds.width() / 2).toFloat()
                ty = y - 5
            }
            1 -> {
                //下
                bounds.left -= 10
                bounds.top -= 15
                bounds.right += 5
                bounds.bottom += 7
                val dx = -bounds.width() / 2 + 30
                val dy = bounds.height() - 6
                imgResId = R.drawable.gro_wpbg_down
                x = (point.x + dx).toFloat() - 20
                y = (point.y + dy).toFloat() + 5
                tx = (point.x - textBounds.width() / 2).toFloat()
                ty = y + textBounds.height() / 2 - 10
            }
            2 -> {
                //左
                bounds.left -= 10
                bounds.top -= 10
                bounds.right += triH
                bounds.bottom += 10
                val dx = -bounds.width() - 6
                val dy = bounds.height() / 4
                imgResId = R.drawable.gro_wpbg_left
                x = (point.x + dx).toFloat() + 5
                y = (point.y + dy).toFloat() - 2
                tx = x
                ty = y
            }
            3 -> {
                //右
                bounds.left -= 10
                bounds.top -= 10
                bounds.right += 10
                bounds.bottom += 10
                val dx = bounds.width() / 2 + 6
                val dy = bounds.height() / 4
                imgResId = R.drawable.gro_wpbg_right
                x = (point.x + dx).toFloat() - 10
                y = (point.y + dy).toFloat()
                tx = x + triH - 16
                ty = y
            }
        }

        canvas.translate(x, y)
        drawNinepatch(context, canvas, imgResId, bounds)
        canvas.translate(-x, -y)

        paintText.color = Color.parseColor("#ffffff")
        canvas.drawText(
            text, tx, ty,
            paintText
        )
    }

    fun getBitmapFromVectorDrawable(
        context: Context,
        drawableId: Int,
        width: Int = 0,
        height: Int = 0
    ): Bitmap {

        if (hmBitmap[drawableId] != null) {
            return hmBitmap[drawableId]!!
        }

        var drawable = ContextCompat.getDrawable(context, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }

        val drawableWidth = if (width > 0) width else drawable!!.intrinsicWidth
        val drawableHeight = if (height > 0) height else drawable!!.intrinsicHeight

        val bitmap = Bitmap.createBitmap(
            drawableWidth, drawableHeight,
            Bitmap.Config.ARGB_4444
        )
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)

        hmBitmap.put(drawableId, bitmap)
        return bitmap
    }


    fun relureToBitmap(context: Context, bitmap: Bitmap, map: Map): Bitmap {
        val newBitmap = bitmap.copy(BitmapConfig, true)
        val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
        paintText.textSize = 15f
        paintText.color = Color.WHITE
        paintText.style = Paint.Style.FILL
        paintText.strokeCap = Paint.Cap.ROUND
        if (map.rulers.isNotEmpty()) {
            val canvas = Canvas(newBitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)//消除锯
            paint.style = Paint.Style.STROKE
            //测距尺
            paint.color = Color.parseColor("#0354FF")
            paint.strokeWidth = 3F
            for (i in 1 until map.rulers.size) {
                val pos0 = map.rulers[i - 1]
                val pos1 = map.rulers[i]

                canvas.drawLine(
                    pos0.x.toFloat(),
                    pos0.y.toFloat(),
                    pos1.x.toFloat(),
                    pos1.y.toFloat(),
                    paint
                )
            }

            var distance = 0.0
            for (i in 0 until map.rulers.size) {
                Log.e("wwww", "3333333")
                val pos = map.rulers[i]
//                val point = mapToCanvas(
//                    bitmap,
//                    map,
//                    pos
//                )

                if (i > 0) {
                    val d = distance(pos, map.rulers[i - 1])
                    distance += d
                }
                paint.color = Color.BLUE
                paint.style = Paint.Style.FILL
                Log.e("wwww", "x:" + pos.x.toFloat() + " y:" + pos.y.toFloat())
                canvas.drawCircle(pos.x.toFloat(), pos.y.toFloat(), 5F, paint)

                if (map.rulers.size > 0 && i == map.rulers.size - 1) {
                    val ddf1: NumberFormat = NumberFormat.getNumberInstance()
                    ddf1.maximumFractionDigits = 2;
                    val dis = ddf1.format(distance * map.resolution);
                    drawNinepatchText(context, canvas, "$dis m", pos, paintText)
                }
            }
        }
        return newBitmap;
    }

    private fun drawNinepatchText(
        context: Context,
        canvas: Canvas,
        text: String,
        point: MapPoint,
        paintText: Paint
    ) {
        val bounds = Rect()
        paintText.getTextBounds(text, 0, text.length, bounds)
        bounds.left -= 15
        bounds.top -= 15
        bounds.right += 15
        bounds.bottom += 15

        val x = (point.x - bounds.width() / 2 + 15).toFloat()
        val y = (point.y - bounds.height() / 2).toFloat()

        canvas.translate(x, y)
        drawNinepatch(context, canvas, R.drawable.gro_msgframe, bounds)
        canvas.translate(-x, -y)

        paintText.color = Color.parseColor("#3576FF")
        canvas.drawText(
            text, x, y - 3,
            paintText
        )
    }
}