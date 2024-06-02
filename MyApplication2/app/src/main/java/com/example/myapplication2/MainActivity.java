package com.example.myapplication2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.face.FaceDetector;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetectorOptions;


public class MainActivity extends AppCompatActivity {
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
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
           // Toast.makeText(this,"starting camera1",Toast.LENGTH_SHORT).show();
           startCamera(cameraFacing);

        }
    });
    public boolean gotallpermissions()
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.INTERNET);

        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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

        progressBar=findViewById(R.id.progress_bar);
        progressBar.setProgress(0);
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
    private boolean storeImage(Bitmap image) throws IOException {
        image=Bitmap.createScaledBitmap(image, 128, 128, true);
        File pictureFile = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
        progressBar.setProgress(progressBar.getProgress()+2);
        if(progressBar.getProgress()>=100)
        {
            progressBar.setProgress(0);
            Intent intent=new Intent(getApplicationContext(), MainActivity2.class);
            intent.putExtra("classFolder",getIntent().getStringExtra("classFolder"));
            intent.putExtra("studentfolder",getIntent().getStringExtra("studentfolder"));
            intent.putExtra("email",getIntent().getStringExtra("email"));
            intent.putExtra("password",getIntent().getStringExtra("password"));
            startActivity(intent);
        }

        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return false;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
            return false;

        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
            return false;

        }

       // connectServer(pictureFile);
        return true;
    }




    //cropping the detecting image
    public boolean cropDetectedFaces(Bitmap bitmap, List<Face> faces) throws IOException {
        Rect rect = faces.get(0).getBoundingBox(); //show second face

        int x = Math.max(rect.left, 0);
        int y = Math.max(rect.top, 0);
        int width = rect.width();
        int height = rect.height();

        Bitmap croppedBitmap = Bitmap.createBitmap(
                bitmap,
                x,
                y,
                (x + width > bitmap.getWidth()) ? bitmap.getWidth() - x : width,
                (y + height > bitmap.getHeight()) ? bitmap.getHeight() - y : height
        );
        if(storeImage(croppedBitmap))
            return true;
        return false;
        //set the cropped bitmap to image view

    }



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
                        if(faces.size() == 1) {
                            for (Face face : faces) {
                                //Get detected face as rectangle
                                Rect rect = face.getBoundingBox();
                                rect.set(rect.left * SCALING_FACTOR,
                                        rect.top * (SCALING_FACTOR - 1),
                                        rect.right * SCALING_FACTOR,
                                        (rect.bottom * SCALING_FACTOR) + 90
                                );

                            }


                            try {
                                cropDetectedFaces(bitmap, faces);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        }
                        else if(faces.size()>1)
                        {
                            Toast.makeText(MainActivity.this,"multiple face is  detected",Toast.LENGTH_SHORT).show();
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
                        else
                        {
                            Toast.makeText(MainActivity.this,"face is not detected",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this, "Detection failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        deleteFile(path);
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
        progressBar.setVisibility(View.VISIBLE);
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
                        Toast.makeText(MainActivity.this, "Failed to save: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
             //   startCamera(cameraFacing);
            }
        });
       deleteFile(file.getPath());
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
                    Toast.makeText(MainActivity.this, "Flash is not available currently", Toast.LENGTH_SHORT).show();
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
                        String path =getExternalFilesDir(null).toString();
                        Log.d("Files", "Path: " + path);
                        File directory = new File(path);
                        File[] files = directory.listFiles();
                        if(files.length>=50)
                        {
                            Intent intent=new Intent(getApplicationContext(), MainActivity2.class);
                            intent.putExtra("department",getIntent().getStringExtra("department"));
                            intent.putExtra("year",getIntent().getStringExtra("year"));
                            intent.putExtra("section",getIntent().getStringExtra("section"));
                            intent.putExtra("pinno",getIntent().getStringExtra("pinno"));
                            intent.putExtra("name",getIntent().getStringExtra("name"));
                            startActivity(intent);
                        }
                        else {
                            for (int i = 0; i < 50; i++) {
                                imageCapture(imageCapture);
                            }
                        }
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
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Flash is not available currently", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        service.shutdown();
    }

}
