package com.example.administrator.travel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by Administrator on 2017/3/25.
 */

public class CacheUtil
{
    public static Bitmap getBitmapCache(String url)
    {
        File file = new File(MainActivity.mainActivity.getFilesDir() + File.separator + url.split("/")[url.split("/").length - 1]);
        System.out.println(file);
        FileInputStream fis = null;
        try
        {
            if (file.exists())
            {
                fis = new FileInputStream(file);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                System.out.println("缓存命中：" + bitmap);
                return bitmap;
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        finally
        {

        }
        return null;
    }

    public void setBitmapCache(String url, Bitmap bitmap)
    {
        File file = new File(MainActivity.mainActivity.getFilesDir() + File.separator + url.split("/")[url.split("/").length - 1]);
        System.out.println("设置缓存:" + file.getAbsolutePath());
        try
        {
            makeRootDirectory(Environment.getDataDirectory() + File.separator + "TravelApp" + File.separator + url.split("/")[url.split("/").length - 1]);
            if (file.exists())
            {
                file.delete();
            }
            else
            {
                file.createNewFile();
            }
//            FileOutputStream fos = new FileOutputStream(file);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
//            fos.flush();
//            fos.close();
//            System.out.println("缓存建立成功");
            new CacheWriter(file, bitmap).start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void makeRootDirectory(String filePath)
    {
        File file = null;
        try
        {
            file = new File(filePath);
            if (!file.exists())
            {
                file.mkdir();
            }
        }
        catch (Exception e)
        {

        }
    }

    class CacheWriter extends Thread
    {
        File file;
        Bitmap bitmap;

        CacheWriter(File file, Bitmap bitmap)
        {
            this.file = file;
            this.bitmap = bitmap;
        }

        @Override
        public void run()
        {
            FileOutputStream fos = null;
            try
            {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
                System.out.println("缓存建立成功");
                bitmap = null;
                System.gc();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
