package com.example.administrator.travel;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MyService extends Service
{
    private XStream xStream = new XStream(new DomDriver());//XML解析器
    private DataOutputStream dos = null;
    public static Socket socket = null;
    private Socket socket1 = null;


    public MyService()
    {

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate()
    {
        System.out.println("启动Service");
        new Initializer().start();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        try
        {
            dos.close();
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        System.out.println("启动服务");
        int msg = intent.getIntExtra("MSG", 0);
        if (msg == 1)//对象发送
        {
            Object obj = intent.getSerializableExtra("obj");
//        LoginRequest loginRequest = new LoginRequest();
//        Object obj = (Object)loginRequest;
            String s = xStream.toXML(obj);
            System.out.println("转换完毕");
            try
            {
                System.out.println("socket:" + socket);
                while (socket == null)
                {
                }
                if (socket.isClosed())
                {
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                socket = new Socket("192.168.0.153", 10000);
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                dos = new DataOutputStream(socket.getOutputStream());
                System.out.println("发送请求");
                dos.writeUTF(s);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return super.onStartCommand(intent, flags, startId);
        }
        else if (msg == 2)//图片发送
        {

            try
            {
                final String url = intent.getStringExtra("url");
                System.out.println(url + "将被上传");
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Client client = null;
                        try
                        {
                            client = new Client();
                            client.sendFile(url);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return super.onStartCommand(intent, flags, startId);
        }
        else
        {
            System.out.println("不知道你想干嘛");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    class Initializer extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                socket = new Socket("192.168.0.153", 10000);
//                socket1 = new Socket("10.0.2.2", 10001);
                System.out.println(socket);
                ReadHandler rh = new ReadHandler();
                Thread t = new Thread(rh);
                t.start();
                System.out.println("t.start()");


            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    class ReadHandler implements Runnable
    {

        @Override
        public void run()
        {
            System.out.println("run");
            DataInputStream dis = null;
            try
            {
//                System.out.println("socket created");
                while (true)
                {
//                    XStream xStream = new XStream(new DomDriver());
                    dis = new DataInputStream(socket.getInputStream());
                    System.out.println("得到输入流");
                    String s = dis.readUTF();
                    Object object = xStream.fromXML(s);
                    if (object instanceof TextMessage)
                    {
                        TextMessage textMessage = (TextMessage) object;
                        System.out.println(textMessage.text);

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("key", textMessage);
                        Intent intent = new Intent();
                        intent.setAction("default");
                        intent.putExtras(bundle);
                        sendBroadcast(intent);
                    }
                    else if (object instanceof PicReply)
                    {
                        PicReply picReply = (PicReply) object;
                        System.out.println("列表：" + picReply.getFilelist());
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("key", picReply);
                        Intent intent = new Intent();
                        intent.setAction("default");
                        intent.putExtras(bundle);
                        sendBroadcast(intent);
                    }
                    else if (object instanceof GetLikeInfoReply)
                    {
                        GetLikeInfoReply getLikeInfoReply = (GetLikeInfoReply) object;
                        MainActivity.mainActivity.LikeMap = getLikeInfoReply.hashMap;
                        System.out.println("likeMap:" + MainActivity.mainActivity.LikeMap);
                        PicInfo picInfo = MainActivity.mainActivity.LikeMap.get("shilaoren-win.png");
                        System.out.println("likeNum:" + picInfo.likeNum);
                        System.out.println("flag:" + picInfo.flag);
                    }
                    else if (object instanceof GetRemarkReply)
                    {
                        System.out.println("接收到评论回复");
                        GetRemarkReply getRemarkReply = (GetRemarkReply) object;
                        MainActivity.mainActivity.remarkItems = getRemarkReply.remarkList;
                        System.out.println("赋值成功:" + MainActivity.mainActivity.remarkItems);
                    }
                    else
                    {
                        System.out.println(s);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
