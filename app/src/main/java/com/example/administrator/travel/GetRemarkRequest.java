package com.example.administrator.travel;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/3/19.
 */

public class GetRemarkRequest implements Serializable
{
    public String picName;

    public GetRemarkRequest(String picName)
    {
        this.picName = picName;
    }
}
