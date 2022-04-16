package com.planit.opencv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.planit.opencv.databinding.ActivityMainBinding;

import org.opencv.android.CameraActivity;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    static{
        if(OpenCVLoader.initDebug()){
            Log.d("MainActivity", "OpenCV is Loaded");
        }else{
            Log.d("MainActivity", "OpenCV is not Loaded");
        }
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnOpenCamera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CameraActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

    }
}