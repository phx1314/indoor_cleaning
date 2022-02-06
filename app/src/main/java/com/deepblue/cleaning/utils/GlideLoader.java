package com.deepblue.cleaning.utils;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.mdx.framework.Frame;

import static com.mdx.framework.F.dp2px;

/**
 * Created by DELL on 2019/10/16.
 */

public class GlideLoader {
    public static void loadImage(String url, ImageView img, int i) {
        Glide.with(Frame.CONTEXT).load(url).error(i) //异常时候显示的图片
                .placeholder(i) //加载成功前显示的图片
                .fallback(i).into(img);
    }

    public static void loadCircleCropImage(String url, ImageView img, int i) {
        Glide.with(Frame.CONTEXT).load(url).error(i) //异常时候显示的图片
                .placeholder(i) //加载成功前显示的图片
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .fallback(i).into(img);
    }

//    public static void loadRoundImage(String url, ImageView img, int i) {
//        CornerTransform transformation = new CornerTransform(Frame.CONTEXT,  dp2px(Frame.CONTEXT, 5));
//        transformation.setExceptCorner(false, false, false, false);
//
//        Glide.with(Frame.CONTEXT)
//                .load(url)
//                .skipMemoryCache(false).dontAnimate()
//                .placeholder(img.getDrawable())
//                .error(img.getDrawable())
//                .transform(transformation)
//                .into(img);
//    }
//
//    public static void loadRoundTopImage(String url, ImageView img, int i) {
//        CornerTransform transformation = new CornerTransform(Frame.CONTEXT,  dp2px(Frame.CONTEXT, 5));
//        transformation.setExceptCorner(false, false, true, true);
//
//        Glide.with(Frame.CONTEXT)
//                .load(url)
//                .skipMemoryCache(false).dontAnimate()
//                .placeholder(i)
//                .error(i)
//                .transform(transformation)
//                .into(img);
//    }
//
//    public static void loadRoundBottomImage(String url, ImageView img, int i) {
//        CornerTransform transformation = new CornerTransform(Frame.CONTEXT, dp2px(Frame.CONTEXT, 5));
//        transformation.setExceptCorner(true, true, false, false);
//
//        Glide.with(Frame.CONTEXT)
//                .load(url)
//                .skipMemoryCache(false).dontAnimate()
//                .placeholder(i)
//                .error(i)
//                .transform(transformation)
//                .into(img);
//    }

    public static void loadImage_error(String url, ImageView img) {
        Glide.with(Frame.CONTEXT).load(url).error(img.getDrawable()) //异常时候显示的图片
                .placeholder(img.getDrawable()).into(img);
    }


}
