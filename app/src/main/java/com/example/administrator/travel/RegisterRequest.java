package com.example.administrator.travel;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/2/25.
 */

public class RegisterRequest implements Serializable
{
    private String Type = "Register Request";
    private String Name, PSW;

    public RegisterRequest(String username, String PSW)
    {
        this.PSW = PSW;
        this.Name = username;
    }

    public String getType()
    {
        return Type;
    }

    public String getPSW()

    {
        return PSW;
    }

    public String getUserame()
    {
        return Name;
    }
}
