package com.watermelonfueled.basiccards;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;

/**
 * Created by dapar on 2017-07-02.
 */

public class ImageHelper {

    public static Bitmap loadImage(String path, int targetW, int targetH) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,opts);
        int fileW = opts.outWidth;
        int fileH = opts.outHeight;
        int scaleFactor = Math.min(fileW/targetW, fileH/targetH);
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = scaleFactor;
        return BitmapFactory.decodeFile(path,opts);
    }

    public static int getScreenWidthPx (Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static float getScreenWidthDp(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels/dm.density;
    }

    public static float getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels/dm.density;
    }

    public static int getPixelsFromDp (Context context, int dp) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int) ((dp*dm.density)+0.5);
    }
}
