package com.example.administrator.travel;

import android.content.Context;
import android.content.Intent;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yongchun.library.view.ImageSelectorActivity;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * created by hanyuanfeng
 */
public class Footstep extends AppCompatActivity
{
    private ArrayList<String> images = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private int maxSelectNum = 20;
    private ImageView mHeadImage;
    private TextView mNameText;
    private Button mButton;
    private String Url;
    private String mName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.footstep);
        mButton = (Button) findViewById(R.id.share_life_btn);
        mNameText = (TextView) findViewById(R.id.login_remind);

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
        mHeadImage = (ImageView) findViewById(R.id.index_my_list1_headphoto);
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
            }
        });
    }

    public void initView(int mode, boolean isCrop)
    {
        //mode 1表示多选，2表示单
        boolean isShow = true;
        boolean isPreview = true;
        ImageSelectorActivity.start(Footstep.this, maxSelectNum, mode, isShow, isPreview, isCrop);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK && requestCode == ImageSelectorActivity.REQUEST_IMAGE)
        {
            images = (ArrayList<String>) data.getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT);
            getImage(images);
        }
    }

    public void getImage(ArrayList<String> images)
    {
        if (images.size() == 1)
        {
            File mfile = new File(images.get(0));
            Url = getUrl(mfile);
//            if (mFlag == true) {
//                Url = getUrl(mfile);
//                Bitmap bmBitmap = null;
//                try {
//                    writetext();
//                    bmBitmap = BitmapFactory.decodeFile(readtext());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                Bitmap mbitmap = toRoundBitmap(bmBitmap);
//                mHeadImage.setImageBitmap(mbitmap);
//            }
        }
        else
        {
            for (int i = 0; i < images.size(); i++)
            {
                File mfile = new File(images.get(i));
                urls.add(getUrl(mfile));
            }
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

//    public void writetext() throws IOException {
//        File mfile = new File(Environment.getExternalStorageDirectory(), "apphistory.txt");
//        try {
//            String counter = Url;
//            OutputStream out = new FileOutputStream(mfile);
//            out.write(counter.getBytes());
//            out.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

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

        }
        return mStr;
    }
}
