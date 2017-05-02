package com.example.administrator.travel;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.administrator.travel.offlinemap.OfflineMapActivity;
import com.yongchun.library.view.ImageSelectorActivity;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
{
    public static MainActivity mainActivity;
    public HashMap<String, PicInfo> LikeMap;
    public ArrayList<RemarkItem> remarkItems;
    private ArrayList<String> images = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView recyclerView2;
    private DemoAdapter adapter;
    private DemoAdapter2 adapter2;
    private int maxSelectNum = 20;
    private ImageView mHeadImage;
    private LinearLayout mGoLoginBtn;
    private TextView mNameText, mGomapBtn, mGoaboutBtn, mGoFeedbackBtn;
    private String Url;
    private boolean mFlag = false;
    private String mName;
    private TextView mSeetingText, mWeatherText, offMapText;
    private Button mButton;
    private ArrayList<String> uploadList = new ArrayList<>();

    ImageButton button1;
    ImageButton button3;
    ImageButton button4;
    LinearLayout buttomBar;
    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;
    private MyReceiver receiver;
    static String s;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

//    public static void verifyStoragePermissions(Activity activity)
//    {
//        // Check if we have write permission
//        int permission = ActivityCompat.checkSelfPermission(activity,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//        if (permission != PackageManager.PERMISSION_GRANTED)
//        {
//            // We don't have permission so prompt the user
//            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
//                REQUEST_EXTERNAL_STORAGE);
//        }
//    }

    public static void verifyStoragePermissionsPro(Activity activity)
    {

    }

    public void getLikeInfo()
    {
        SharedPreferences sharedPreferences1 = getSharedPreferences(
                "user_config", Context.MODE_PRIVATE);
        String username = sharedPreferences1.getString("username", null);
//        System.out.println("获取成功：" + username);
        Bundle bundle = new Bundle();
        bundle.putSerializable("obj", new GetLikeInfoRequest(username));
        Intent intent = new Intent(MainActivity.this, MyService.class);
        intent.putExtras(bundle);
        intent.putExtra("MSG", 1);
        startService(intent);
    }

    public String getUserName()
    {
        SharedPreferences sharedPreferences1 = getSharedPreferences(
                "user_config", Context.MODE_PRIVATE);
        String username = sharedPreferences1.getString("username", null);
        return username;
    }


    public void startAct(Intent intent)
    {
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        mainActivity = this;

        getLikeInfo();


//        System.out.println(s);
        recyclerView = (RecyclerView) findViewById(R.id.recylerview);
        recyclerView.setHasFixedSize(true);


        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter = new DemoAdapter());

        adapter.replaceAll(getData());


        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("default");
        registerReceiver(receiver, filter);
        System.out.println("注册广播接收器成功");

        String[] perms = {"android.permission. WRITE_EXTERNAL_STORAGE"};

//        verifyStoragePermissions(this);

    }

    public ArrayList<String> getData()
    {
        ArrayList<String> list = new ArrayList<>();
        for (String url : ImageUtil.imageUrls)
        {
            list.add(url);
        }
        return list;
    }

    public ArrayList<String> getData2()
    {
//        ArrayList<String> list = new ArrayList<>();
//        for (String url : ImageUtil.imageUrls)
//        {
//            list.add(url);
//        }
        return uploadList;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        try
        {
            unregisterReceiver(receiver);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Intent intent = new Intent(MainActivity.this, MyService.class);
        stopService(intent);
    }

    private void findView()
    {
        button1 = (ImageButton) findViewById(R.id.button1);
//        button2 = (Button) findViewById(R.id.button2);
        button3 = (ImageButton) findViewById(R.id.button3);
        button4 = (ImageButton) findViewById(R.id.button4);
        mGomapBtn = (TextView) findViewById(R.id.go_map_btn);
        mNameText = (TextView) findViewById(R.id.login_remind);
        mGoFeedbackBtn = (TextView) findViewById(R.id.feedback_btn);
        mGoaboutBtn = (TextView) findViewById(R.id.about_btn);
        mGoLoginBtn = (LinearLayout) findViewById(R.id.login_btn);
        mSeetingText = (TextView) findViewById(R.id.me_item_1);
        mWeatherText = (TextView) findViewById(R.id.me_item_2);
        mHeadImage = (ImageView) findViewById(R.id.index_my_list1_headphoto);
        mButton = (Button) findViewById(R.id.share_life_btn);
        mNameText = (TextView) findViewById(R.id.login_remind);
        offMapText = (TextView) findViewById(R.id.off_map);


//        send = (Button) findViewById(R.id.button);
//        send.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("obj", new LoginRequest());
//                startService(new Intent(MainActivity.this, MyService.class).putExtras(bundle));
//            }
//        });
        buttomBar = (LinearLayout) findViewById(R.id.bottomBar);
        BottomBarListener bottomBarListener = new BottomBarListener();
        button1.setOnTouchListener(bottomBarListener);
//        button2.setOnTouchListener(bottomBarListener);
        button3.setOnTouchListener(bottomBarListener);
        button4.setOnTouchListener(bottomBarListener);


//        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.load1);
//
//        ImageFactory imageFactory = new ImageFactory();
//        try
//        {
//            System.out.println("压缩" + Environment.getExternalStorageDirectory().toString());
//            imageFactory.compressAndGenImage(bitmap, Environment.getExternalStorageDirectory().toString() + "/abc.png", 256);
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }

//        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
//        bitmap.compress(Bitmap.CompressFormat.PNG, 1, baos);
//        byte[] appicon = baos.toByteArray();// 转为byte数组
//        s = Base64.encodeToString(appicon, Base64.DEFAULT);
//        String s = "hehe";

        Bundle bundle = new Bundle();
        bundle.putSerializable("obj", new PicRequest());
        Intent intent = new Intent(MainActivity.this, MyService.class);
        intent.putExtras(bundle);
        intent.putExtra("MSG", 1);
        startService(intent);
        System.out.println(s);

//        Intent intent = new Intent(MainActivity.this, MyService.class);
//        intent.putExtra("MSG", 2);
//        intent.putExtra("url",)
//        startService(intent);
    }

    public static String getPic()
    {
        return s;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            //当手指按下的时候
            x1 = event.getX();
            y1 = event.getY();
        }
        else if (event.getAction() == MotionEvent.ACTION_UP)
        {
            //当手指离开的时候
            x2 = event.getX();
            y2 = event.getY();
//            System.out.println(buttomBar.getY());
            if (y1 - y2 > 50 && buttomBar.getY() <= y1 + 64)
            {
//                Toast.makeText(MainActivity.this, "向上滑", Toast.LENGTH_SHORT).show();
                buttomBar.setVisibility(View.VISIBLE);
                buttomBar.setEnabled(true);
            }
//            else if (y2 - y1 > 50)
//            {
//                Toast.makeText(MainActivity.this, "向下滑", Toast.LENGTH_SHORT).show();
////                button.setVisibility(View.INVISIBLE);
////                button.setEnabled(false);
//            }
//            else if (x1 - x2 > 50)
//            {
//                Toast.makeText(MainActivity.this, "向左滑", Toast.LENGTH_SHORT).show();
//            }
//            else if (x2 - x1 > 50)
//            {
//                Toast.makeText(MainActivity.this, "向右滑", Toast.LENGTH_SHORT).show();
//            }
        }
        return super.onTouchEvent(event);
    }

    class BottomBarListener implements View.OnTouchListener
    {
        float x1 = 0;
        float x2 = 0;
        float y1 = 0;
        float y2 = 0;

        @Override
        public boolean onTouch(View view, MotionEvent event)
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                //当手指按下的时候
                x1 = event.getX();
                y1 = event.getY();
                System.out.println(view.getId());
                System.out.println(button1.getId());
                System.out.println(button3.getId());
                System.out.println(button4.getId());
//景点
                if (view.getId() == button1.getId())
                {
                    getLikeInfo();
                    setContentView(R.layout.activity_main);
                    recyclerView = (RecyclerView) findViewById(R.id.recylerview);
                    recyclerView.setHasFixedSize(true);

                    recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                    recyclerView.setAdapter(adapter = new DemoAdapter());

                    adapter.replaceAll(getData());
                    findView();
                    BottomBarListener bottomBarListener = new BottomBarListener();
                    button1.setOnTouchListener(bottomBarListener);
//        button2.setOnTouchListener(bottomBarListener);
                    button3.setOnTouchListener(bottomBarListener);
                    button4.setOnTouchListener(bottomBarListener);

                }
                //足迹
                else if (view.getId() == button3.getId())
                {
                    getLikeInfo();
                    setContentView(R.layout.footstep);
                    findView();

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("obj", new PicRequest());
                    Intent intent = new Intent(MainActivity.this, MyService.class);
                    intent.putExtras(bundle);
                    intent.putExtra("MSG", 1);
                    startService(intent);
                    System.out.println(s);

                    recyclerView2 = (RecyclerView) findViewById(R.id.recylerview1);
                    recyclerView2.setHasFixedSize(true);

                    recyclerView2.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                    recyclerView2.setAdapter(adapter2 = new DemoAdapter2());

                    adapter2.replaceAll(getData2());

                    BottomBarListener bottomBarListener = new BottomBarListener();
                    button1.setOnTouchListener(bottomBarListener);
//        button2.setOnTouchListener(bottomBarListener);
                    button3.setOnTouchListener(bottomBarListener);
                    button4.setOnTouchListener(bottomBarListener);


                    SharedPreferences sharedPreferences1 = getSharedPreferences(
                            "user_config", Context.MODE_PRIVATE);
                    String username = sharedPreferences1.getString("username", null);
                    System.out.println("获取成功：" + username);
                    mName = username;
                    //显示昵称
                    if (mName != null)
                    {
                        mNameText.setText(mName);
                    }
                    //显示头像
                    Bitmap bmBitmap = null;
                    try
                    {
                        bmBitmap = BitmapFactory.decodeFile(readtext());
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
//        mHeadImage = (ImageView) findViewById(R.id.index_my_list1_headphoto);
                    if (bmBitmap != null)
                    {
                        Bitmap mbitmap = toRoundBitmap(bmBitmap);
                        mHeadImage.setImageBitmap(mbitmap);
                    }
                    else
                    {
                        mHeadImage.setImageResource(R.drawable.person_default);
                    }

                    mButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            initView(1, false);
                            mFlag = false;

//                            //获取最终url
//
//
//                            Intent intent = new Intent(MainActivity.this, MyService.class);
//                            intent.putExtra("MSG", 2);
//                            intent.putExtra("url", 最终url);
//                            startService(intent);
                        }
                    });

                }
                //个人中心
                else if (view.getId() == button4.getId())
                {
                    setContentView(R.layout.index_my);
                    findView();
                    BottomBarListener bottomBarListener = new BottomBarListener();
                    button1.setOnTouchListener(bottomBarListener);
//        button2.setOnTouchListener(bottomBarListener);
                    button3.setOnTouchListener(bottomBarListener);
                    button4.setOnTouchListener(bottomBarListener);


                    //显示昵称
                    SharedPreferences sharedPreferences1 = getSharedPreferences(
                            "user_config", Context.MODE_PRIVATE);
                    String username = sharedPreferences1.getString("username", null);
                    System.out.println("获取成功：" + username);
                    mName = username;
                    if (mName != null)
                    {
                        mNameText.setText(mName);
                    }

                    //显示头像
                    Bitmap bmBitmap = null;
                    try
                    {
                        bmBitmap = BitmapFactory.decodeFile(readtext());
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
//        mHeadImage = (ImageView) findViewById(R.id.index_my_list1_headphoto);
                    if (bmBitmap != null)
                    {
                        Bitmap mbitmap = toRoundBitmap(bmBitmap);
                        mHeadImage.setImageBitmap(mbitmap);
                    }
                    else
                    {
                        mHeadImage.setImageResource(R.drawable.person_default);
                    }
                    //头像选择
                    mHeadImage.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            initView(2, true);
                            mFlag = true;
                        }
                    });
                    offMapText.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            System.out.println("offMapText");
                            Intent mIntent = new Intent(MainActivity.this, OfflineMapActivity.class);
                            startActivity(mIntent);
                        }
                    });
                    mGomapBtn.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent mIntent = new Intent(MainActivity.this, NaviActivity.class);
                            startActivity(mIntent);
                        }
                    });
                    mSeetingText.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent mIntent = new Intent(MainActivity.this, SettingClass.class);
                            startActivity(mIntent);
                        }
                    });
                    mGoLoginBtn.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            unregisterReceiver(receiver);
                            Intent intent1 = new Intent(MainActivity.this, MyService.class);
                            stopService(intent1);
                            Intent mIntent = new Intent(MainActivity.this, Login.class);
                            finish();
                            startActivity(mIntent);
                            //从这里写登录代码
                        }
                    });
                    mWeatherText.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            System.out.println("mWeatherText");
                            Intent intent = new Intent(MainActivity.this, WeatherSearchActivity.class);
                            startActivity(intent);
                        }
                    });
                    mGoFeedbackBtn.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(MainActivity.this, Feedback.class);
                            startActivity(intent);
                        }
                    });
                    mGoaboutBtn.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(MainActivity.this, About.class);
                            startActivity(intent);
                        }
                    });


                }
            }
            else if (event.getAction() == MotionEvent.ACTION_UP)
            {
                //当手指离开的时候
                x2 = event.getX();
                y2 = event.getY();
//                if (y1 - y2 > 50)
//                {
//                    Toast.makeText(MainActivity.this, "向上滑 按钮", Toast.LENGTH_SHORT).show();
//                }
//                else
                if (y2 - y1 > 50)
                {
//                    Toast.makeText(MainActivity.this, "向下滑 按钮", Toast.LENGTH_SHORT).show();
                    buttomBar.setVisibility(View.INVISIBLE);
                    buttomBar.setEnabled(false);
                }
//                else if (x1 - x2 > 50)
//                {
//                    Toast.makeText(MainActivity.this, "向左滑 按钮", Toast.LENGTH_SHORT).show();
//                }
//                else if (x2 - x1 > 50)
//                {
//                    Toast.makeText(MainActivity.this, "向右滑 按钮", Toast.LENGTH_SHORT).show();
//                }
//            }
            }
            return true;
        }
    }

    class MyReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            System.out.println("接收到广播");
            Object obj = intent.getSerializableExtra("key");
            System.out.println("获取Object");
            if (obj instanceof TextMessage)
            {
                TextMessage textMessage = (TextMessage) obj;
                System.out.println("转化完成");
                System.out.println("接收到文本消息：" + textMessage.getText());
            }
            else if (obj instanceof PicReply)
            {
                PicReply picReply = (PicReply) obj;
                uploadList = picReply.getFilelist();
                System.out.println("图片列表获取成功");
            }
            else
            {
                System.out.println(obj.getClass());
            }
        }
    }

    public void footstep(View view)
    {
        setContentView(R.layout.footstep);
        System.out.println("hehe");
    }

    public void me(View view)
    {
        setContentView(R.layout.index_my);
    }

    public void initView(int mode, boolean isCrop)
    {
        //mode 1表示多选，2表示单
        boolean isShow = true;
        boolean isPreview = true;
        ImageSelectorActivity.start(MainActivity.this, maxSelectNum, mode, isShow, isPreview, isCrop);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        System.out.println("选择完成");
        if (resultCode == RESULT_OK && requestCode == ImageSelectorActivity.REQUEST_IMAGE)
        {
            System.out.println("if1");
            images = (ArrayList<String>) data.getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT);
            getImage(images);
//            if (images.size() != 1 && images != null)
//            {
//                System.out.println("if2");
//                Intent mIntent = new Intent(MainActivity.this, MyService.class);
//                mIntent.putExtra("MSG", 2);
//                for (int i = 0; i < urls.size(); i++)
//                {
//                    mIntent.putExtra("MSG", 2);
//                    mIntent.putExtra("url", urls.get(i));
//                    System.out.println(urls.get(i));
//                    startService(mIntent);
//                }
//            }
        }
    }

    public void getImage(ArrayList<String> images)
    {
        if (images.size() == 1)
        {
            File mfile = new File(images.get(0));
            Url = getUrl(mfile);
            if (mFlag == true)
            {//头像，写入文件
                Url = getUrl_forhead(mfile);
                Bitmap bmBitmap = null;
                try
                {
                    writetext();
                    bmBitmap = BitmapFactory.decodeFile(readtext());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                Bitmap mbitmap = toRoundBitmap(bmBitmap);//头像扁圆
                mHeadImage.setImageBitmap(mbitmap);//显示头像
            }
        }
        else
        {
            for (int i = 0; i < images.size(); i++)
            {
                File mfile = new File(images.get(i));
                urls.add(getUrl(mfile));
            }
        }
        if (images.size() != 1 && images.size() != 0)
        {
            Intent mIntent = new Intent(MainActivity.this, MyService.class);
            mIntent.putExtra("MSG", 2);
            for (int i = 0; i < urls.size(); i++)
            {//分享图片
                mIntent.putExtra("MSG", 2);
                mIntent.putExtra("url", urls.get(i));
                startService(mIntent);
            }
        }
        else if (images.size() == 1)
        {//传送头像
            Intent mIntent = new Intent(MainActivity.this, MyService.class);
            mIntent.putExtra("MSG", 2);
            mIntent.putExtra("url", Url);
            System.out.println(Url);
            startService(mIntent);
        }

    }


    public void copyFile(String oldPath, String newPath)
    {
        try
        {
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists())
            { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                //建立缓冲区
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1)
                {
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                System.out.println("写入成功");
            }
        }
        catch (Exception e)
        {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }
    }

    public String getUrl(File oldfile)
    {
        //获取当前时间
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date curDate = new Date(System.currentTimeMillis());
        String timeStr = formatter.format(curDate);
        //获取当前用户名
        SharedPreferences sharedPreferences1 = getSharedPreferences(
                "user_config", Context.MODE_PRIVATE);
        String username = sharedPreferences1.getString("username", null);
        System.out.println("获取成功：" + username);
        copyFile(oldfile.getAbsolutePath(), oldfile.getParent() + "/" + username + "_" + timeStr + ".png");
        String Str = oldfile.getParent() + "/" + username + "_" + timeStr + ".png";
        return Str;
    }

    public String getUrl_forhead(File oldfile)
    {
        //获取当前时间
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date curDate = new Date(System.currentTimeMillis());
        String timeStr = formatter.format(curDate);
        //获取当前用户名
        SharedPreferences sharedPreferences1 = getSharedPreferences(
                "user_config", Context.MODE_PRIVATE);
        String username = sharedPreferences1.getString("username", null);
        System.out.println("获取成功：" + username);
        copyFile(oldfile.getAbsolutePath(), oldfile.getParent() + "/" + username + "_" + "icon.png");
        String Str = oldfile.getParent() + "/" + username + "_" + "icon.png";
        return Str;
    }

    /**
     * 转换图片成圆形
     *
     * @param bitmap 传入Bitmap对象
     * @return
     */
    public Bitmap toRoundBitmap(Bitmap bitmap)
    {
        int width = 219;
        int height = 219;
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height)
        {
            roundPx = width / 2;

            left = 0;
            top = 0;
            right = width;
            bottom = width;

            height = width;

            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        }
        else
        {
            roundPx = height / 2;

            float clip = (width - height) / 2;

            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;

            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);// 设置画笔无锯齿

        canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas

        // 以下有两种方法画圆,drawRounRect和drawCircle
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);// 画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。
        // canvas.drawCircle(roundPx, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));// 设置两张图片相交时的模式,参考http://trylovecatch.iteye.com/blog/1189452
        canvas.drawBitmap(bitmap, src, dst, paint); // 以Mode.SRC_IN模式合并bitmap和已经draw了的Circle

        return output;
    }

    public void writetext() throws IOException
    {
        File mfile = new File(Environment.getExternalStorageDirectory(), "apphistory.txt");
        try
        {
            String counter = Url;
            OutputStream out = new FileOutputStream(mfile);
            out.write(counter.getBytes());
            out.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public String readtext() throws IOException
    {
        String mStr = "";
        File mfile = new File(Environment.getExternalStorageDirectory(), "apphistory.txt");
        if (mfile.exists())
        {
            FileInputStream fis = new FileInputStream(mfile.getAbsolutePath());
            DataInputStream dataIO = new DataInputStream(fis);
            String strLine = null;
            while ((strLine = dataIO.readLine()) != null)
            {
                mStr += strLine;
            }
            dataIO.close();
            fis.close();
        }
        else
        {
//            writetext();
        }
        return mStr;
    }
}