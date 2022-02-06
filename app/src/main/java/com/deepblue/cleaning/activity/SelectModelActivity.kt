package com.deepblue.cleaning.activity

import android.os.Bundle
import android.view.View
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R
import kotlinx.android.synthetic.main.activity_selmodel.*
import org.jetbrains.anko.startActivity

class SelectModelActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selmodel)

        if (Const.fieldModel.isNotEmpty()) {
            if (Const.fieldModel.equals(Const.INDOOR_MODEL.common_model.toString())) {
                rl_common.setBackgroundResource(R.drawable.model_checked)

            } else if (Const.fieldModel.equals(Const.INDOOR_MODEL.stone_model.toString())) {
                rl_stone.setBackgroundResource(R.drawable.stone_bg_sel)
            }
        }
        rl_common.setOnClickListener(this)
        rl_stone.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.rl_common -> {
                robotApp!!.mSharedPreferencesHelper!!.put(Const.SP_MODE, Const.INDOOR_MODEL.common_model)
                Const.fieldModel = Const.INDOOR_MODEL.common_model.toString()
                rl_common.setBackgroundResource(R.drawable.model_checked)
                rl_stone.setBackgroundResource(R.drawable.sel_stone)
//                startActivity<CleanMainActivity>()
                finish()
            }
            R.id.rl_stone -> {
                robotApp!!.mSharedPreferencesHelper!!.put(Const.SP_MODE, Const.INDOOR_MODEL.stone_model)
                Const.fieldModel = Const.INDOOR_MODEL.stone_model.toString()
                rl_common.setBackgroundResource(R.drawable.sel_model_bg)
                rl_stone.setBackgroundResource(R.drawable.stone_bg_sel)
//                startActivity<CleanMainActivity>()
                finish()
            }
        }
    }
}