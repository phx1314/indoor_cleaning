//
//  PopKeybord
//
//  Created by 86139 on 2020-09-15 09:29:52
//  Copyright (c) 86139 All rights reserved.


/**

 */

package com.deepblue.cleaning.item;

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import com.deepblue.cleaning.R
import com.mdx.framework.Frame
import com.mdx.framework.utility.Verify.isNumeric
import kotlinx.android.synthetic.main.item_pop_keybord.view.*
import java.lang.reflect.Method


class PopKeybord(context: Context?) : BaseItem(context) {
    var mPopShowKeyBord: PopShowBase? = null
    var mTextView: TextView? = null
    var isKeyAll = true
    var isShowEdit = false

    init {
        val flater = LayoutInflater.from(context)
        flater.inflate(R.layout.item_pop_keybord, this)

        mTextView_1.setOnClickListener(this)
        mTextView_2.setOnClickListener(this)
        mTextView_3.setOnClickListener(this)
        mTextView_4.setOnClickListener(this)
        mTextView_5.setOnClickListener(this)
        mTextView_6.setOnClickListener(this)
        mTextView_7.setOnClickListener(this)
        mTextView_8.setOnClickListener(this)
        mTextView_9.setOnClickListener(this)
        mTextView_0.setOnClickListener(this)

        mTextView_a.setOnClickListener(this)
        mTextView_b.setOnClickListener(this)
        mTextView_c.setOnClickListener(this)
        mTextView_d.setOnClickListener(this)
        mTextView_e.setOnClickListener(this)
        mTextView_f.setOnClickListener(this)
        mTextView_g.setOnClickListener(this)
        mTextView_h.setOnClickListener(this)
        mTextView_i.setOnClickListener(this)
        mTextView_j.setOnClickListener(this)
        mTextView_k.setOnClickListener(this)
        mTextView_l.setOnClickListener(this)
        mTextView_m.setOnClickListener(this)
        mTextView_n.setOnClickListener(this)
        mTextView_o.setOnClickListener(this)
        mTextView_p.setOnClickListener(this)
        mTextView_q.setOnClickListener(this)
        mTextView_r.setOnClickListener(this)
        mTextView_s.setOnClickListener(this)
        mTextView_t.setOnClickListener(this)
        mTextView_u.setOnClickListener(this)
        mTextView_v.setOnClickListener(this)
        mTextView_w.setOnClickListener(this)
        mTextView_x.setOnClickListener(this)
        mTextView_y.setOnClickListener(this)
        mTextView_z.setOnClickListener(this)
        mTextView_dot.setOnClickListener(this)
        mTextView_special.setOnClickListener(this)
        mTextView_sure.setOnClickListener {
            if (isShowEdit) {
                if (mEditText.text.toString().trim().length != 18) {
                    mLinearLayout_warning.visibility = View.VISIBLE
                    return@setOnClickListener
                } else {
                    Frame.HANDLES.sentAll("FrgGcmb", 119, mTextView)
                }
            }

            mPopShowKeyBord?.hide()
        }
        mTextView_sure1.setOnClickListener {
            if (isShowEdit) {
                if (mEditText.text.toString().trim().length != 18) {
                    mLinearLayout_warning.visibility = View.VISIBLE
                    return@setOnClickListener
                } else {
                    Frame.HANDLES.sentAll("FrgGcmb", 119, mTextView)
                }
            }
            mPopShowKeyBord?.hide()
        }
        mTextView_cancel.setOnClickListener { mPopShowKeyBord?.hide() }
        mImageButton_del.setOnClickListener(this)
        mImageButton_dx.setOnClickListener(this)
        mImageButton_dx.setOnClickListener(this)
        mImageButton_del.setOnLongClickListener {
            mTextView?.text = ""
            mEditText.setText("")
            true
        }

        (context as Activity)?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        try {
            val cls: Class<EditText> = EditText::class.java
            val setSoftInputShownOnFocus: Method
            setSoftInputShownOnFocus = cls.getMethod("setShowSoftInputOnFocus", Boolean::class.javaPrimitiveType)
            setSoftInputShownOnFocus.isAccessible = true
            setSoftInputShownOnFocus.invoke(mEditText, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onClick(v: View) {
        mTextView?.let {
            if (v is TextView) {
                val index = mEditText.selectionStart //获取光标所在位置
                val edit: Editable = mEditText.editableText //获取EditText的文字
                if (isKeyAll) {
                    edit.insert(index, v.text) //光标所在位置插入文字
                } else {
                    if (isNumeric(v.text.toString()) || v.text.toString() == ".") edit.insert(index, v.text) //光标所在位置插入文字
                }
//                if (isKeyAll) {
//                    it.text = it.text.toString() + v.text
//                } else {
//                    if (isNumeric(v.text.toString()) || v.text.toString() == ".") it.text = it.text.toString() + v.text
//                }
            } else {
                when (v.id) {
                    R.id.mImageButton_del -> {
                        val index = mEditText.selectionStart //获取光标所在位置
                        val edit: Editable = mEditText.editableText //获取EditText的文字
                        if (index > 0) edit.delete(index - 1, index);

//                        it.text = if (it.text.toString().isNotEmpty()) it.text.toString().substring(0, it.text.length - 1) else ""
                    }
                    R.id.mImageButton_dx -> {
                        if (mTextView_a.text.toString() == "a") {
                            mImageButton_dx.setImageResource(R.drawable.input_keybord_press_left_d)
                            updateDx(true)
                        } else {
                            mImageButton_dx.setImageResource(R.drawable.input_keybord_press_left_x)
                            updateDx(false)
                        }
                    }
                }
            }
            it.text = mEditText.text
//            mEditText.setText(it.text)
//            mEditText.setSelection(mEditText.text.length)
        }

    }

    fun updateDx(isBig: Boolean) {
        mTextView_a.text = if (isBig) "A" else "a"
        mTextView_b.text = if (isBig) "B" else "b"
        mTextView_c.text = if (isBig) "C" else "c"
        mTextView_d.text = if (isBig) "D" else "d"
        mTextView_e.text = if (isBig) "E" else "e"
        mTextView_f.text = if (isBig) "F" else "f"
        mTextView_g.text = if (isBig) "G" else "g"
        mTextView_h.text = if (isBig) "H" else "h"
        mTextView_i.text = if (isBig) "I" else "i"
        mTextView_j.text = if (isBig) "J" else "j"
        mTextView_k.text = if (isBig) "K" else "k"
        mTextView_l.text = if (isBig) "L" else "l"
        mTextView_m.text = if (isBig) "M" else "m"
        mTextView_n.text = if (isBig) "N" else "n"
        mTextView_o.text = if (isBig) "O" else "o"
        mTextView_p.text = if (isBig) "P" else "p"
        mTextView_q.text = if (isBig) "Q" else "q"
        mTextView_r.text = if (isBig) "R" else "r"
        mTextView_s.text = if (isBig) "S" else "s"
        mTextView_t.text = if (isBig) "T" else "t"
        mTextView_u.text = if (isBig) "U" else "u"
        mTextView_v.text = if (isBig) "V" else "v"
        mTextView_w.text = if (isBig) "W" else "w"
        mTextView_x.text = if (isBig) "X" else "x"
        mTextView_y.text = if (isBig) "Y" else "y"
        mTextView_z.text = if (isBig) "Z" else "z"

    }

    fun set(mPopShowKeyBord: PopShowBase, mTextView: TextView, isKeyAll: Boolean = true, isShowEdit: Boolean = false) {
        this.mPopShowKeyBord = mPopShowKeyBord
        this.mTextView = mTextView
        this.isKeyAll = isKeyAll
        this.isShowEdit = isShowEdit
        mLinearLayout_edit.visibility = if (isShowEdit) View.VISIBLE else View.GONE
        mLinearLayout_warning.visibility = if (isShowEdit) View.INVISIBLE else View.GONE

        if (mTextView.filters != null&& mTextView.filters.isNotEmpty() && mTextView.filters[0] is InputFilter.LengthFilter) {//可能会有其他情况filter 判断不太严谨
            val max = (mTextView.filters[0] as InputFilter.LengthFilter).max
            mEditText.filters = arrayOf(InputFilter.LengthFilter(max))
        }

    }

}