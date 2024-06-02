package com.example.myapplication2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class student_first_details extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private EditText editTextDepartment, editTextYear, editTextSection;
    private Button buttonsubmit;
    public static String message=null;
    private Spinner departmentSpinner, yearSpinner, sectionSpinner;

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
//            case R.id.logout:
//
//
//
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_first_details);
        buttonsubmit = findViewById(R.id.buttonsubmit);
        departmentSpinner = findViewById(R.id.department_spinner);
        yearSpinner = findViewById(R.id.year_spinner);
        sectionSpinner = findViewById(R.id.section_spinner);
        final String[] department_values = getResources().getStringArray(R.array.departments_array);
        final Spinner dept = (Spinner)findViewById(R.id.department_spinner);

        final ArrayAdapter<String> ar = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,department_values);
        dept.setAdapter(ar);

        dept.setOnItemSelectedListener(this);

        //
        final String[] section_values = getResources().getStringArray(R.array.sections_array);
        final Spinner sect = (Spinner)findViewById(R.id.section_spinner);

        final ArrayAdapter<String> sect_ar = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,section_values);
        sect.setAdapter(sect_ar);

        sect.setOnItemSelectedListener(this);


        final String[] year_values = getResources().getStringArray(R.array.years_array);
        final Spinner years = (Spinner)findViewById(R.id.year_spinner);

        final ArrayAdapter<String> year_ar = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,year_values);
        years.setAdapter(year_ar);

        years.setOnItemSelectedListener(this);


        buttonsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to SecondActivity
                Intent intent = new Intent(getApplicationContext(), mainmenu.class);
                intent.putExtra("email",getIntent().getStringExtra("email"));
                intent.putExtra("password",getIntent().getStringExtra("password"));
                String department= departmentSpinner.getSelectedItem().toString();
                String year=yearSpinner.getSelectedItem().toString();
                String section= sectionSpinner.getSelectedItem().toString();
                try {
                    if(validate_spinners(department,year,section)) {
                        connectServer(getIntent().getStringExtra("email"),department, year, section);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"select values",Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        });
    }
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.department_spinner)
        {

        }
        else if(spinner.getId() == R.id.section_spinner)
        {
            //do your staff
        }


    }

    public void onNothingSelected(AdapterView<?> arg0)
    {
        // Auto-generated method stub
    }
    boolean validate_spinners(String department,String year,String section)
    {
        if(!department.equals("--department--")&&!year.equals("--year--")&&!section.equals("--section--"))
        {
            return true;
        }
        return false;
    }
    void connectServer(String dbname,String department, String year,String section) throws IOException {

        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                String url = commonclass.url+"student_first_details";
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
                        Toast.makeText(getApplicationContext(), "failed to connect", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT).show();
                        if(response.code()==200)
                        {
                            message="successfully verified";
                            Intent intent = new Intent(getApplicationContext(), mainmenu.class);
                            intent.putExtra("classFolder",department+"-"+year+"-"+section);
                            intent.putExtra("email",getIntent().getStringExtra("email"));
                            intent.putExtra("password",getIntent().getStringExtra("password"));
                            startActivity(intent);
                        }
                        else
                        {
                            message="error";
                        }


                    }
                });


//        ImageUploadTask task = new ImageUploadTask(picturePath.getPath(),
//        task.execute();
            }
        }));

    }
}