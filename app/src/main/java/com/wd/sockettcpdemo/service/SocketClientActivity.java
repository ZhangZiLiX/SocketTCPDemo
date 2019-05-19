package com.wd.sockettcpdemo.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wd.sockettcpdemo.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SocketClientActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int CODE_SOCKET_CONNECT = 8688;
    private TextView tvMessage;
    private EditText etReceive;
    private PrintWriter mPrintWriter;
    /**
     * 向服务器发消息
     */
    private Button btSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        // 开启服务端
        Intent intent = new Intent(this, SocketServerService.class);
        startService(intent);
        new Thread() {
            @Override
            public void run() {
                // 连接到服务端
                connectSocketServer();
            }
        }.start();
    }

    private void initView() {
        tvMessage = (TextView) findViewById(R.id.tv_message);
        etReceive = (EditText) findViewById(R.id.et_receive);
        btSend = (Button) findViewById(R.id.bt_send);
        btSend.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.bt_send:
                final String msg = etReceive.getText().toString();
                //向服务器发送信息
                if (!TextUtils.isEmpty(msg) && null != mPrintWriter) {
                    mPrintWriter.println(msg);
                    tvMessage.setText(tvMessage.getText() + "\n" + getCurrentTime() + "\n" + "客户端：" + msg);
                    etReceive.setText("");
                }
                break;
        }
    }


    private void connectSocketServer() {
        Socket socket = null;
        while (socket == null) {
            try {
                // 一个流套接字，连接到服务端
                socket = new Socket("localhost", CODE_SOCKET_CONNECT);
                // 获取到客户端的文本输出流
                mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
            } catch (IOException e) {
                e.printStackTrace();
                SystemClock.sleep(1000);
            }
        }

        try {
            // 接收服务端发送的消息
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!isFinishing()) {
                final String str = reader.readLine();
                if (str != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvMessage.setText(tvMessage.getText() + "\n" + getCurrentTime() + "\n" + "服务端：" + str);
                        }
                    });
                }
            }
            mPrintWriter.close();
            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }

}
