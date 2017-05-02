package com.example.administrator.travel;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/3/20.
 */

public class DeleteRemarkRequest implements Serializable
{
    public String picName;
    public String userName;

    public DeleteRemarkRequest(String picName, String userName)
    {
        this.picName = picName;
        this.userName = userName;
    }
}
