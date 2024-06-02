package com.example.myapplication2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class student_second_details extends AppCompatActivity {
    private EditText pinnumber,personname;
    private Button submitbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(getApplicationContext(),getIntent().getStringExtra("classFolder"),Toast.LENGTH_SHORT).show();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_second_details);
        pinnumber=findViewById(R.id.pin);
        personname=findViewById(R.id.PersonName);
        submitbutton=findViewById(R.id.button4);
        submitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] classFolders =getIntent().getStringExtra("classFolder").split("-");
               // String[] studentfolders =getIntent().getStringExtra("studentfolder").split("-");
                String department=classFolders[0];
                String year=classFolders[1];
                String section=classFolders[2];
//                String name=studentfolders[0];
//                String pinno=studentfolders[1];

                try {
                    StringBuffer res=connectServer(getIntent().getStringExtra("email"),pinnumber.getText().toString(),personname.getText().toString(),department,year,section);
                    Toast.makeText(getApplicationContext(),res,Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        });
    }
    StringBuffer connectServer(String dbname,String pinno, String name,String dept,String year,String section) throws IOException {



                OkHttpClient client = new OkHttpClient();
                String url = commonclass.url+"studentseconddetails";
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("dbname",dbname)
                        .addFormDataPart("department", dept)
                        .addFormDataPart("year", year)
                        .addFormDataPart("section", section)
                        .addFormDataPart("name", name)
                        .addFormDataPart("pinno", pinno)

                        .build();
                Request request = new Request.Builder()

                        .url(url)
                        .post(requestBody)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT).show();
                        if(response.code()==200)
                        {
                            teacherLogin.message= new StringBuffer("successfully verified");
                            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                            intent.putExtra("classFolder",getIntent().getStringExtra("classFolder"));
                            intent.putExtra("studentfolder",pinno+"-"+name);
                            intent.putExtra("email",getIntent().getStringExtra("email"));
                            intent.putExtra("password",getIntent().getStringExtra("password"));
                            startActivity(intent);
                        }
                        else
                        {

                            teacherLogin.message= new StringBuffer("student is already registered");
                        }
                    }
                });


//        ImageUploadTask task = new ImageUploadTask(picturePath.getPath(),
//        task.execute();


        return teacherLogin.message;
    }
}