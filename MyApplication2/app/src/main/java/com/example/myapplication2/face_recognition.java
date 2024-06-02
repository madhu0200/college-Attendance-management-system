package com.example.myapplication2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.face.FaceDetector;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.example.myapplication2.commonclass;

public class face_recognition extends AppCompatActivity {
    ExecutorService service;
    private static final String TAG = "FACE_DETECT_TAG";

    // This factor is used to make the detecting image smaller, to make the process faster
    private static final int SCALING_FACTOR = 10;
    public ProgressBar progressBar;


    public static int i=0;

    private FaceDetector detector;
    ImageButton  capture,toggleFlash, flipCamera;
    Button button;
    PreviewView previewView;
    public static int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Toast.makeText(this,"starting camera1",Toast.LENGTH_SHORT).show();
            startCamera(cameraFacing);

        }
    });
    public boolean gotallpermissions()
    {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.INTERNET);

        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.CAMERA);

        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activityResultLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
        else
            activityResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(getApplicationContext(),getIntent().getStringExtra("classFolder")+getIntent().getStringExtra("studentfolder"),Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        i=0;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
        previewView = findViewById(R.id.viewFinder);
        capture = findViewById(R.id.capture);



        toggleFlash = findViewById(R.id.toggleFlash);

        flipCamera = findViewById(R.id.flipCamera);
        if(gotallpermissions())
            startCamera(cameraFacing);
        else
            gotallpermissions();





        flipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
                    cameraFacing = CameraSelector.LENS_FACING_FRONT;
                } else {
                    cameraFacing = CameraSelector.LENS_FACING_BACK;
                }
                startCamera(cameraFacing);
            }
        });
    }



    ///connecting to server



    // storing image





    //cropping the detecting image




    //detecting the face
    public boolean analyzePhoto(String path)
    {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Bitmap smallerBitmap = Bitmap.createScaledBitmap(
                bitmap,
                bitmap.getWidth()/SCALING_FACTOR,
                bitmap.getHeight()/SCALING_FACTOR,
                false);
        InputImage inputImage = InputImage.fromBitmap(smallerBitmap, 0);

        //start detection process
        detector.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {

                        //There can be multiple faces detected from an image, manage them using loop from List<Face> faces
                        Log.d(TAG, "onSuccess: No of faces detected: "+faces.size());
                        if(faces.size()>0) {
                            File file=new File(path);
                            try {
                                connectServer(getIntent().getStringExtra("email"),file);
                            } catch (IOException e) {
                                Log.d("error",e.getMessage());
                            }

                        }

                        else
                        {
                            Toast.makeText(getApplicationContext(),"face is not detected",Toast.LENGTH_SHORT).show();
                            String path =getExternalFilesDir(null).toString();
                            Log.d("Files", "Path: " + path);
                            File directory = new File(path);
                            File[] files = directory.listFiles();
                            Log.d("Files", "Size: "+ files.length);
                            for (int i = 0; i < files.length; i++) {
                                deleteFile(  path +"/"+ files[i].getName());
                            }
                            Intent intent=new Intent(getApplicationContext(), mainmenu.class);
                            intent.putExtra("classFolder",getIntent().getStringExtra("classFolder"));
                            intent.putExtra("studentfolder",getIntent().getStringExtra("studentfolder"));
                            intent.putExtra("email",getIntent().getStringExtra("email"));
                            intent.putExtra("password",getIntent().getStringExtra("password"));
                            startActivity(intent);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                                          @Override
                                          public void onFailure(@NonNull Exception e) {
                                              //Detection failed
                                              Log.e(TAG, "onFailure: ", e);
                                              Toast.makeText(getApplicationContext(), "Detection failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                              deleteFile(path);
                                              String path =getExternalFilesDir(null).toString();
                                              Log.d("Files", "Path: " + path);
                                              File directory = new File(path);
                                              File[] files = directory.listFiles();
                                              Log.d("Files", "Size: "+ files.length);
                                              for (int i = 0; i < files.length; i++) {
                                                  deleteFile(  path +"/"+ files[i].getName());
                                              }
                                              Intent intent=new Intent(getApplicationContext(), mainmenu.class);
                                              intent.putExtra("classFolder",getIntent().getStringExtra("classFolder"));
                                              intent.putExtra("studentfolder",getIntent().getStringExtra("studentfolder"));
                                              intent.putExtra("email",getIntent().getStringExtra("email"));
                                              intent.putExtra("password",getIntent().getStringExtra("password"));
                                              startActivity(intent);
                                          }
                                      }
                );
        //deleteFile(path);
        return true;
    }



    //deleting the image
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



    //detecting the face
    public boolean detect_face(String path)
    {
        FaceDetectorOptions realTimeFdo =
                new FaceDetectorOptions.Builder()
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .build();

        //init FaceDetector obj
        detector = FaceDetection.getClient(realTimeFdo);
        try {

            if(analyzePhoto(path))
            {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    //capturing image
    public boolean imageCapture(ImageCapture imageCapture)
    {

        //capture.setImageResource(R.drawable.baseline_stop_circle_24);
        final File file;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            file=new File(getFilesDir(),System.currentTimeMillis() + ".jpg");
        }
        else {
            file = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
        }
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
        imageCapture.takePicture(outputFileOptions, Executors.newCachedThreadPool(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(detect_face(file.getPath()) ){
                            //     Toast.makeText(MainActivity.this, "Image saved at: " + file.getPath(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                //  startCamera(cameraFacing);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Failed to save: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                //   startCamera(cameraFacing);
            }
        });
      //  deleteFile(file.getPath());
        return true;
    }

    private void setFlashIcon(Camera camera) {
        if (camera.getCameraInfo().hasFlashUnit()) {
            if (camera.getCameraInfo().getTorchState().getValue() == 0) {
                camera.getCameraControl().enableTorch(true);
                toggleFlash.setImageResource(R.drawable.baseline_flash_off_24);
            } else {
                camera.getCameraControl().enableTorch(false);
                toggleFlash.setImageResource(R.drawable.baseline_flash_on_24);
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Flash is not available currently", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private int aspectRatio(int width, int height) {
        double previewRatio = (double) Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }


    public void startCamera(int cameraFacing) {

        int aspectRatio = aspectRatio(previewView.getWidth(), previewView.getHeight());
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);

        listenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider) listenableFuture.get();

                Preview preview = new Preview.Builder().setTargetAspectRatio(aspectRatio).build();

                ImageCapture imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                cameraProvider.unbindAll();

                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                capture.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {



                                imageCapture(imageCapture);


                    }
                });
                toggleFlash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setFlashIcon(camera);
                    }
                });

                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

    }

    private void toggleFlash(Camera camera) {
        if (camera.getCameraInfo().hasFlashUnit()) {
            if (camera.getCameraInfo().getTorchState().getValue() == 0) {
                camera.getCameraControl().enableTorch(true);
                toggleFlash.setImageResource(R.drawable.baseline_flash_off_24);
            } else {
                camera.getCameraControl().enableTorch(false);
                toggleFlash.setImageResource(R.drawable.baseline_flash_on_24);
            }
        } else {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Flash is not available currently", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        service.shutdown();
    }
    void connectServer(String dbname,File picturePath) throws IOException {
        Log.d("state","in connect server");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath.getPath());
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        String[] classFolders = getIntent().getStringExtra("classFolder").split("-");
   //     String[] studentfolders = getIntent().getStringExtra("studentfolder").split("-");
        String department = classFolders[0];
        String year = classFolders[1];
        String section = classFolders[2];


        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        RequestBody requestBody = null;
        if (department != null && year != null && section != null ) {
            requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("dbname",dbname)
                    .addFormDataPart("classFolder", Objects.requireNonNull(getIntent().getStringExtra("classFolder")))
                    .addFormDataPart("name", picturePath.getName())
                    .addFormDataPart("image", encodedImage)
                   // .addFormDataPart("studentfolder",getIntent().getStringExtra("studentfolder"))
                    .build();
        }

        Request request = new Request.Builder()
                .url(commonclass.url+"/recognize")//http://192.168.0.128:8000/face
                .post(Objects.requireNonNull(requestBody))
                .build();
        try {

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            //client.connectTimeoutMillis(60, TimeUnit.SECONDS);
            Response response = client.newCall(request).execute();
           // Toast.makeText(getApplicationContext(),response.body().toString(),Toast.LENGTH_SHORT).show();
            Log.d("response",response.toString());

            if(response.code()==200)
            {
                connectServer();
            }
            deleteFile(picturePath.getPath());
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "poor internet", Toast.LENGTH_SHORT).show();
            deleteFile(picturePath.getPath());
        }
    }
    void connectServer() throws IOException {
        String str=commonclass.url+"recognize";
        URLConnection urlConn = null;
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(str);
            urlConn = url.openConnection();
            bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuffer.append(line);
            }
            JSONObject jObject  = new JSONObject(stringBuffer.toString());
            JSONObject faces=(JSONObject)jObject.get("message");
            Iterator iterator  = faces.keys();
            String res="detected: ";
            String key = null;
            while(iterator.hasNext()){
                key = (String)iterator.next();
                Log.d("inval value: ",faces.get(key).toString());
                res+=faces.get(key).toString()+" ";
            }
            Toast.makeText(getApplicationContext(),res,Toast.LENGTH_SHORT).show();




            Log.d("ans",stringBuffer.toString());
        }
        catch(Exception ex)
        {
            Log.e("App", "yourDataTask", ex);

        }
        finally
        {
            if(bufferedReader != null)
            {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
