package com.example.administrator.travel;

import java.io.Serializable;

public class TextMessage implements Serializable
{
	public String text;

	public TextMessage(String text)
	{
		this.text = text;
	}

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }
}
