package com.zeikkussj.azurelog.developer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.game.Game;
import com.zeikkussj.azurelog.game.GameConstants;
import com.zeikkussj.azurelog.game.GameDbHelper;
import com.zeikkussj.azurelog.util.BackupAlarmHandler;
import com.zeikkussj.azurelog.util.OnThisDayAlarmHandler;
import com.zeikkussj.azurelog.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DeveloperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);
    }

    public void testMethod(View view) {
        // some method
    }

    public void testOnThisDay(View view) {
        OnThisDayAlarmHandler otdah = new OnThisDayAlarmHandler();
        otdah.onReceive(this, new Intent());
    }

    public void testBackup(View view) {
        BackupAlarmHandler bah = new BackupAlarmHandler();
        bah.onReceive(this, new Intent());
    }

}
