package com.zeikkussj.azurelog.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.util.ImageHandler;
import com.zeikkussj.azurelog.util.JSONHandler;
import com.zeikkussj.azurelog.util.StaticFields;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class ProfileActivity extends AppCompatActivity {
    private TextView[] profileTVs;
    private ImageView userPic;
    private File userPicFile;
    private static final int[] tvIds = {
            R.id.profileTvUsername,
            R.id.profileTvJoined,
            R.id.profileTvTotalGames,
            R.id.profileTvPlanToPlay,
            R.id.profileTvPlaying,
            R.id.profileTvOnHold,
            R.id.profileTvDropped,
            R.id.profileTvCompleted,
            R.id.profileTvMastered,
            R.id.profileTvTotalCompletedGames,
            R.id.profileTvMeanScore
    };

    private static final int[] stringIds = {
            R.string.username,
            R.string.joined,
            R.string.profileTotalGames,
            R.string.profilePlanToPlay,
            R.string.profilePlaying,
            R.string.profileOnHold,
            R.string.profileDropped,
            R.string.profileCompleted,
            R.string.profileMastered,
            R.string.profileTotalCompletedGames,
            R.string.profileMeanScore
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileTVs = new TextView[tvIds.length];
        for (int i = 0; i < tvIds.length; i++){
            profileTVs[i] = findViewById(tvIds[i]);
        }
        userPic = findViewById(R.id.profileIvUserPic);
        userPicFile = new File(getFilesDir() + StaticFields.USER_DIRECTORY + StaticFields.PROFILE_PIC);
        readUser();
        userPic.setOnClickListener(v -> ImageHandler.startImagePickIntent(this));

    }

    /**
     * Lee el usuario del archivo JSON y lo coloca en las diferentes vistas del Activity
     */
    private void readUser() {
        User user = JSONHandler.readUser(this);
        profileTVs[0].setText(getString(stringIds[0], user.name));
        profileTVs[1].setText(getString(stringIds[1], user.joined));
        profileTVs[2].setText(getString(stringIds[2], user.totalGames));
        profileTVs[3].setText(getString(stringIds[3], user.planToPlay));
        profileTVs[4].setText(getString(stringIds[4], user.playing));
        profileTVs[5].setText(getString(stringIds[5], user.onHold));
        profileTVs[6].setText(getString(stringIds[6], user.dropped));
        profileTVs[7].setText(getString(stringIds[7], user.completed));
        profileTVs[8].setText(getString(stringIds[8], user.mastered));
        profileTVs[9].setText(getString(stringIds[9], user.totalCompletedGames));
        profileTVs[10].setText(getString(stringIds[10], new DecimalFormat("#.##").format(user.meanScore)));
        try {
            if (userPicFile.exists())
                ImageHandler.openImage(userPicFile.getAbsolutePath(), userPic, this);
        } catch (IOException e) {
            Toast.makeText(this, R.string.failedToGetProfilePic, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Activado cuando decides cambiar la imagen de perfil
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == 1){
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        Drawable pic = Drawable.createFromStream(getContentResolver().openInputStream(selectedImageUri), "cover");
                        userPic.setImageDrawable(pic);
                        if (userPicFile.createNewFile())
                            ImageHandler.saveUserPic(pic, userPicFile.getAbsolutePath());
                    } catch (IOException e) {
                        Toast.makeText(this, R.string.failedToGetProfilePic, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
