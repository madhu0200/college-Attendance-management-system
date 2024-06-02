package com.example.myapplication2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class attendancesheet extends AppCompatActivity {
    ArrayList<String> rollsno = new ArrayList<>();
    ArrayList<String> totalclasses = new ArrayList<>();
    ArrayList<String> attended = new ArrayList<>();
    ArrayList<String> percent = new ArrayList<>();

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendancesheet);


        String classFolder[] = getIntent().getStringExtra("classFolder").split("-");
        String department = classFolder[0];
        String year = classFolder[1];
        String section = classFolder[2];

        try {
            rollsno=new ArrayList(Arrays.asList(getIntent().getStringExtra("rollsno").split(",")));
            totalclasses=new ArrayList(Arrays.asList(getIntent().getStringExtra("totalclasses").split(",")));
            attended=new ArrayList(Arrays.asList(getIntent().getStringExtra("attended").split(",")));
            percent=new ArrayList(Arrays.asList(getIntent().getStringExtra("percent").split(",")));
            Log.d("len",String.valueOf(rollsno.size()));
            createtable();

            Log.d("creating table", "hi");


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    private TextView createTextView(String text) {
        TextView textView = new TextView(getApplicationContext());
        // Set text view properties (color, size, etc.)

        textView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 8);
        textView.setTypeface(Typeface.SERIF, Typeface.BOLD);
        textView.setGravity(Gravity.LEFT);
        textView.setText(text);

        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        ViewGroup.LayoutParams params = textView.getLayoutParams();

// Check if the layout params are of the correct type (e.g., MarginLayoutParams for margins)
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;

            // Set margins in pixels (adjust as needed)
            marginParams.leftMargin = 10;
            marginParams.topMargin = 10;
            marginParams.rightMargin = 10;
            marginParams.bottomMargin = 10;

            // Apply the modified layout params
            textView.setLayoutParams(marginParams);
        }
        return textView;
    }
    void createtable() {
        TableLayout textView = (TableLayout) findViewById(R.id.tl1);

        final TableRow row = new TableRow(getApplicationContext());

        // Create and add text views for each header column
        TextView rollnoTextView = createTextView("rollno");
        TextView totalClassesTextView = createTextView(" total classes ");
        TextView classesPresentTextView = createTextView("classes present ");
        TextView percentageTextView = createTextView("percentage");
        row.setBackgroundColor(Color.rgb(92,206,113));
        row.addView(rollnoTextView);
        row.addView(totalClassesTextView);
        row.addView(classesPresentTextView);
        row.addView(percentageTextView);

        textView.addView(row);

        int Row=percent.size();
        for (int i = 0; i < Row; i++) {

            final TableRow rows = new TableRow(getApplicationContext());
            if (i % 2 == 0) {
                rows.setBackgroundColor(Color.BLUE);
            } else {
                rows.setBackgroundColor(Color.rgb(92,166,206));
            }
            TextView rollnoTextView2 = createTextView(rollsno.get(i).replace('"',' '));
            TextView totalClassesTextView2 = createTextView(totalclasses.get(i));
            TextView classesPresentTextView2 = createTextView(attended.get(i));
            TextView percentageTextView2 = createTextView(percent.get((i)).substring(0,percent.get((i)).length()>4?4:percent.get((i)).length()));

            rows.addView(rollnoTextView2);
            rows.addView(totalClassesTextView2);
            rows.addView(classesPresentTextView2);
            rows.addView(percentageTextView2);

            textView.addView(rows);




        }
    }







}