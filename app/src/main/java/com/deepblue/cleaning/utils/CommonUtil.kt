package com.deepblue.cleaning.utils

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.deepblue.cleaning.R
import com.mdx.framework.Frame
import java.text.SimpleDateFormat
import java.util.*

object CommonUtil {
    fun logger(tag: String, msg: String) {
        val maxStrLength = 2001 - tag.length
        var msg2 = msg
        while (msg2.length > maxStrLength) {
            Log.e(tag, msg2.substring(0, maxStrLength))
            msg2 = msg2.substring(maxStrLength)
        }
        Log.e(tag, msg2)
    }

    fun play(context: Context, mediaPlayer: MediaPlayer, surfaceView: SurfaceView, path: String) {
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
                try {
                    mediaPlayer.run {
                        reset()
                        setDisplay(holder)
                        setDataSource(context, Uri.parse(path))
                        setOnPreparedListener { start() }
                        setOnCompletionListener { start() }
                        prepare()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
//                val canvas = holder?.lockCanvas()
//                val bitmap =
//                    BitmapFactory.decodeResource(context.resources, R.drawable.clean_bg)
//                canvas?.drawBitmap(bitmap,0f,0f,null)
//                holder?.unlockCanvasAndPost(canvas)
            }
        })
    }


    fun Long2Data(l: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
            Date(l)
        )
    }

    fun <T> Intent.get(key: String): T? {
        try {
            val extras = InterFieldMethod.mExtras.get(this) as Bundle
            InterFieldMethod.unparcel.invoke(extras)
            val map = InterFieldMethod.mMap.get(extras) as Map<String, Any>
            return map[key] as T
        } catch (e: Exception) {
        }
        return null
    }

    /*
     后台true           前台false
     */
    fun isBackground(from: String): Boolean {
        return !(Frame.HANDLES.HANDLES.size > 0 && Frame.HANDLES.HANDLES[Frame.HANDLES.HANDLES.size - 1].id == from)
    }

}