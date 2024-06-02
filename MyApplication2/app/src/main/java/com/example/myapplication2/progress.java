package com.example.myapplication2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;



public class progress extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        ProgressBar progress=findViewById(R.id.progressBar);
        Handler mhandler=new Handler();
        mhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(getApplicationContext(),mainmenu.class);
                intent.putExtra("classFolder",getIntent().getStringExtra("classFolder"));
                intent.putExtra("email",getIntent().getStringExtra("email"));
                intent.putExtra("password",getIntent().getStringExtra("password"));
                startActivity(intent);

            }
        },1000*15);
    }
}