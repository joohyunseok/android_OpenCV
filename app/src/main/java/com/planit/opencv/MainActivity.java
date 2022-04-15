package com.planit.opencv;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    static{
        if(OpenCVLoader.initDebug()){
            Log.d("MainActivity", "OpenCV is Loaded");
        }else{
            Log.d("MainActivity", "OpenCV is not Loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}