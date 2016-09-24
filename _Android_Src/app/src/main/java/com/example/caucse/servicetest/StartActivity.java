package com.example.caucse.servicetest;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

public class StartActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.cauicon);

        ConnectivityManager manager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = manager.getActiveNetworkInfo();

        if(network.getState() == NetworkInfo.State.CONNECTED){
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Intent i = new Intent(StartActivity.this, MainActivity.class); // xxx가 현재 activity,
                    startActivity(i);
                    finish();
                }
            }, 3000); // 1000ms

        }
        else{
            Toast.makeText(StartActivity.this, "인터넷 연결을 확인해주세요", Toast.LENGTH_LONG).show();
            finish();
        }

    }
}
