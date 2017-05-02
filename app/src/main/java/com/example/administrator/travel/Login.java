package com.example.administrator.travel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Login extends AppCompatActivity
{

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == R.id.login || id == EditorInfo.IME_NULL)
                {
                    attemptLogin();
                }
                return false;
            }
        });
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                attemptLogin();
            }
        });

    }

    public void attemptLogin()
    {
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        new NetworkThread(email, password).start();
    }

    class NetworkThread extends Thread
    {
        String mEmail;
        String mPassword;

        NetworkThread(String mEmail, String mPassword)
        {
            this.mEmail = mEmail;
            this.mPassword = mPassword;
        }

        @Override
        public void run()
        {
            DataInputStream dis = null;
            try
            {
                // Simulate network access.
                Socket socket = new Socket("192.168.0.153", 10000);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                XStream xStream = new XStream(new DomDriver());
                String s = xStream.toXML(new LoginRequest(mEmail, mPassword));
                dos.writeUTF(s);
//                dos.close();


                dis = new DataInputStream(socket.getInputStream());
                String reply = dis.readUTF();
                Object object = xStream.fromXML(reply);
                if (object instanceof LoginReply)
                {
                    LoginReply loginReply = (LoginReply) object;
                    if (loginReply.isSuccessful())
                    {
                        System.out.println("登录成功");
                        SharedPreferences sharedPreferences = getSharedPreferences(
                                "user_config", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", mEmail);
                        editor.commit();
                        finish();
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                    }
                    else
                    {
                        System.out.println("登录失败");
                    }
                }

                dis.close();
                dos.close();
                socket.close();
//                Thread.sleep(2000);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void register(View view)
    {
        Intent intent = new Intent(Login.this, RegisterActivity.class);
        startActivity(intent);
    }
}
