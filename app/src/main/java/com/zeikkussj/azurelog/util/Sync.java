package com.zeikkussj.azurelog.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.game.Game;
import com.zeikkussj.azurelog.game.GameDbHelper;
import com.zeikkussj.azurelog.settings.SettingsActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public abstract class Sync {

    /**
     * Recopila la base de datos local del usuario y lo transforma en un JSON que manda al servidor
     * de Silent Azure
     * @param activity el <code>SettingsActivity</code> de origen
     * @param context el contexto de la aplicación
     * @param ip la IP de Silent Azure
     * @param username el nombre de usuario
     * @param password la contraseña del usuario
     */
    public static void sendDatabaseChanges(Activity activity, Context context, String ip, String username, String password){
        if (username != null && password != null && !username.trim().isEmpty() && !password.trim().isEmpty()) {

            // Preparación de los AlertDialog
            AlertDialog.Builder adb = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
            AlertDialog.Builder adbresult = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
            adb.setTitle(R.string.sendingData);
            adb.setView(R.layout.progress_bar_base);
            adbresult.setTitle(R.string.results);
            AlertDialog ad = adb.create();
            activity.runOnUiThread(ad::show);

            new Thread(() -> {
                Gson gson =  new GsonBuilder().serializeNulls().setPrettyPrinting().create();
                // serializeNulls() evita conflictos con PHP
                JsonObject master = new JsonObject();
                JsonArray array = new JsonArray();
                ArrayList<Game> games = GameDbHelper.getInstance(context).getGames();
                for (Game game : games){
                    // Popula el JsonArray con todos los datos específicos de los juegos de la
                    // base de datos local
                    JsonObject gameObject = new JsonObject();
                    String id = game.getId();
                    String name = game.getName();
                    int score = game.getScore();
                    int status = game.getStatus();
                    String comment = game.getComment();
                    int favourite = game.isFavourite() ? 1 : 0;
                    String startDate = game.getStartDate();
                    String finishDate = game.getFinishDate();
                    double playtime = game.getPlaytime();
                    int replaying = game.isReplaying() ? 1 : 0;

                    gameObject.addProperty("id", id);
                    gameObject.addProperty("name", name);
                    gameObject.addProperty("score", score);
                    gameObject.addProperty("status", status);
                    gameObject.addProperty("comment", comment);
                    gameObject.addProperty("favourite", favourite);
                    gameObject.addProperty("startdate", startDate);
                    gameObject.addProperty("finishdate", finishDate);
                    gameObject.addProperty("playtime", playtime);
                    gameObject.addProperty("replaying", replaying);

                    array.add(gameObject);
                }
                master.add("data", array);
                master.addProperty("username", username); // Añade el usuario y contraseña
                master.addProperty("password", password); // para saber a quién sincronizar
                String json = gson.toJson(master);
                try {
                    URL url = new URL("http://" + ip + "/azurelog/ajax/syncRecieveGames.php");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json; utf-8");
                    con.setDoOutput(true);
                    try(OutputStream os = con.getOutputStream()) { // Manda los datos
                        byte[] input = json.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                    String responseLine;
                    try(BufferedReader br = new BufferedReader( // Recibe la respuesta
                            new InputStreamReader(con.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim()).append("\n");
                        }
                        responseLine = response.toString();
                    }

                    String finalResponseLine = responseLine;
                    activity.runOnUiThread(() -> {
                        ad.dismiss();
                        Toast.makeText(activity, R.string.dataSent, Toast.LENGTH_SHORT).show();
                        // Muestra con la respuesta los posibles errores
                        adbresult.setMessage(finalResponseLine);
                        adbresult.show();
                    });
                } catch (IOException e) {
                    activity.runOnUiThread(() -> {
                        Toast.makeText(context, R.string.couldNotConnectToSilentAzure, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
                }
            }).start();
        } else {
            Toast.makeText(activity, R.string.ipUsernameOrPasswordInvalid, Toast.LENGTH_SHORT).show();
        }

    }
}
