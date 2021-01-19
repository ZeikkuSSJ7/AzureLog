package com.zeikkussj.azurelog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.zeikkussj.azurelog.activities.PlayingActivity;
import com.zeikkussj.azurelog.game.GameDbHelper;
import com.zeikkussj.azurelog.util.Util;

import java.io.IOException;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        new Thread(() ->{
            GameDbHelper.getInstance(this);
            try {
                Util.initDB(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(this, PlayingActivity.class);
            startActivity(intent);
            finish();
        }).start();

    }
}
