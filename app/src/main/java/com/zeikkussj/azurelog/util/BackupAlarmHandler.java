package com.zeikkussj.azurelog.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.widget.Toast;

import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.game.GameDbHelper;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

public class BackupAlarmHandler extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, context.getString(R.string.makingBackup), Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            String ip = PreferenceManager.getDefaultSharedPreferences(context).getString("backupIP", "");
            String result = FTPBackupHandler.backup(context, ip, FTPBackupHandler.zip(context), context.getDatabasePath(GameDbHelper.DATABASE_NAME), null, null);
            NotificationCompat.Builder notiBuilder;
            notiBuilder = new NotificationCompat.Builder(context, StaticFields.NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.icon_noti)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon))
                    .setContentTitle(context.getString(R.string.azureLogDatabaseBackup))
                    .setContentText(result)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(result))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notiBuilder.setColor(context.getColor(R.color.colorPrimary));
            }
            NotificationManagerCompat.from(context).notify(StaticFields.BACKUP_NOTIFICATION_ID, notiBuilder.build());
        }).start();
    }
}
