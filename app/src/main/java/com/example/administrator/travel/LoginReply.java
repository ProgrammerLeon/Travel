package com.example.administrator.travel;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/2/25.
 */

public class LoginReply implements Serializable
{
    boolean successful;
    public LoginReply(boolean b)
    {
        successful = b;
    }
    public boolean isSuccessful()
    {
        return successful;
    }
}
