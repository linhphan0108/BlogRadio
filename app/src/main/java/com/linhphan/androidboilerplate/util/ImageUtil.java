package com.linhphan.androidboilerplate.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by linhphan on 11/12/15.
 */
public class ImageUtil {

    /**
     * store a bitmap to the storage
     * @param dir  the directory where the bitmap will be stored
     * @param name the name of bitmap which included the extension
     */
    public static String storeBitmap(Bitmap bitmap, String dir, String name) {
        File directory = new File(dir);
        if (directory.isDirectory()) {
            directory.mkdirs();
        }

        File file = new File(directory + name);
        try {
            OutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return file.getAbsolutePath();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * try to retrieve an image from the given path
     * @return a bitmap if success otherwise return null
     */
    public static Bitmap retrieveBimap(String path, BitmapFactory.Options options) {
        return BitmapFactory.decodeFile(path, options);
    }
}
