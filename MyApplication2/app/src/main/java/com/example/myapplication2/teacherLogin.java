package com.example.myapplication2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class teacherLogin extends AppCompatActivity {
    public static StringBuffer message=new StringBuffer(" ");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_login);
        EditText editTextEmail, editTextPassword;
        Button buttonNext;
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                if (isValidEmail(email) && isValidPassword(password)) {
                    // Perform the next action here
                   // Toast.makeText(getApplicationContext(), "Email: " + email + "\nPassword: " + password, Toast.LENGTH_SHORT).show();
                    try {
                        StringBuffer res=connectServer(email, password);
                        Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    // Validation for email
    private boolean isValidEmail(String email) {
        // You can add your email validation logic here
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Validation for password
    private boolean isValidPassword(String password) {
        // You can add your password validation logic here (e.g., minimum length)
        return password.length() >= 8;
    }

    StringBuffer connectServer(String email, String password) throws IOException {


                try {


                    OkHttpClient client = new OkHttpClient();
                    String url = commonclass.url+"login";
                    RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("email", email)
                            .addFormDataPart("password", password)
                            .build();
                    Request request = new Request.Builder()

                            .url(url)
                            .post(requestBody)
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            message = new StringBuffer("error");
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT).show();
                            Log.d("code",response.code()+" ");
                            Log.d("message",response.message());
                            if (response.code() == 200) {
                                teacherLogin.message =  new StringBuffer("successfully verified");
                                Intent intent = new Intent(getApplicationContext(), student_first_details.class);
                                intent.putExtra("email", email);
                                intent.putExtra("password", password);

                                startActivity(intent);
                            } else {
                                teacherLogin.message =  new StringBuffer("please enter valid credentials");
                            }
                        }

                    });
//        ImageUploadTask task = new ImageUploadTask(picturePath.getPath(),
//        task.execute();
                }
                catch (Exception e)
                {
                    message= new StringBuffer(e.getMessage());
                }

return message;
    }
}
