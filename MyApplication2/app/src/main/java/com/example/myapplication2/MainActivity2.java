package com.example.myapplication2;

import androidx.appcompat.app.AppCompatActivity;
import java.util.*;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity2 extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toast.makeText(getApplicationContext(),getIntent().getStringExtra("classFolder")+getIntent().getStringExtra("year")+getIntent().getStringExtra("section")+getIntent().getStringExtra("name")+getIntent().getStringExtra("pinno"),Toast.LENGTH_SHORT).show();

        Button upload=findViewById(R.id.upload);
        ProgressBar progressBar=findViewById(R.id.progress_bar);
        TextView text=findViewById(R.id.textView);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                String path =getExternalFilesDir(null).toString();
                Log.d("Files", "Path: " + path);
                File directory = new File(path);
                File[] files = directory.listFiles();
                Log.d("Files", "Size: "+ files.length);
                for (int i = 0; i < files.length; i++) {
                    Log.d("Files", "FileName:" + path +"/"+ files[i].getName());
                }
                if(files.length==50)
                {
                    for(int i=0;i<50;i++)
                    {

                        File pic=new File(path+"/"+files[i].getName());
                        try {
                            connectServer(getIntent().getStringExtra("email"),pic);
                           // deleteFile(path+"/"+files[i].getName());
                            progressBar.setProgress(progressBar.getProgress()+20);
                            text.setText(String.valueOf(i+2)+"%");

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                      //  deleteFile(path+"/"+files[i].getName());
                    }
                    Toast.makeText(getApplicationContext(), "uploaded successfully", Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(getApplicationContext(), mainmenu.class);
                    intent.putExtra("classFolder",getIntent().getStringExtra("classFolder"));
                    intent.putExtra("email",getIntent().getStringExtra("email"));
                    intent.putExtra("password",getIntent().getStringExtra("password"));
                    startActivity(intent);
                }
                else
                {
                    for (int i = 0; i < files.length; i++) {
                       deleteFile(path+"/"+files[i].getName());
                    }
                  //  Toast.makeText(getApplicationContext(),"there are more images \n please delete by clicking delete button",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    void connectServer(String dbname,File picturePath) throws IOException {

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                Bitmap bitmap = BitmapFactory.decodeFile(picturePath.getPath());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                String[] classFolders =getIntent().getStringExtra("classFolder").split("-");
                String[] studentfolders =getIntent().getStringExtra("studentfolder").split("-");
                String department=classFolders[0];
                String year=classFolders[1];
                String section=classFolders[2];
                String name=studentfolders[0];
                String pinno=studentfolders[1];

                String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                RequestBody requestBody = null;
                if (department != null && year!=null && section !=null && name!=null && pinno!=null) {
                    requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("classFolder",getIntent().getStringExtra("classFolder"))
                           .addFormDataPart("studentfolder",getIntent().getStringExtra("studentfolder"))
////
                            .addFormDataPart("name",picturePath.getName())
                            .addFormDataPart("image", encodedImage)
                            .build();
                }

                Request request = new Request.Builder()
                        .url(commonclass.url+"face")//http://192.168.0.128:8000/face
                        .post(requestBody)
                        .build();
                try {
                    OkHttpClient client = new OkHttpClient().newBuilder().build();
                    //client.connectTimeoutMillis(60, TimeUnit.SECONDS);
                    Response response = client.newCall(request).execute();
                    Toast.makeText(getApplicationContext(), "successully uploaded", Toast.LENGTH_SHORT).show();
                    deleteFile(picturePath.getPath());
                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(), "poor internet", Toast.LENGTH_SHORT).show();
                }


    }
    public boolean deleteFile(String path) {
        File file = new File(path);
        try {
            file.delete();

            return true;
        }
        catch (Exception e)
        {
            Log.d("error",e.getMessage());
        }
        return false;
    }

}