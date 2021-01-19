package com.zeikkussj.azurelog.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.game.GameAdapter;
import com.zeikkussj.azurelog.game.GameDbHelper;
import com.zeikkussj.azurelog.util.Util;

public class OnHoldActivity extends AppCompatActivity {
    private GridView tableGrid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_grid_no_drawer);
        tableGrid = findViewById(R.id.grid);
        new Thread(() -> LoadClass.createGrid(OnHoldActivity.this, tableGrid)).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Util.onCreateOptionsMenu(this, menu, tableGrid);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Util.onOptionItemSelected(item, this, null);
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Util.onActivityResult((GameAdapter) tableGrid.getAdapter(), requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        GameDbHelper.getInstance(this).close(); // Cerrar la base de datos para actualizar cambios
        super.onPause();
    }
}
