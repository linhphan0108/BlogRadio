package com.linhphan.androidboilerplate.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by linhphan on 11/11/15.
 */
public class FileUtil {
    /**
     * check if the external storage is available for reading and writing
     * @return true if...
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * check if the external storage is available to at least reading
     * @return true if...
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * copy a special file
     * @param fileName the name of file
     * @param sourceDir the source directory where the file is located
     * @param DestinationDir the destination directory where the file will be copied to
     * @return the path that locate the new file
     */
    public static String copyFile(String fileName, String sourceDir, String DestinationDir) {
        InputStream inputStream;
        OutputStream outputStream;

        File dir = new File(DestinationDir);
        if (!dir.exists())
            dir.mkdirs();

        try {
            inputStream = new BufferedInputStream(new FileInputStream(sourceDir + fileName));
            File file = new File(DestinationDir + fileName);
            outputStream = new BufferedOutputStream(new FileOutputStream(file));

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            outputStream.flush();
            inputStream.close();
            outputStream.close();

            return file.getAbsolutePath();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * move a file to a special directory
     * @param fileName the name of the file
     * @param sourceDir the directory of source where the source file is located
     * @param destinationDir  the destination directory where the file will be moved to
     * @return the path that new file is located new destination folder
     */
    public static String moveFile(String fileName, String sourceDir, String destinationDir){
        InputStream inputStream = null;
        OutputStream outputStream = null;

        File dir = new File(destinationDir);
        if (!dir.exists())
            dir.mkdirs();

        try {
            File in = new File(sourceDir + fileName);
            File out = new File(destinationDir + fileName);
            inputStream = new BufferedInputStream(new FileInputStream(in));
            outputStream = new BufferedOutputStream(new FileOutputStream(out));

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1){
                outputStream.write(buffer, 0, read);
            }

            outputStream.flush();
            inputStream.close();
            outputStream.close();

            in.delete();//delete the old file

            return out.getAbsolutePath();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * delete a special file in a special directory
     * @param dir the directory where the file is located
     * @param fileName the name of file
     * @return true if the file was deleted otherwise return false
     */
    public static boolean deleteFile(String dir, String fileName) {
        return (new File(dir + fileName)).delete();

    }

    /**
     * delete a whole folder and it's content
     * @param fileOrDirectory the directory contains content which will be deleted
     * @return true if it's content is deleted successfully otherwise return false
     */
    public static boolean deleteFile(File fileOrDirectory){
        if (fileOrDirectory.isDirectory()){
            for (File child : fileOrDirectory.listFiles()){
                deleteFile(child);
            }
        }

        return fileOrDirectory.delete();
    }

    /**
     * try to parse an uri to string path
     * @param uri the uri which will be parsed
     * @return the string absolute path
     */
    public static String getAbsolutePath(Context context, Uri uri){
        String result = null;
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int col = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            if (col >= 0 && cursor.moveToFirst())
                result = cursor.getString(col);
            cursor.close();
        }
        return result;
    }

    /**
     * try to create a folder in external storage if it is exists. otherwise it will create a folder in internal storage
     * @return the path which new created folder was created, otherwise return null;
     */
    public static File getOrCreateAppFolder(String homeFolderName){
        if (isExternalStorageWritable()){//== if there is an external storage available
            File folder = new File(Environment.getExternalStorageDirectory(), homeFolderName);
            if (!folder.isDirectory()) {
                if (folder.mkdirs()) {
                    return folder;
                } else {
                    return null;
                }
            }else{
                return folder;
            }
        }else{
            File folder = new File(Environment.getDataDirectory(), homeFolderName);
            if (!folder.isDirectory()){
                if (folder.mkdirs()){
                    return folder;
                }else{
                    return null;
                }
            }else{
                return folder;
            }
        }
    }

    public static File getPublicMusicStorageDirectory(){
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    }

    public static File getPublicDownloadDirectory(){
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }
}
