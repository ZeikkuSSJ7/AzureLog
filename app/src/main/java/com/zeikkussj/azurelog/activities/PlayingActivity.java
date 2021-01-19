package com.zeikkussj.azurelog.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import com.google.android.material.navigation.NavigationView;
import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.game.GameAdapter;
import com.zeikkussj.azurelog.game.GameDbHelper;
import com.zeikkussj.azurelog.util.JSONHandler;
import com.zeikkussj.azurelog.util.StaticFields;
import com.zeikkussj.azurelog.util.Util;

import java.io.File;

public class PlayingActivity extends AppCompatActivity {
    private GridView tableGrid;
    private NavigationView nav;
    private long tiempoDeInicio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tiempoDeInicio = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_grid);
        Util.createNotificationChannel(this);
        if (!new File(getFilesDir() + StaticFields.JSON_DATA_DIRECTORY).exists())
            JSONHandler.makeUser(this, this);
        nav = findViewById(R.id.nav);
        tableGrid = findViewById(R.id.grid);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Util.onCreateOptionsMenu(this, menu, tableGrid);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Util.onOptionItemSelected(item, this, findViewById(R.id.drawer));
        return false;
    }

    @Override
    protected void onResume() {
        // Recarga la lista de juegos
        new Thread(() -> {
            Util.setNavItems(nav, PlayingActivity.this);
            Util.createOnThisDayJob(PlayingActivity.this);
            Util.createBackupJob(PlayingActivity.this);
            LoadClass.createGrid(PlayingActivity.this, tableGrid);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.hamburger);
        }).start();
        super.onResume();
    }

    @Override
    protected void onStart() {
        Log.d("Response Time", String.valueOf(System.currentTimeMillis() - tiempoDeInicio));
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Util.onActivityResult((GameAdapter) tableGrid.getAdapter(), requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        GameDbHelper.getInstance(this).close();
        super.onPause();
    }
}
