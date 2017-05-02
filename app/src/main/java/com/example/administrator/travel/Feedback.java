package com.example.administrator.travel;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_LONG;

public class Feedback extends AppCompatActivity {
    private EditText mEditText;
    private Button mConnect, mPush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        mEditText=(EditText)findViewById(R.id.edit_feedback);
        mConnect=(Button)findViewById(R.id.conn_btn);
        mPush=(Button)findViewById(R.id.push_ad);
        mPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText.setText("");
                finish();
                Toast.makeText(getApplicationContext(),"建议提交成功",LENGTH_LONG).show();
            }
        });
        mConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" +123456789 ));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}
