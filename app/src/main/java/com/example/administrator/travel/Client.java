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
        //��ʼ����ͼƬ����ʱ��options.inJustDecodeBounds ���true��
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//��ʱ����bmΪ��

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //���������ֻ��Ƚ϶���800*480�ֱ��ʣ����ԸߺͿ���������Ϊ
        float hh = 800f;//�������ø߶�Ϊ800f
        float ww = 480f;//�������ÿ��Ϊ480f
        //���űȡ������ǹ̶��������ţ�ֻ�ø߻��߿�����һ�����ݽ��м��㼴��
        int be = 1;//be=1��ʾ������
        if (w > h && w > ww)
        {//�����ȴ�Ļ����ݿ�ȹ̶���С����
            be = (int) (newOpts.outWidth / ww);
        }
        else if (w < h && h > hh)
        {//����߶ȸߵĻ����ݿ�ȹ̶���С����
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
        {
            be = 1;
        }
        newOpts.inSampleSize = be;//�������ű���
        //���¶���ͼƬ��ע���ʱ�Ѿ���options.inJustDecodeBounds ���false��
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);//ѹ���ñ�����С���ٽ�������ѹ��
    }


    private Bitmap compressImage(Bitmap image)
    {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//����ѹ������������100��ʾ��ѹ������ѹ��������ݴ�ŵ�baos��
        int options = 100;
        while (baos.toByteArray().length / 1024 > 500)
        {  //ѭ���ж����ѹ����ͼƬ�Ƿ����200kb,���ڼ���ѹ��
            baos.reset();//����baos�����baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//����ѹ��options%����ѹ��������ݴ�ŵ�baos��
            options -= 10;//ÿ�ζ�����10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//��ѹ���������baos��ŵ�ByteArrayInputStream��
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//��ByteArrayInputStream��������ͼƬ
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
            System.out.println("�����ļ�");
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