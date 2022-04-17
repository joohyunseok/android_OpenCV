package com.planit.opencv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.planit.opencv.databinding.ActivityCameraBinding;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";

    private ActivityCameraBinding binding;
    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase mOpenCvCameraView;
    private int mCamearId = 0; //기본 카메라 전면카메라로 정의
    private int take_image = 0; //촬영 Flag - if 1 : take picture , if 0 : waiting

    private BaseLoaderCallback mLoderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case LoaderCallbackInterface
                        .SUCCESS:{
                    Log.i(TAG, "OpenCV is Loaded");
                    mOpenCvCameraView.enableView();
                }
                default:
                {
                    super.onManagerConnected(status);
                }
                break;
            }

        }
    };

    public CameraActivity () {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int MY_PERMISSIONS_REQUEST_CAMERA = 0;
        if(ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
        if(ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
        if(ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //CameraBridgeViewBase로 변형을 위해 binding 사용 x 안그럼 오류
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.frame_Surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        // Click Event
        // 카메라 전환 클릭 시
        binding.ivFlipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapCamera();
            }
        });
        // 카메라 촬영 클릭 시
        binding.ivTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(take_image==0){
                    take_image=1;
                }else {
                    take_image=0;
                }
            }
        });
    }

    private void swapCamera() {
        // Change mCameraId
        // if 0 change it to 1
        // if 1 change it to 0
        // xor연산자
        mCamearId=mCamearId^1;
        // 현재 카메라 오프
        mOpenCvCameraView.disableView();
        // 새로운 카메라 세팅
        mOpenCvCameraView.setCameraIndex(mCamearId);
        // 새로운 카메라 실행
        mOpenCvCameraView.enableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV initailization is done");
            mLoderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            Log.d(TAG, "OpenCv is not Loaded, try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this, mLoderCallback);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(mOpenCvCameraView!= null){
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height,width, CvType.CV_8UC4);
        mGray = new Mat(height,width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        //카메라 전면 카메라로 전환시 화면 180도 변환 되는 문제를 해결 하기 위해 아래 코드 작성
        if(mCamearId==1){
            Core.flip(mRgba,mRgba, -1);
            Core.flip(mGray,mGray, -1);
        }

        // when we take picture
        // if input 1 -> output 0
        // so far next frame input will be 0
        // it will take only one frame to save it
        take_image=take_picture_function_rgb(take_image, mRgba);

        return mRgba;
    }

    private int take_picture_function_rgb(int take_image, Mat mRgba) {
        if(take_image==1){
            OutputStream fos = null;
            // Create Bitmap to save it in Pictures folder of gallery

            Bitmap save_Bitmap= Bitmap.createBitmap(mRgba.width(), mRgba.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mRgba, save_Bitmap);

            // Make File Format and FileName
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String currentDateAndTime = sdf.format(new Date());
            String fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + currentDateAndTime + ".jpg";

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){

                ContentResolver resolver = getContentResolver();

                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, currentDateAndTime + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                contentValues.put(MediaStore.MediaColumns.WIDTH, save_Bitmap.getWidth());
                contentValues.put(MediaStore.MediaColumns.HEIGHT, save_Bitmap.getHeight());
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                try {
                    fos = resolver.openOutputStream(imageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                save_Bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            }
            // Convert Image to BGRA to save
            Mat save_mat= new Mat();
            Core.flip(mRgba.t(), save_mat, 1);
            Imgproc.cvtColor(save_mat,save_mat,Imgproc.COLOR_RGBA2BGRA);

            // Check Folder Directory
            File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/");
            boolean success = true;
            if(!folder.exists()){
                success=folder.mkdirs();
            }



            // Save File
            boolean save_photo_flag = true;
            save_photo_flag = Imgcodecs.imwrite(fileName, save_mat);

            //reset take_image to wait
            take_image=0;
        }
        return take_image;
    }
}