package com.zeikkussj.azurelog.settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.game.GameDbHelper;
import com.zeikkussj.azurelog.util.FTPBackupHandler;
import com.zeikkussj.azurelog.util.Util;

import java.io.File;

public class BackupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        Button tvZip = findViewById(R.id.buttonSubmitBackup);
        EditText ip = findViewById(R.id.etIpToConnect);
        ip.setText(PreferenceManager.getDefaultSharedPreferences(this).getString("backupIP", ""));
        tvZip.setOnClickListener(v -> {

            // Comprueba la IP
            String ipToConnect = ip.getText().toString();
            if (!Util.validateIP(ipToConnect)){
                Toast.makeText(BackupActivity.this, R.string.ipNotValid, Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear el diálogo para mostrarlo
            AlertDialog.Builder adb = new AlertDialog.Builder(BackupActivity.this, R.style.AlertDialogCustom);
            adb.setTitle(getString(R.string.creatingBackupPleaseWait));
            RelativeLayout wrapper = new RelativeLayout(this);
            RelativeLayout rlProgress = (RelativeLayout) getLayoutInflater().inflate(R.layout.alert_progress_with_message, wrapper);
            adb.setView(wrapper);
            adb.setCancelable(false);
            AlertDialog dialogBackup = adb.create();
            TextView message = rlProgress.findViewById(R.id.tvMessage);

            // Lanza un hilo para comprimir y enviar los datos
            new Thread(() -> {
                runOnUiThread(() -> {
                    message.setText(R.string.zippingImages);
                    dialogBackup.show();
                });
                File imagesZip = FTPBackupHandler.zip(BackupActivity.this);
                if (imagesZip != null){
                    File database = getDatabasePath(GameDbHelper.DATABASE_NAME);
                    String result = FTPBackupHandler.backup(BackupActivity.this, ipToConnect, imagesZip, database, BackupActivity.this, message);
                    runOnUiThread(() -> {
                        dialogBackup.dismiss();
                        Toast.makeText(BackupActivity.this, result, Toast.LENGTH_SHORT).show();
                    });
                    finish();

                }
            }).start();
        });
    }


}
