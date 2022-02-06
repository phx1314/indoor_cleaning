package com.deepblue.cleaning.activity

import android.media.MediaPlayer
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.deepblue.cleaning.R
import com.deepblue.cleaning.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_shutdown.*

class ShutdownActivity : BaseActivity() {
    var mediaPlayer: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isIndex = true
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_shutdown)

        mediaPlayer = MediaPlayer()
        val uri = "android.resource://" + packageName.toString() + "/" + R.raw.welcome
        CommonUtil.play(this.applicationContext, mediaPlayer!!, sv_welcome, uri)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}