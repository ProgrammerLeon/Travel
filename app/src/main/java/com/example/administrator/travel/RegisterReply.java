package com.example.administrator.travel;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/2/25.
 */

public class RegisterReply implements Serializable
{
    boolean successful;

    public RegisterReply(boolean b)
    {
        successful = b;
    }

    public boolean isSuccessful()
    {
        return successful;
    }
}
