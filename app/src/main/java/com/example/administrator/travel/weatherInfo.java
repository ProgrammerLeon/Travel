package com.example.administrator.travel;

import org.xmlpull.v1.XmlPullParser;

/**
 * Created by Administrator on 2017/2/26.
 */

public class weatherInfo
{

    static String cityname = "深圳";
    static String stateDetailed = "多云转阵雨";
    static String tem1 = "28";
    static String tem2 = "22";
    static String temNow = "25";
    static String windState = "微风";
    static String windDir = "北风";
    static String windPower = "2级";
    static String humidity = "63%";
    static String time = "10:30";

    public void setCityWeatherInfo(XmlPullParser xmlParser)
    {
        cityname = xmlParser.getAttributeValue(null, "cityname");
        stateDetailed = xmlParser.getAttributeValue(null, "stateDetailed");
        tem1 = xmlParser.getAttributeValue(null, "tem1");
        tem2 = xmlParser.getAttributeValue(null, "tem2");
        temNow = xmlParser.getAttributeValue(null, "temNow");
        windState = xmlParser.getAttributeValue(null, "windState");
        windDir = xmlParser.getAttributeValue(null, "windDir");
        windPower = xmlParser.getAttributeValue(null, "windPower");
        humidity = xmlParser.getAttributeValue(null, "humidity");
        time = xmlParser.getAttributeValue(null, "time");
    }

    public String getCityWeatherInfo()
    {
        String info = "所在城市：" + cityname + "\n"
                + "天气情况：" + stateDetailed + ", 湿度：" + humidity + "\n"
                + "现在气温：" + temNow + "°C, " + "最低：" + tem2 + "°C, " + "最高：" + tem1 + "°C\n"
                + "风情：" + windState + ", 风向：" + windDir + ", 风力：" + windPower + "\n"
                + "更新时间：" + time + "\n";
        return info;
    }

}