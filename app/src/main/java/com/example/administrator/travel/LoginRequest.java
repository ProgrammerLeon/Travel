package com.example.administrator.travel;

import java.io.Serializable;

public class LoginRequest implements Serializable
{
    private String Type = "Login Request";
    private String ID, PSW;
    public LoginRequest()
    {
    }
    public LoginRequest(String ID, String PSW)
    {
        this.ID = ID;
        this.PSW = PSW;
    }
    public String getType()
    {
        return Type;
    }
    public String getID()
    {
        return ID;
    }
    public String getPSW()
    {
        return PSW;
    }
}
