package com.zeikkussj.azurelog.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.os.Bundle;
import android.widget.RelativeLayout;

import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.util.FTPBackupHandler;
import com.zeikkussj.azurelog.util.Util;

public class RestoreActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);
        RelativeLayout rlBackupParent = findViewById(R.id.rlBackupParent);
        RelativeLayout progress = Util.openProgressBar(this, rlBackupParent);
        FTPBackupHandler.getBackupsFromFTP(
                PreferenceManager.getDefaultSharedPreferences(this).getString("backupIP", ""),
                this);
        Util.closeProgressBar(this, progress);

    }
}
