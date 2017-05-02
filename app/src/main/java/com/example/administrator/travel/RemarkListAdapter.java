package com.example.administrator.travel;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/3/19.
 */

public class RemarkListAdapter extends BaseAdapter
{
    private Context context;
    private ArrayList<RemarkItem> remarkList;
    private RemarkItem remarkItem;
    private int pos = -1;

    public RemarkListAdapter(Context context, ArrayList<RemarkItem> remarkList)
    {
        this.context = context;
        this.remarkList = remarkList;
    }

    @Override
    public int getCount()
    {
        return remarkList.size();
    }

    @Override
    public Object getItem(int i)
    {
        return i;
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup)
    {
        ViewHolder viewHolder = null;
        if (view == null)
        {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.remark_list, null);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.image);
            viewHolder.remark = (TextView) view.findViewById(R.id.remarkText);
            viewHolder.userName = (TextView) view.findViewById(R.id.userName);
            view.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) view.getTag();
        }

        remarkItem = remarkList.get(position);
        viewHolder.remark.setText(remarkItem.text);
        viewHolder.userName.setText(remarkItem.userName);//得到Resources对象
        Resources r = MainActivity.mainActivity.getResources();
//以数据流的方式读取资源
        Bitmap bitmap = BitmapFactory.decodeResource(MainActivity.mainActivity.getResources(), R.mipmap.ic_launcher);
        if (bitmap != null)
        {
            viewHolder.imageView.setImageBitmap(bitmap);
        }
        new IconLoader(viewHolder).start();
        viewHolder.imageView.setTag(null);
        return view;
    }

    public class ViewHolder
    {
        public ImageView imageView;
        public TextView userName;
        public TextView remark;
    }

    public class IconLoader extends Thread
    {
        ViewHolder viewHolder;

        IconLoader(ViewHolder viewHolder)
        {
            this.viewHolder = viewHolder;
        }

        @Override
        public void run()
        {
            URL myFileURL = null;
            try
            {
                myFileURL = new URL("http://192.168.0.153/icon/" + remarkItem.userName + "_icon.png");
                //获得连接
                HttpURLConnection conn = null;
                conn = (HttpURLConnection) myFileURL.openConnection();
                conn.setConnectTimeout(1000);
                //连接设置获得数据流
                conn.setDoInput(true);
                //不使用缓存
                conn.setUseCaches(true);
                //这句可有可无，没有影响
                conn.connect();
                //得到数据流
                InputStream is = conn.getInputStream();
                //解析得到图片
                final Bitmap bitmap = BitmapFactory.decodeStream(is);
                //关闭数据流
                is.close();
                if (bitmap != null)
                {
                    MainActivity.mainActivity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            viewHolder.imageView.setImageBitmap(bitmap);
                        }
                    });
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
