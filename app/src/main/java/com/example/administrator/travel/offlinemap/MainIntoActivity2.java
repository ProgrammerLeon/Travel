package com.example.administrator.travel.offlinemap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.administrator.travel.CacheUtil;
import com.example.administrator.travel.GetRemarkRequest;
import com.example.administrator.travel.MainActivity;
import com.example.administrator.travel.MyService;
import com.example.administrator.travel.R;
import com.example.administrator.travel.RemarkListAdapter;
import com.example.administrator.travel.RemarkRequest;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class MainIntoActivity2 extends AppCompatActivity
{
    private ImageView mSpring;
    private ImageView mSummer;
    private ImageView mAutumn;
    private ImageView mWinter;
    private ListView remarkList;
    private Button button;
    private RelativeLayout remarkBar;
    private EditText editText;
    private static String url_Summer;
//    String url = "http://www.davidzhao.cn/pic/badaguan-win.png";
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_into2);
        ImageView mSummer = (ImageView) findViewById(R.id.spot_summer);
        url_Summer = getIntent().getExtras().getString("Summer");
        loadImage(url_Summer, mSummer);
        System.out.println("图片接收成功");

        final String picName = url_Summer.split("/")[url_Summer.split("/").length - 1];

        final ObjectSender ObjectSender = new ObjectSender(new GetRemarkRequest(picName));
        ObjectSender.start();


        System.out.println("发送拉取评论请求");
        remarkList = (ListView) findViewById(R.id.remarkList);
//        //创建列表
//        ArrayList<RemarkItem> arrayList = new ArrayList<>();
//        arrayList.add(new RemarkItem("David", "很好的景点", "hahaha"));
//        arrayList.add(new RemarkItem("小明", "妈的智障 只有四天了", "hahaha"));
//        arrayList.add(new RemarkItem("小红", "这图片真的是个景点？", "hahaha"));
//        arrayList.add(new RemarkItem("外星人A", "FNDSKLN FLSDK ", "hahaha"));
//        arrayList.add(new RemarkItem("外星人B", "似懂非懂收复失地", "hahaha"));
//        arrayList.add(new RemarkItem("David", "很好的景点", "hahaha"));
//        arrayList.add(new RemarkItem("David", "很好的景点", "hahaha"));
//        arrayList.add(new RemarkItem("David", "很好的景点", "hahaha"));
//        arrayList.add(new RemarkItem("David", "很好的景点", "hahaha"));
//        arrayList.add(new RemarkItem("David", "很好的景点", "hahaha"));
//        arrayList.add(new RemarkItem("David", "很好的景点", "hahaha"));
//        arrayList.add(new RemarkItem("David", "很好的景点", "hahaha"));
//        arrayList.add(new RemarkItem("David", "很好的景点", "hahaha"));

        while (MainActivity.mainActivity.remarkItems == null)
        {
        }
        //创建列表
        remarkList.setAdapter(new RemarkListAdapter(getApplicationContext(), MainActivity.mainActivity.remarkItems));
        MainActivity.mainActivity.remarkItems = null;

        remarkBar = (RelativeLayout) findViewById(R.id.remarkBar);
        editText = (EditText) findViewById(R.id.remarkInput);
        Button remarkButton = (Button) findViewById(R.id.remark);
        sendButton = (Button) findViewById(R.id.sendButton);
        remarkButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                remarkBar.setVisibility(View.VISIBLE);
                editText.setVisibility(View.VISIBLE);
                sendButton.setVisibility(View.VISIBLE);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //发送信息
                RemarkRequest remarkRequest = new RemarkRequest(picName, MainActivity.mainActivity.getUserName(), editText.getText().toString());
                ObjectSender os = new ObjectSender(remarkRequest);
                os.start();


                //发送信息

                remarkBar.setVisibility(View.GONE);
                editText.setVisibility(View.GONE);
                sendButton.setVisibility(View.GONE);
                editText.setText("");
                String picName = url_Summer.split("/")[url_Summer.split("/").length - 1];

                ObjectSender ObjectSender = new ObjectSender(new GetRemarkRequest(picName));
                ObjectSender.start();

                while (MainActivity.mainActivity.remarkItems == null)
                {
                }
                remarkList.setAdapter(new RemarkListAdapter(getApplicationContext(), MainActivity.mainActivity.remarkItems));
                MainActivity.mainActivity.remarkItems = null;

            }
        });
    }

    public void loadImage(String url, ImageView view)
    {
        NetService netService = new NetService(url);
        netService.start();
        Bitmap bitmap;
        if ((bitmap = CacheUtil.getBitmapCache(url)) != null)
        {
            view.setImageBitmap(bitmap);
        }
        while ((bitmap = netService.getBitmap()) == null)
        {
//            try
//            {
//                Thread.sleep(50);
//            }
//            catch (InterruptedException e)
//            {
//                e.printStackTrace();
//            }
        }
        view.setImageBitmap(bitmap);
        CacheUtil cacheUtil = new CacheUtil();
        cacheUtil.setBitmapCache(url, bitmap);
    }

    class ObjectSender extends Thread
    {
        Object object;

        ObjectSender(Object object)
        {
            this.object = object;
        }

        @Override
        public void run()
        {
            Socket socket = MyService.socket;
            try
            {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                XStream xStream = new XStream(new DomDriver());
                String s = xStream.toXML(object);
                dos.writeUTF(s);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }


    class NetService extends Thread
    {
        String url;
        Bitmap bitmap = null;


        public Bitmap getBitmap()
        {
            return bitmap;
        }

        NetService(String url)
        {
            this.url = url;
        }

        @Override
        public void run()
        {
            URL myFileURL;
            try
            {
                myFileURL = new URL(url);
                //获得连接
                HttpURLConnection conn = (HttpURLConnection) myFileURL.openConnection();
                //设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
                conn.setConnectTimeout(0);
                //连接设置获得数据流
                conn.setDoInput(true);
                //不使用缓存
                conn.setUseCaches(true);
                //这句可有可无，没有影响
                conn.connect();
                //得到数据流
                InputStream is = conn.getInputStream();
                //解析得到图片
                bitmap = BitmapFactory.decodeStream(is);
                //关闭数据流
                is.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap getHttpBitmap(String url)
    {
        URL myFileURL;
        Bitmap bitmap = null;
        try
        {
            myFileURL = new URL(url);
            //获得连接
            HttpURLConnection conn = (HttpURLConnection) myFileURL.openConnection();
            //设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
            conn.setConnectTimeout(6000);
            //连接设置获得数据流
            conn.setDoInput(true);
            //不使用缓存
            conn.setUseCaches(true);
            //这句可有可无，没有影响
            //conn.connect();
            //得到数据流
            InputStream is = conn.getInputStream();
            //解析得到图片
            bitmap = BitmapFactory.decodeStream(is);
            //关闭数据流
            is.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return bitmap;

    }
}
