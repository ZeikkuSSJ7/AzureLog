package com.zeikkussj.azurelog.search;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.game.Game;
import com.zeikkussj.azurelog.game.GameInfoActivity;
import com.zeikkussj.azurelog.util.JSONHandler;
import com.zeikkussj.azurelog.util.StaticFields;
import com.zeikkussj.azurelog.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class GameSearchActivity extends AppCompatActivity {
    private SearchView svQuery;
    private LinearLayout llResults;
    private RelativeLayout rlParent;
    private FloatingActionButton addNewGame;
    public static ArrayList<String[]> urls;
    public static String nextPage;
    boolean activityOpened;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_search);
        svQuery = findViewById(R.id.svQuery);
        llResults = findViewById(R.id.searchResult);
        rlParent = findViewById(R.id.rlParent);
        addNewGame = findViewById(R.id.addNewGame);
        urls = new ArrayList<>();
        addNewGame.setOnClickListener(v -> {
            Intent i = new Intent(this, NewGameActivity.class);
            startActivity(i);
        });
        svQuery.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                llResults.removeAllViews();
                search(svQuery.getQuery().toString());
                svQuery.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        activityOpened = true;
    }

    /**
     * Busca la cadena indicada en el servidor externo de TheGamesDB y va incluyendo en un
     * <code>LinearLayout</code> los resultados
     * @param search la cadena a buscar, normalmente el nombre de un juego
     */
    private void search(String search) {
        new Thread(() -> {
            urls.clear();
            RelativeLayout progress = Util.openProgressBar(GameSearchActivity.this, rlParent);
            ArrayList<Game> games = JSONHandler.getGamesFromExternalDB(GameSearchActivity.this, search);
            for (int i = 0; i < games.size(); i++) {
                Game currentGame = games.get(i);
                RelativeLayout rl = new RelativeLayout(GameSearchActivity.this);
                rl.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
                RelativeLayout gameCard = (RelativeLayout) getLayoutInflater().inflate(R.layout.game_card, rl, true);
                String[] covers = urls.get(i);
                if (covers != null) {
                    for (int j = 0; j < 4; j++) {
                        try {
                            currentGame.setCover(covers[j]);
                            URLConnection urlConnection = new URL(currentGame.getCover()).openConnection();
                            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                            addCover(urlConnection.getInputStream(), gameCard);
                            break;
                        } catch (UnknownHostException e) {
                            addViewToResults(Util.errorText(GameSearchActivity.this, getResources().getString(R.string.errorConnectToServer)));
                            e.printStackTrace();
                        } catch (IOException e) {
                            addViewToResults(Util.errorText(GameSearchActivity.this, getResources().getString(R.string.errorUnknown)));
                            e.printStackTrace();
                        }
                    }
                }
                addGameDescriptors(games.get(i), gameCard);
                Util.closeProgressBar(GameSearchActivity.this, progress);
                rl.setOnClickListener(v -> {
                    Intent intent = new Intent(GameSearchActivity.this, GameInfoActivity.class);
                    intent.putExtra(StaticFields.KEY_INFO_DATA, currentGame.toArray());
                    startActivity(intent);
                });
                runOnUiThread(() -> llResults.addView(rl));
            }
            Util.closeProgressBar(GameSearchActivity.this, progress);
            loadMoreItems(nextPage, nextPage == null);
        }).start();
    }

    /**
     * Descarga la carátula del juego encontrado
     * @param url la URL del juego
     * @param card la View donde está la <code>ImageView</code>
     */
    private void addCover(InputStream url, View card){
        ImageView iv = card.findViewById(R.id.ivCover);
        Drawable d = Drawable.createFromStream(url, "cover");
        runOnUiThread(() -> iv.setImageDrawable(d));
    }

    /**
     * Añade el nombre, fecha de salida y plataforma a la <code>View</code> indicada
     * @param game el juego a describir
     * @param card la <code>View</code> a modificar
     */
    private void addGameDescriptors(Game game, View card){
        TextView[] tvs = {card.findViewById(R.id.tvQueryTitle), card.findViewById(R.id.tvQueryRelease), card.findViewById(R.id.tvQueryPlatform)};
        runOnUiThread(() -> {
            tvs[0].setText(game.getName());
            tvs[1].setText(game.getRelease_date());
            tvs[2].setText(game.getPlatform());
        });
    }

    /**
     * Añade a la lista el juego
     * @param v la <code>View</code> a añadir
     */
    private void addViewToResults(View v){
        runOnUiThread(() -> llResults.addView(v));
    }

    /**
     * Elije según sea el mensaje a mostrar al cargar todos los juegos de una búsqueda
     * @param url la URL, en caso de existir, de la siguiente página de resultados
     * @param flagEndOfResults si se ha llegado al final
     */
    private void loadMoreItems(String url, boolean flagEndOfResults){
        if (!flagEndOfResults){
            TextView tv = format(new TextView(this), getString(R.string.loadMoreItems));
            tv.setOnClickListener(v -> {
                llResults.removeViewAt(llResults.getChildCount() - 1);
                search(url);
            });
            addViewToResults(tv);
        } else if (llResults.getChildCount() == 0) {
            addViewToResults(format(new TextView(this), getString(R.string.noResults)));
        } else {
            addViewToResults(format(new TextView(this), getString(R.string.noMoreResults)));
        }

    }

    /**
     * Crea una <code>TextView</code> a medida con el texto indicado
     * @param tv la vista a modificar
     * @param text el texto a incluir
     * @return la <code>View</code> modificada
     */
    private TextView format(TextView tv, String text){
        tv.setText(text);
        tv.setTextSize(16);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setPadding(10, 10, 10,10);
        return tv;
    }

    @Override
    protected void onResume() {
        // Al volver a la lista de juegos, no abrir teclado para buscar.
        if (activityOpened) {
            svQuery.setFocusable(true);
            svQuery.setIconified(false);
            activityOpened = false;
        } else {
            svQuery.clearFocus();
        }
        super.onResume();
    }
}
