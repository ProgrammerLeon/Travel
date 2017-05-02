package com.example.administrator.travel;

import android.content.Intent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListenerManager
{
    /**
     * 单例模式
     */
    public static ListenerManager listenerManager;
    
    /**
     * 注册的接口集合，发送广播的时候都能收到
     */
    private List<mListener> mListenerList = new CopyOnWriteArrayList<mListener>();

    /**
     * 获得单例对象
     */
    public static ListenerManager getInstance()
    {
        if(listenerManager == null)
        {
            listenerManager = new ListenerManager();
        }
        return listenerManager;
    }
    
    /**
     * 注册监听
     */
    public void registerListtener(mListener mListener)
    {
        mListenerList.add(mListener);
    }
    
    /**
     * 注销监听
     */
    public void unRegisterListener(mListener mListener)
    {
        if(mListenerList.contains(mListener))
        {
            mListenerList.remove(mListener);
        }
    }
    
    /**
     * 发送广播
     */
    public void sendBroadCast(Intent intent)
    {
        for (mListener mListener : mListenerList)
        {
            mListener.notifyAllActivity(intent);
        }
    }
    
}
