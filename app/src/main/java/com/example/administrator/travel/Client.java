package com.example.administrator.travel;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;

public class Client extends Socket
{

    private static final String SERVER_IP = "192.168.0.153"; // ?????IP
    //    private static final String SERVER_IP = "192.168.43.44"; // ?????IP
    private static final int SERVER_PORT = 8899; // ???????

    private Socket client;

    private FileInputStream fis;

    private DataOutputStream dos;

    public Client() throws Exception
    {
        super(SERVER_IP, SERVER_PORT);
        this.client = Client.this;
        System.out.println("Cliect[port:" + client.getLocalPort() + "] ???????????");
    }


    private Bitmap getimage(String srcPath)
    {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww)
        {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        }
        else if (w < h && h > hh)
        {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
        {
            be = 1;
        }
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }


    private Bitmap compressImage(Bitmap image)
    {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 500)
        {  //循环判断如果压缩后图片是否大于200kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * ????????????
     *  @author David
     * @throws Exception
     */
    //TODO
    //FIXME
    public void sendFile(String path) throws Exception
    {
        try
        {
            System.out.println("发送文件");
            File file = new File(path);
            File tmp_file = new File(Environment.getExternalStorageDirectory() + File.separator + file.getName());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmp_file));
//            Bitmap bitmap = BitmapFactory.decodeFile(path);
            Bitmap bitmap = getimage(path);
            Bitmap com_bitmap = compressImage(bitmap);
            com_bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
            if (tmp_file.exists())
            {
                fis = new FileInputStream(tmp_file);
                dos = new DataOutputStream(client.getOutputStream());

                dos.writeUTF(file.getName());
                dos.flush();
                dos.writeLong(file.length());
                dos.flush();

                byte[] bytes = new byte[1024];
                int length = 0;
                long progress = 0;
                while ((length = fis.read(bytes, 0, bytes.length)) != -1)
                {
                    dos.write(bytes, 0, length);
                    dos.flush();
                    progress += length;
                    System.out.print("| " + (100 * progress / file.length()) + "% |\n");
                }
                System.out.println();
//                tmp_file.delete();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (fis != null)
            {
                fis.close();
            }
            if (dos != null)
            {
                dos.close();
            }
            client.close();
        }
    }

    /**
     * ???
     *
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            Client client = new Client(); // ?????????????
            client.sendFile(Environment.getExternalStorageDirectory().toString() + "/abc.png"); // ???????
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}