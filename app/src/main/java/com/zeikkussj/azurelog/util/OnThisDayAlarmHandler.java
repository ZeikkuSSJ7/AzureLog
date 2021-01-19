package com.zeikkussj.azurelog.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.game.Game;
import com.zeikkussj.azurelog.game.GameDbHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class OnThisDayAlarmHandler extends BroadcastReceiver {
    private int notificationId = 0;
    @Override
    public void onReceive(Context context, Intent intent) {
        new Thread(() -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            String wildCard = "%-" + Util.correctPattern(String.valueOf(month)) + "-" + Util.correctPattern(String.valueOf(day));
            ArrayList<Game> games = GameDbHelper.getInstance(context).getGamesFinishedToday(wildCard);
            for (Game game : games){
                String gameFinishDate = game.getFinishDate();
                int yearsAgo = year - Integer.parseInt(gameFinishDate.split("-")[0]);
                String gameName = game.getName();
                String notificationText = context.getString(R.string.onThisDayYou, gameName) +
                        " " +
                        context.getResources().getQuantityString(R.plurals.yearsAgoOn, yearsAgo, yearsAgo, gameFinishDate);
                notification(context, context.getString(R.string.onThisDay), notificationText);
                notificationId++;
            }
            if (notificationId == 0){
                notification(context, context.getString(R.string.noGamesFinished), context.getString(R.string.youHaveNoGamesFinishedToday));
            }
        }).start();
    }

    /**
     * Notifica al usuario con un mensaje concreto
     * @param context el contexto de la aplicación
     * @param title el título de la notificación
     * @param message el mensaje de la notificación
     */
    private void notification(Context context, String title, String message){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, StaticFields.NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.icon_noti)
                .setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setColor(context.getColor(R.color.colorPrimary));
        }
        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }
}
