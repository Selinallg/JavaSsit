package com.nolovr.javassit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
    public   void log(View view) {
        http("http:/baidu.com", " 编译时打印");
    }

    public void http(String url,String data) {
//        long start = System.currentTimeMillis();
//        long end = System.currentTimeMillis();
//        android.util.Log.d("llg-usetime", "http: "+(end - start));
    }
}
