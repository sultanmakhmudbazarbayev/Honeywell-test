package com.honeywell.bccrfid.test;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.honeywell.bccrfid.R;

public class BlankActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blank_layout);
        Intent intent = getIntent();
        if (intent != null) {
            try {

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
