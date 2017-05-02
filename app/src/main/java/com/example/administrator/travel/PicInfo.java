package com.example.administrator.travel;

import java.io.Serializable;

public class PicInfo implements Serializable
{
	int likeNum;
	boolean flag;

	public PicInfo(int likeNum, boolean flag)
	{
		this.flag = flag;
		this.likeNum = likeNum;
	}

	public int getLikeNum()
	{
		return likeNum;
	}

	public boolean getFlag()
	{
		return flag;
	}
}
