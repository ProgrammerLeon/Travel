package com.example.administrator.travel;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/3/19.
 */

public class GetRemarkReply implements Serializable
{
    public ArrayList<RemarkItem> remarkList;
    GetRemarkReply(ArrayList<RemarkItem> remarkList)
    {
        this.remarkList = remarkList;
    }

}
