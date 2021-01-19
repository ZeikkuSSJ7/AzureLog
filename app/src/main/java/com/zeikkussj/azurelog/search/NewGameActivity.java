package com.zeikkussj.azurelog.search;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.game.Game;
import com.zeikkussj.azurelog.game.GameDbHelper;
import com.zeikkussj.azurelog.game.GameInfoActivity;
import com.zeikkussj.azurelog.util.ImageHandler;
import com.zeikkussj.azurelog.util.ListenerHandler;
import com.zeikkussj.azurelog.util.StaticFields;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class NewGameActivity extends AppCompatActivity {
    private EditText[] editTexts;
    private static final int[] idEditTexts = {
            R.id.newGameTitle,
            R.id.newGamePlatform,
            R.id.newGameDeveloper,
            R.id.newGamePublisher,
            R.id.newGameReleaseDate,
            R.id.newGameDescription,
            R.id.newGameRating,
            R.id.newGameGenres
    };
    private ImageView cover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
        editTexts = new EditText[idEditTexts.length];
        for (int i = 0; i < editTexts.length; i++) {
            editTexts[i] = findViewById(idEditTexts[i]);
        }
        Button changeDate = findViewById(R.id.newGameReleaseDateButton);
        FloatingActionButton saveGame = findViewById(R.id.saveNewGame);
        cover = findViewById(R.id.newGameCover);
        changeDate.setOnClickListener(ListenerHandler.onClickDatePicker(editTexts[4], this));
        saveGame.setOnClickListener(v -> saveGame());
        cover.setOnClickListener(v -> ImageHandler.startImagePickIntent(this));
    }

    /**
     * Guarda el juego customizado con los datos obtenidos del <code>Activity</code>. A su vez,
     * inicia un nuevo <code>Activity</code> con el nuevo juego
     */
    private void saveGame() {
        String[] data = new String[editTexts.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = editTexts[i].getText().toString();
        }
        String id = data[0].hashCode() + StaticFields.CUSTOM_GAME_SUFFIX;
        Game newGame = new Game(id, id + StaticFields.DEFAULT_COVER_EXTENSION, data);
        try {
            ImageHandler.saveImageFromLocalSource(cover.getDrawable(), id, this);
        } catch (IOException e) {
            Toast.makeText(this, R.string.failedToGetCover, Toast.LENGTH_SHORT).show();
        }
        GameDbHelper.getInstance(this).insertGame(newGame);
        Toast.makeText(this, getString(R.string.gameSaved), Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, GameInfoActivity.class);
        i.putExtra(StaticFields.KEY_INFO_DATA, newGame.toArray());
        startActivity(i);
        finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                    try {
                        InputStream is = getContentResolver().openInputStream(selectedImageUri);
                        cover.setImageDrawable(Drawable.createFromStream(is, "cover"));
                    } catch (FileNotFoundException e) {
                        Toast.makeText(this, R.string.failedToGetCover, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}