package com.zeikkussj.azurelog.activities;

import android.widget.GridView;
import android.widget.RelativeLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.game.Game;
import com.zeikkussj.azurelog.game.GameAdapter;
import com.zeikkussj.azurelog.util.ListenerHandler;
import com.zeikkussj.azurelog.util.Util;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

abstract class LoadClass {
    private static FloatingActionButton fab1;
    private static FloatingActionButton fab2;
    private static FloatingActionButton fab3;
    private static boolean isFabOpen = false;

    /**
    * Crea el GridView del Activity según la categoría que se esté
    * requeriendo. Activa tambíen todos los listeners de cada juego
    * y los FAB con los métodos {@link #showFabs()} y {@link #closeFabs()}
     * @param activity el Activity usado
     * @param tableGrid el GridView al que aplicar los cambios
    */
    static void createGrid(AppCompatActivity activity, GridView tableGrid){
        RelativeLayout rlWrapper = activity.findViewById(R.id.gridWrapper);
        RelativeLayout pb = Util.openProgressBar(activity, rlWrapper);
        FloatingActionButton fab = rlWrapper.findViewById(R.id.fabExpand);
        Util.openToolbar(activity, R.id.my_toolbar);
        fab1 = rlWrapper.findViewById(R.id.fab1);
        fab2 = rlWrapper.findViewById(R.id.fab2);
        fab3 = rlWrapper.findViewById(R.id.fab3);
        int numberOfColumns = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(activity).getString("gridNumberOfColumns", "3"));
        activity.runOnUiThread(() -> {
            tableGrid.setNumColumns(numberOfColumns);
            closeFabs();
        });
        fab.setOnClickListener(v -> {
            if (!isFabOpen)
                showFabs();
            else
                closeFabs();
        });
        ArrayList<Game> games = Util.getDataFromDB(activity, Util.FLAG_DEFAULT_QUERY);
        GameAdapter gameAdapter = new GameAdapter(activity, games, activity.getWindowManager().getDefaultDisplay());
        ListenerHandler.setGridFABListeners(activity, gameAdapter, games, fab1, fab2, fab3);
        activity.runOnUiThread(() -> tableGrid.setAdapter(gameAdapter));
        tableGrid.setOnItemClickListener(ListenerHandler.onClickGame(activity, games));
        tableGrid.setOnItemLongClickListener(ListenerHandler.onLongClickGame(activity, gameAdapter, games));
        Util.closeProgressBar(activity, pb);
    }

    /**
     * Muestra los tres FABs pequeños localizados debajo del grande
     */
    private static void showFabs(){
        isFabOpen = true;
        fab1.animate().translationY(-140);
        fab2.animate().translationY(-100).translationX(-100);
        fab3.animate().translationX(-140);
    }

    /**
     * Esconde los tres FABs pequeños debajo del grande
     */
    private static void closeFabs(){
        isFabOpen = false;
        fab1.animate().translationY(0);
        fab2.animate().translationY(0).translationX(0);
        fab3.animate().translationX(0);
    }
}
