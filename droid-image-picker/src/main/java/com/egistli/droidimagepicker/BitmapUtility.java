package com.egistli.droidimagepicker;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by egistli on 2016/5/12.
 */
public class BitmapUtility {
    public static Bitmap rotate(final Bitmap bitmap, final int rotation) {
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }

    public static Bitmap decodeBitmap(final ContentResolver contentResolver, final Uri theUri, final int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;

        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = contentResolver.openAssetFileDescriptor(theUri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeFileDescriptor(assetFileDescriptor.getFileDescriptor(), null, options);
    }

    public static boolean writeBitmap(final Bitmap.CompressFormat format, final Bitmap bitmap, final String filePath) {
        final File file = new File(filePath);
        try {
            file.createNewFile();
            final FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(format, 100, outputStream);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
