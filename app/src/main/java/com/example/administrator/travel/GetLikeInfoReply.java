package com.example.administrator.travel;

import java.io.Serializable;
import java.util.HashMap;

public class GetLikeInfoReply implements Serializable
{
	public HashMap<String, PicInfo> hashMap;

	public GetLikeInfoReply(HashMap<String, PicInfo> hashMap)
	{
		this.hashMap = hashMap;
	}
}
