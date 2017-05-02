package com.example.administrator.travel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WeatherActivity extends AppCompatActivity
{

    private Button mButton = null;
    private TextView mTextView = null;
    private EditText mCityNameEdit = null;

    final String DEBUG_TAG = "weather";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather);

        mTextView = (TextView) findViewById(R.id.infoText);
        mTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        mCityNameEdit = (EditText) findViewById(R.id.CityName);
        mButton = (Button) findViewById(R.id.ButtonGo);
        mButton.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                new hehe().start();
            }
        });

    }

    class hehe extends Thread
    {
        @Override
        public void run()
        {
            super.run();
            String httpUrl = "http://flash.weather.com.cn/wmaps/xml/" +HanZi2PinYin( mCityNameEdit.getText().toString()) + ".xml";

            String resultData = "";
            URL url = null;
            try
            {
                url = new URL(httpUrl);
            }
            catch (MalformedURLException e)
            {
                Log.e(DEBUG_TAG, "MalformedURLException");
            }
            if (url != null)
            {
                try
                {
                    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                    InputStreamReader in = new InputStreamReader(urlConn.getInputStream());
                    BufferedReader buffer = new BufferedReader(in);
                    String inputLine = null;
                    while (((inputLine = buffer.readLine()) != null))
                    {
                        resultData += inputLine + "\n";
                    }
                    in.close();
                    urlConn.disconnect();
                    if (resultData != null)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                mTextView.setText("");
                            }
                        });
                        weatherInfoXmlPullParser(resultData);
                    }
                    else
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                mTextView.setText("读取的内容为NULL");
                            }
                        });
                    }
                }
                catch (IOException e)
                {
                    Log.e(DEBUG_TAG, "IOException");
                }
            }
            else
            {
                Log.e(DEBUG_TAG, "Url NULL");
            }
        }
    }

    public void weatherInfoXmlPullParser(String buffer)
    {

        XmlPullParser xmlParser = Xml.newPullParser();

        ByteArrayInputStream tInputStringStream = null;
        if (buffer != null && !buffer.trim().equals(""))
        {
            tInputStringStream = new ByteArrayInputStream(buffer.getBytes());
        }
        else
        {
            return;
        }

        try
        {
            xmlParser.setInput(tInputStringStream, "UTF-8");

            int evtType = xmlParser.getEventType();

            while (evtType != XmlPullParser.END_DOCUMENT)
            {
                switch (evtType)
                {
                    case XmlPullParser.START_TAG:
                        String tag = xmlParser.getName();
                        if (tag.equalsIgnoreCase("city"))
                        {
                            final weatherInfo info = new weatherInfo();
                            info.setCityWeatherInfo(xmlParser);
                            final String s = info.getCityWeatherInfo() + "\n";

                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    mTextView.append(s);
                                }
                            });
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        break;
                }
                evtType = xmlParser.next();
            }
        }
        catch (XmlPullParserException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (Exception e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    private String HanZi2PinYin(String src) {
        char[] chars = src.toCharArray();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        StringBuffer sb = new StringBuffer();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
        for (int i = 0; i < chars.length; i++) {
            try {
                String[] strings = PinyinHelper.toHanyuPinyinStringArray(chars[i], format);
                if (strings != null) {
                    for (int j = 0; j < strings.length; j++) {
                        sb.append(strings[j]);
                        if (j != strings.length-1)
                            sb.append(",");
                    }
                }
            } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                badHanyuPinyinOutputFormatCombination.printStackTrace();
            }
        }
        return sb.toString();
    }
}
