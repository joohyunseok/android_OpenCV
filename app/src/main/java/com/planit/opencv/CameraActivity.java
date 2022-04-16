package com.planit.opencv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.planit.opencv.databinding.ActivityCameraBinding;
import com.planit.opencv.databinding.ActivityMainBinding;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";

    private ActivityCameraBinding binding;
    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase mOpenCvCameraView;
    //기본 카메라 전면카메라로 정의
    private int mCamearId = 0;
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
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.frameSurface.setVisibility(SurfaceView.VISIBLE);
        binding.frameSurface.setCvCameraViewListener(this);

        // Click Event : Image View
        binding.ivFlipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapCamera();
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
        if(!OpenCVLoader.initDebug()){
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

        //카메라 전면 카메라로 전환시 화면 180도 변환 되는 문제를 해결하기 위해 아래 코드 작성
        if(mCamearId==1){
            Core.flip(mRgba,mRgba, -1);
            Core.flip(mGray,mGray, -1);
        }
        return mRgba;
    }
}