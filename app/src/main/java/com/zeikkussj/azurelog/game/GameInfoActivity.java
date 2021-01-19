package com.zeikkussj.azurelog.game;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.game.GameConstants.GameEntry;
import com.zeikkussj.azurelog.util.ImageHandler;
import com.zeikkussj.azurelog.util.JSONHandler;
import com.zeikkussj.azurelog.util.ListenerHandler;
import com.zeikkussj.azurelog.util.StaticFields;
import com.zeikkussj.azurelog.util.Util;

import java.io.FileNotFoundException;
import java.io.IOException;

public class GameInfoActivity extends AppCompatActivity {
    private int gameDataTextCount;

    private ImageView cover;
    private RelativeLayout wrapper;
    private String[] gameData;
    private int originalStatus;
    private boolean isInDb;
    private TextView tvName;
    private TextView tvPlatform;
    private TextView tvDeveloper;
    private TextView tvPublisher;
    private TextView tvReleaseDate;
    private TextView tvDescription;
    private TextView tvGenres;
    private TextView tvRating;
    private TextView tvStartDate;
    private TextView tvFinishDate;
    private TextView tvPlaytime;
    private CheckBox cbReplaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_info);
        cover = findViewById(R.id.rlCover);
        wrapper = findViewById(R.id.wrapper);
        gameData = getIntent().getStringArrayExtra(StaticFields.KEY_INFO_DATA);
        gameDataTextCount = 2;
        initialize();
        wrapper.setVisibility(View.GONE);
        tvName.setVisibility(View.GONE);
        obtenerDatosJuego();
        originalStatus = Integer.parseInt(gameData[11]);
    }

    /**
     * Inicializa los apuntadores del Activity
     */
    private void initialize() {
        tvName = findViewById(R.id.tvTitle);
        tvPlatform = findViewById(R.id.tvPlatform);
        tvDeveloper = findViewById(R.id.tvDeveloper);
        tvPublisher = findViewById(R.id.tvPublisher);
        tvReleaseDate = findViewById(R.id.tvRelease);
        tvDescription = findViewById(R.id.tvDescription);
        tvGenres = findViewById(R.id.tvGenres);
        tvRating = findViewById(R.id.tvRating);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvFinishDate = findViewById(R.id.tvFinishDate);
        tvPlaytime = findViewById(R.id.tvPlaytime);
        cbReplaying = findViewById(R.id.cbReplaying);
    }

    /**
     * Obtiene los datos del juego y los pone en las diferentes Views
     */
    public void obtenerDatosJuego() {
        ProgressBar pb = findViewById(R.id.progressLoadGame);
        new Thread(() -> {
            isInDb = checkForDB(gameData[0]);
            addTextToView();
            addGameCover(gameData[1], gameData[0]);
            runOnUiThread(() -> {
                pb.setVisibility(View.GONE);
                wrapper.setVisibility(View.VISIBLE);
                tvName.setVisibility(View.VISIBLE);
            });
        }).start();
    }

    /**
     * Comprueba si el juego recibido a través del {@link Intent} está en
     * la base de datos (es decir, comprueba si lo tienes ya o lo has buscado).
     * En caso de estar, se obtienen los datos específicos (puntuación, estado...)
     * con la función {@link #getSpecificDataFromDB(String[])}, si no está se añade
     * la opción de añadirlo con {@link #showAddGameOptions(String)}
     * @param id el id del juego
     * @return true si está, false en caso contrario
     */
    private boolean checkForDB(String id) {
        GameDbHelper db = GameDbHelper.getInstance(this);
        if (!db.isGameInDB(id)){
            showAddGameOptions(id);
            return false;
        } else {
            getSpecificDataFromDB(db.getOwnGameInfo(id).toArray());
            return true;
        }
    }

    /**
     * Incluye la opción de añadir el juego al Activity
     * @param id el id del juego a añadir
     */
    private void showAddGameOptions(String id) {
        RelativeLayout container = findViewById(R.id.rlDatabase);
        RelativeLayout view = (RelativeLayout) getLayoutInflater().inflate(R.layout.add_database, container, false);
        TextView tvAddButton = view.findViewById(R.id.tvAddButton);
        tvAddButton.setOnClickListener(v -> new Thread(() -> {
            runOnUiThread(container::removeAllViews);
            RelativeLayout progressBar = Util.openProgressBar(GameInfoActivity.this, container);
            try {
                ImageHandler.saveImage(gameData[1], getFilesDir() + "/" + id + StaticFields.DEFAULT_COVER_EXTENSION, cover, this);
            } catch (IOException e) {
                Toast.makeText(this, R.string.failedToDownCover, Toast.LENGTH_SHORT).show();
            }
            Game newGame = new Game(gameData);
            GameDbHelper.getInstance(this).insertGame(newGame);
            runOnUiThread(this::activateListeners);
            ListenerHandler.onCoverLongClick(this, cover, gameData[2], gameData[0]);
            getSpecificDataFromDB(newGame.toArray());
            Util.closeProgressBar(GameInfoActivity.this, progressBar);
        }).start());
        runOnUiThread(() -> container.addView(view));
    }

    /**
     * Comprueba si necesita descargar la imagen del juego o si ya está
     * en local para mostrarla en pantalla
     * @param url la URL, usada en caso de necesitarla
     * @param id el id del juego para conocer su dirección local
     */
    private void addGameCover(String url, String id){
        try {
            if (isInDb){
                ImageHandler.openImage(getFilesDir() + "/" + id + StaticFields.DEFAULT_COVER_EXTENSION, cover, this);
                ListenerHandler.onCoverLongClick(this, cover, gameData[2], gameData[0]);
            } else {
                Drawable d = ImageHandler.getImage(url);
                runOnUiThread(() -> cover.setImageDrawable(d));
            }
        } catch (IOException e) {
            Toast.makeText(this, R.string.failedToDownCover, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Coloca todos los datos del juego en sus respectivas Views
     * junto al método {@link #setViewText(TextView, int, int, int, String)}
     */
    private void addTextToView(){
        tvName.setText(gameData[gameDataTextCount++]);
        setViewText(tvPlatform, R.string.changePlatform, R.string.setPlatform, R.string.platformNotUpdated, GameEntry.PLATFORM);
        setViewText(tvDeveloper, R.string.changeDeveloper, R.string.setDeveloper, R.string.developerNotUpdated, GameEntry.DEVELOPER);
        setViewText(tvPublisher, R.string.changePublisher, R.string.setPublisher, R.string.publisherNotUpdated, GameEntry.PUBLISHER);
        setViewText(tvReleaseDate, 0,0,0, null);
        tvReleaseDate.setOnClickListener(ListenerHandler.onClickAlertDate(tvReleaseDate, 0, GameEntry.RELEASEDATE, this, gameData[2], gameData[3]));
        setViewText(tvDescription, R.string.changeDescription, R.string.setDescription, R.string.descriptionNotUpdated, GameEntry.DESCRIPTION);
        setViewText(tvRating, R.string.changeRating, R.string.setRating, R.string.ratingNotUpdated, GameEntry.RATING);
        setViewText(tvGenres, R.string.changeGenres, R.string.setGenres, R.string.genresNotUpdated, GameEntry.GENRES);
        runOnUiThread(() -> {
            if (!isInDb){
                tvStartDate.setText(getString(R.string.defaultStartDate, getString(R.string.notDefinedYet)));
                tvFinishDate.setText(getString(R.string.defaultFinishDate, getString(R.string.notDefinedYet)));
                tvPlaytime.setText(getString(R.string.defaultPlaytime, getString(R.string.notDefinedYet)));
            }
        });
    }

    /**
     * Coloca el texto en un <code>TextView</code> concreto
     * @param tv la <code>TextView</code> concreta
     * @param titleID el id del título de su <code>AlertDialog</code>
     * @param positiveButtonID el id de la afirmación de su <code>AlertDialog</code>
     * @param toastMessageID el id del mensaje marcado por negar el <code>AlertDialog</code>
     * @param columnName la columna a modificar
     */
    private void setViewText(TextView tv, int titleID, int positiveButtonID, int toastMessageID, String columnName){
        tv.setText(gameData[gameDataTextCount++]);
        setListener(tv, titleID, positiveButtonID, toastMessageID, columnName);
    }

    /**
     * Marca el listener de la <code>TextView</code> indicada
     * @param tv la <code>TextView</code> concreta
     * @param titleID el id del título de su <code>AlertDialog</code>
     * @param positiveButtonID el id de la afirmación de su <code>AlertDialog</code>
     * @param toastMessageID el id del mensaje marcado por negar el <code>AlertDialog</code>
     * @param columnName la columna a modificar
     */
    private void setListener(TextView tv, int titleID, int positiveButtonID, int toastMessageID, String columnName){
        tv.setOnClickListener(
                ListenerHandler.onClickUpdateText(this, tv, titleID, positiveButtonID, toastMessageID, columnName, true, gameData[2], gameData[3]));
        tv.setTag(tv.getText().toString());
        tv.setEnabled(isInDb);
    }

    /**
     * Modifica el layout para añadir los datos específico de un juego
     * @param specificData los datos específicos
     */
    public void getSpecificDataFromDB(String[] specificData) {
        TextView[] tvDataArray = new TextView[3];
        ImageView ivFavourite = findViewById(R.id.ivFavourite);
        RelativeLayout rlDatabase = findViewById(R.id.rlDatabase);
        View view = getLayoutInflater().inflate(R.layout.database_data, rlDatabase, false);
        tvDataArray[0] = view.findViewById(R.id.tvScore);
        tvDataArray[1] = view.findViewById(R.id.tvStatus);
        tvDataArray[2] = view.findViewById(R.id.tvComment);
        tvDataArray[0].setText(getString(R.string.score, specificData[10].equals("0") ? getString(R.string.unranked) : specificData[10]));
        tvDataArray[0].setTag(specificData[10]);
        tvDataArray[0].setOnClickListener(ListenerHandler.onClickChangeScoreStatusPlaytime(tvDataArray[0], R.array.validScoreOptions, getString(R.string.scoretype), GameEntry.SCORE, this, gameData[2], gameData[3]));
        tvDataArray[1].setText(getString(R.string.status, getResources().getStringArray(R.array.validStatusOptions)[Integer.parseInt(specificData[11])]));
        tvDataArray[1].setTag(Integer.parseInt(specificData[11]));
        tvDataArray[1].setOnClickListener(ListenerHandler.onClickChangeScoreStatusPlaytime(tvDataArray[1], R.array.validStatusOptions, getString(R.string.statustype), GameEntry.STATUS, this, gameData[2], gameData[3]));
        tvDataArray[2].setTag(specificData[12]);
        tvDataArray[2].setOnClickListener(ListenerHandler.onClickUpdateText(this, tvDataArray[2], R.string.gameProgressComment, R.string.saveProgressNote, R.string.commentNotUpdated, GameEntry.COMMENT, false, gameData[2], gameData[3]));
        String playtime = Double.parseDouble(specificData[16]) != 0 ? specificData[16] : getString(R.string.notDefinedYet);

        boolean isFavourite = specificData[13].equals("true");
        ivFavourite.setTag(isFavourite);
        checkFavourite(isFavourite, ivFavourite);
        runOnUiThread(() -> ivFavourite.setOnClickListener(v -> {
            ContentValues cv = new ContentValues();
            boolean newValue = !((boolean) ivFavourite.getTag());
            checkFavourite(newValue, ivFavourite);
            cv.put(GameEntry.FAVOURITE, newValue);
            GameDbHelper.getInstance(this).updateGame(cv, this.gameData[2], this.gameData[3]);
            ivFavourite.setTag(newValue);
        }));

        boolean replaying = Boolean.parseBoolean(specificData[17]);
        runOnUiThread(() -> {
            cbReplaying.setChecked(replaying);
            cbReplaying.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ContentValues cv = new ContentValues();
                Log.d("ZZZZ", "getSpecificDataFromDB: EO " + isChecked);
                cv.put(GameEntry.REPLAYING, isChecked);
                GameDbHelper.getInstance(this).updateGame(cv, this.gameData[2], this.gameData[3]);
            });
        });

        String startDate = Util.isSet(specificData[14]) ? specificData[14] : getString(R.string.notDefinedYet);
        String finishDate = Util.isSet(specificData[15]) ? specificData[15] : getString(R.string.notDefinedYet);
        runOnUiThread(() -> {
            tvPlaytime.setText(getString(R.string.defaultPlaytime, playtime));
            tvPlaytime.setTag(playtime);
            tvPlaytime.setOnClickListener(ListenerHandler.onClickPlaytime(this, tvPlaytime, gameData[2], gameData[3]));
            tvStartDate.setText(getString(R.string.defaultStartDate, startDate));
            tvStartDate.setTag(startDate);
            tvStartDate.setOnClickListener(ListenerHandler.onClickAlertDate(tvStartDate, R.string.defaultStartDate, GameEntry.START_DATE, this, gameData[2], gameData[3]));
            tvFinishDate.setText(getString(R.string.defaultFinishDate, finishDate));
            tvFinishDate.setTag(finishDate);
            tvFinishDate.setOnClickListener(ListenerHandler.onClickAlertDate(tvFinishDate, R.string.defaultFinishDate, GameEntry.FINISH_DATE, this, gameData[2], gameData[3]));
            rlDatabase.addView(view);
        });
    }

    /**
     * Comprueba si el juego es favorito y modifica el marcador en cada caso
     * @param isFavourite el estado actual
     * @param favourite la <code>ImageView</code> a modificar
     */
    private void checkFavourite(boolean isFavourite, ImageView favourite) {
        if (isFavourite){
            runOnUiThread(() -> favourite.setImageDrawable(getResources().getDrawable(R.drawable.favourite)));

        } else {
            runOnUiThread(() -> favourite.setImageDrawable(getResources().getDrawable(R.drawable.not_favourite)));
        }
    }

    /**
     * Activa los listeners al añadir el juego a la base de datos
     * Este método sólo se usa en caso de que el juego no estuviera antes en la base de datos
     */
    private void activateListeners(){
        tvPlatform.setEnabled(true);
        tvDeveloper.setEnabled(true);
        tvPublisher.setEnabled(true);
        tvDescription.setEnabled(true);
        tvReleaseDate.setEnabled(true);
        tvRating.setEnabled(true);
        tvGenres.setEnabled(true);
    }

    /**
     * Al pulsar el botón de atrás, se manda al activity de origen una flag de borrar
     * el juego de la lista si el estado ha cambiado
     */
    @Override
    public void onBackPressed() {
        JSONHandler.updateJSON(this);
        if (isInDb){
            TextView status = findViewById(R.id.tvStatus);
            int newStatus = (int)status.getTag();
            if (newStatus != originalStatus){
                Intent i = new Intent();
                i.putExtra(StaticFields.KEY_GAME_POS, getIntent().getIntExtra(StaticFields.KEY_GAME_POS, -1));
                setResult(1, i);
            } else {
                setResult(-1, null);
            }
            finish();
        }
        super.onBackPressed();
    }

    /**
     * Activado si decides cambiar una carátula
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                    try {
                        Drawable newCover = Drawable.createFromStream(getContentResolver().openInputStream(selectedImageUri), "cover");
                        cover.setImageDrawable(newCover);

                        try {
                            ImageHandler.saveImageFromLocalSource(newCover, gameData[0], this);
                        } catch (IOException e) {
                            Toast.makeText(this, R.string.failedToSaveCover, Toast.LENGTH_SHORT).show();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Activado al pedir permisos para escribir en el almacenamiento externo
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == StaticFields.PERMISSION_OK){
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];
                if (permission.equals(Manifest.permission.SEND_SMS)) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, R.string.ifPermissionIsNot, Toast.LENGTH_SHORT).show();
                    } else {
                        ImageHandler.saveImageToExternalStorage(this, cover, gameData[2], gameData[0]);
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
