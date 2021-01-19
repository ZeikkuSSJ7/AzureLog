package com.zeikkussj.azurelog.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.game.Game;
import com.zeikkussj.azurelog.game.GameAdapter;
import com.zeikkussj.azurelog.game.GameDbHelper;
import com.zeikkussj.azurelog.util.ListenerHandler;
import com.zeikkussj.azurelog.util.StaticFields;
import com.zeikkussj.azurelog.util.Util;

import java.util.ArrayList;

public class AdvancedSearchResultsActivity extends AppCompatActivity {
    private GridView tableGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_search_results);
        tableGrid = findViewById(R.id.grid);
        Util.openToolbar(this, R.id.my_toolbar);
        String sql = getIntent().getStringExtra(StaticFields.KEY_INFO_DATA);
        new Thread(() -> {
            ArrayList<Game> games = GameDbHelper.getInstance(this).customQuery(sql);
            int numberOfColumns = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("gridNumberOfColumns", "3"));
            runOnUiThread(() -> tableGrid.setNumColumns(numberOfColumns));
            GameAdapter gameAdapter = new GameAdapter(this, games, getWindowManager().getDefaultDisplay());
            runOnUiThread(() -> tableGrid.setAdapter(gameAdapter));
            tableGrid.setOnItemClickListener(ListenerHandler.onClickGame(this, games));
            tableGrid.setOnItemLongClickListener(ListenerHandler.onLongClickGame(this, gameAdapter, games));
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Util.onCreateOptionsMenu(this, menu, tableGrid);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Util.onOptionItemSelected(item, this, null);
        return super.onOptionsItemSelected(item);
    }
}
