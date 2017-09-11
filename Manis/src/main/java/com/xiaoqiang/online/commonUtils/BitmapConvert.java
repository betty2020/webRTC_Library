package com.xiaoqiang.online.commonUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class BitmapConvert {

    /**
     * 根据源图片文件进行缩放并读取至内存
     * 
     * @param sourceFileName
     * @return
     * @throws FileNotFoundException
     */
    public static Bitmap resizeBitmap(String sourceFileName, int toSize) throws FileNotFoundException {
        return resizeBitmap(new FileInputStream(sourceFileName), toSize);
    }

    /**
     * 根据源图片文件进行缩放并读取至内存
     * 
     * @param sourceInputStream
     * @param toSize
     * @return
     */
    public static Bitmap resizeBitmap(InputStream sourceInputStream, int toSize) {

        Bitmap resizedBitmap = null;

        byte[] buffer = null;
        try {
            buffer = new byte[sourceInputStream.available()];
            sourceInputStream.read(buffer);
            sourceInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (buffer != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(buffer, 0, buffer.length, options);

            options.inJustDecodeBounds = false;

            int width = options.outWidth;
            int height = options.outHeight;
            if (width > toSize || height > toSize) {
                float temp = ((float) height) / ((float) width);
                int newHeight = 0, newWidth = 0;
                if (temp > 1) {
                    newHeight = toSize;
                    newWidth = (int) (newHeight / temp);
                } else {
                    newWidth = toSize;
                    newHeight = (int) (newWidth * temp);
                }
                float scaleWidth = ((float) width) / newWidth;

                int i = 1;
                while (true) {
                    if (scaleWidth < Math.pow(2, i)) {
                        double left = scaleWidth - Math.pow(2, i - 1);
                        double right = Math.pow(2, i) - scaleWidth;
                        options.inSampleSize = (left > right) ? (int) Math.pow(2, i) : (int) Math.pow(2, i - 1);
                        break;
                    }
                    i++;
                }
            } else {
                options.inSampleSize = 1;
            }
            resizedBitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length, options);
            buffer = null;
        }
        return resizedBitmap;
    }
}
