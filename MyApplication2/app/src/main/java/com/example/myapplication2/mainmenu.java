package com.example.myapplication2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.IInterface;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class mainmenu extends AppCompatActivity {


    private Button newstudent,genclass,takeattendance,attendanceSheet;
    StringBuffer message=new StringBuffer(" ");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(getApplicationContext(),getIntent().getStringExtra("classFolder"),Toast.LENGTH_SHORT).show();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);
        newstudent= findViewById(R.id.button);
        genclass= findViewById(R.id.button2);
        takeattendance=findViewById(R.id.button3);
        attendanceSheet=findViewById(R.id.buttonAttendanceSheet);
        attendanceSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String classfolder[]=getIntent().getStringExtra("classFolder").split("-");

                try {
                    connectServer(getIntent().getStringExtra("email"),classfolder[0],classfolder[1],classfolder[2]);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        newstudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to SecondActivity
                Intent intent=new Intent(getApplicationContext(), student_second_details.class);
                intent.putExtra("classFolder",getIntent().getStringExtra("classFolder"));
                intent.putExtra("email",getIntent().getStringExtra("email"));
                intent.putExtra("password",getIntent().getStringExtra("password"));
                startActivity(intent);
            }
        });
        genclass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to SecondActivity
                try {
                   // progressBar.setVisibility(View.VISIBLE);

                    connectServer(getIntent().getStringExtra("email"),getIntent().getStringExtra("classFolder"));

                    Intent intent=new Intent(getApplicationContext(), progress.class);
                    intent.putExtra("classFolder",getIntent().getStringExtra("classFolder"));
                    intent.putExtra("email",getIntent().getStringExtra("email"));
                    intent.putExtra("password",getIntent().getStringExtra("password"));
                    startActivity(intent);

                } catch (Exception e) {
                    message=new StringBuffer(e.getMessage());
                    Log.d("error",e.getMessage());
                    throw new RuntimeException(e);
                }
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
            }
        });
        takeattendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to SecondActivity
                Intent intent=new Intent(getApplicationContext(), face_recognition.class);
                intent.putExtra("classFolder",getIntent().getStringExtra("classFolder"));
                intent.putExtra("email",getIntent().getStringExtra("email"));
                intent.putExtra("password",getIntent().getStringExtra("password"));
                startActivity(intent);

            }
        });
    }

    void connectServer(String dbname,String classFolder) throws IOException, InterruptedException {
                try {

                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(1, TimeUnit.MINUTES)
                            .readTimeout(1,TimeUnit.MINUTES)
                            .callTimeout(1,TimeUnit.MINUTES)
                            .build();
                    String url = commonclass.url;
                    RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("dbname",dbname)
                            .addFormDataPart("classFolder", classFolder)

                            .build();
                    Request request = new Request.Builder()

                            .url(url)
                            .post(requestBody)
                            .build();

                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.d("error",e.getMessage());
                            message = new StringBuffer(e.getMessage());

                        }
                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT).show();
                            if (response.code() == 200) {
                                teacherLogin.message=new StringBuffer("class is successfully created ");
                            } else {
                                teacherLogin.message =  new StringBuffer(response.message());
                            }
                        }
                    });
                }
                catch (Exception e)
                {
                    message= new StringBuffer(e.getMessage());
                    Log.d("error",e.getMessage());
                }

    }
    void connectServer(String dbname,String department, String year,String section) throws IOException {

        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                String url = commonclass.url+"attendancesheet";
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("dbname",dbname)
                        .addFormDataPart("department",department )
                        .addFormDataPart("year",year)
                        .addFormDataPart("section",section)
                        .build();
                Request request = new Request.Builder()

                        .url(url)
                        .post(requestBody)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                       // Toast.makeText(getApplicationContext(), "failed to connect", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        // Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT).show();
                        Log.e( "onResponse: ",(response.toString()));


                        Log.d( "shutdown: ","shutting down");
                        client.dispatcher().executorService().shutdown();
                        connectServer();
                        //createtable();

                    }
                });


            }

        }));
        // createtable();
        return;
    }
    void connectServer() throws IOException {

        String str = commonclass.url+"attendancesheet";
        URLConnection urlConn = null;
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(str);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            urlConnection.disconnect();
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            Intent intent=new Intent(getApplicationContext(), attendancesheet.class);
            intent.putExtra("classFolder",getIntent().getStringExtra("classFolder"));
            intent.putExtra("email",getIntent().getStringExtra("email"));
            intent.putExtra("password",getIntent().getStringExtra("password"));


            JSONObject jObject = new JSONObject(stringBuffer.toString());

            String attendance = jObject.get("nums").toString();
            Log.d("nums", attendance);
            JSONObject rollsobj = new JSONObject(attendance);
            Log.d("rolls", rollsobj.get("rolls").toString());
            String rolls = rollsobj.get("rolls").toString();
            Log.d("rolls", rolls.substring(1, rolls.length() - 1));
            rolls = rolls.substring(1, rolls.length() - 1);
            intent.putExtra("rollsno",rolls);


            JSONObject totalobj = new JSONObject(attendance);
            Log.d("rotal", totalobj.get("total").toString());
            String total = totalobj.get("total").toString();
            Log.d("total", total.substring(1, total.length() - 1));
            total = total.substring(1, total.length() - 1);
            intent.putExtra("totalclasses",total);


            JSONObject presentobj = new JSONObject(attendance);
            Log.d("present", presentobj.get("no_of_classes_present").toString());
            String present = presentobj.get("no_of_classes_present").toString();
            Log.d("present", present.substring(1, present.length() - 1));
            present = present.substring(1, present.length() - 1);
            intent.putExtra("attended",present);


            JSONObject precentageobj = new JSONObject(attendance);
            Log.d("percentage", precentageobj.get("percentage").toString());
            String precentage = precentageobj.get("percentage").toString();
            Log.d("percentage", precentage.substring(1, precentage.length() - 1));
            precentage = precentage.substring(1, precentage.length() - 1);
            intent.putExtra("percent",precentage);
            startActivity(intent);
//
            //    createtable();


            Log.d("ans", stringBuffer.toString());

        } catch (Exception ex) {
            Log.e("App", "yourDataTask", ex);

        } finally {

            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}