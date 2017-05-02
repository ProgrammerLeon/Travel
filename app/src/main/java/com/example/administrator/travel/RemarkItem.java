package com.example.administrator.travel;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/3/19.
 */

public class RemarkItem implements Serializable
{
    public String userName;
    public String text;
    public String image;

    public RemarkItem(String userName, String text, String image)
    {
        this.userName = userName;
        this.text = text;
        this.image = image;
    }
}
