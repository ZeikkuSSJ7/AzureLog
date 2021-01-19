package com.zeikkussj.azurelog.util;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.activities.PlayingActivity;
import com.zeikkussj.azurelog.game.Game;
import com.zeikkussj.azurelog.game.GameDbHelper;
import com.zeikkussj.azurelog.search.GameSearchActivity;
import com.zeikkussj.azurelog.settings.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public abstract class JSONHandler {
    private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Crea un <code>AlertDialog</code> que insta al usuario a crear un perfil
     * @param context el contexto de la aplicación
     * @param activity el <code>PlayingActivity</code> de origen
     */
    public static void makeUser(Context context, PlayingActivity activity){
        AlertDialog.Builder adb = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        RelativeLayout rl = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.dialog_create_user, null);
        adb.setTitle(R.string.noUserCreated).setView(rl).setCancelable(false);

        EditText etUsername = rl.findViewById(R.id.etUsername);
        adb.setPositiveButton("OK", (dialog, which) -> {
            try {
                String username = etUsername.getText().toString();
                Calendar c = Calendar.getInstance();
                User user = new User(username, String.format(Locale.ENGLISH, "%d-%d-%d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)));
                File json = new File(context.getFilesDir() + StaticFields.JSON_DATA_DIRECTORY);
                json.getParentFile().mkdir();
                if (json.createNewFile()){
                    BufferedWriter bw = new BufferedWriter(new FileWriter(json));
                    gson.toJson(user, bw);
                    bw.close();
                }
            } catch (FileNotFoundException e) {
                Log.d("JSON Parsing Error", "file not found. Is user created?");
                e.printStackTrace();
            } catch (IOException e){
                Log.d("JSON r/w error", "does directory exist or do you have permissions on the folder?");
                e.printStackTrace();
            }
        }).show();
    }

    /**
     * Lee el usuario y lo transforma a un objeto <code>User</code>
     * @param context el contexto de la aplicación
     * @return el usuario construido
     */
    public static User readUser(final Context context){
        User user;
        try {
            File json = new File(context.getFilesDir().getAbsolutePath() + StaticFields.JSON_DATA_DIRECTORY);
            BufferedReader br = new BufferedReader(new FileReader(json));
            user = gson.fromJson(br, User.class);
        } catch (IOException e) {
            user = null;
            Log.d("JSON r/w error", "does directory exist or do you have permissions on the folder?");
        }
        return user;
    }

    /**
     * Actualiza los datos del usuario y los guarda en el archivo JSON
     * @param context el contexto de la aplicación
     */
    public static void updateJSON(Context context){
        User user = readUser(context);
        GameDbHelper db = GameDbHelper.getInstance(context);
        ArrayList<Game> games = db.getGames();
        float meanScore = db.getMeanScore();
        user.putValuesToZero();
        for (Game game : games){
            int status = game.getStatus();
            switch (status) {
                case 1:
                    user.playing++;
                    break;
                case 0:
                    user.planToPlay++;
                    break;
                case 2:
                    user.onHold++;
                    break;
                case 3:
                    user.dropped++;
                    break;
                case 4:
                    user.completed++;
                    user.totalCompletedGames++;
                    break;
                default:
                    user.mastered++;
                    user.totalCompletedGames++;
                    break;
            }
            user.totalGames++;
        }
        user.meanScore = meanScore;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(context.getFilesDir().getAbsolutePath() + StaticFields.JSON_DATA_DIRECTORY)));
            gson.toJson(user, bw);
            bw.close();
            Log.d("JSON Update", "Succesfully updated user JSON");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Busca en la base de datos externa la cadena indicada y devuelve una lista de los juegos
     * encontrados junto a sus URLs
     * @param context el contexto de la aplicación
     * @param query la cadena a buscar
     * @return la lista de los juegos
     */
    public static ArrayList<Game> getGamesFromExternalDB(Context context, String query){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(new URL("https://api.thegamesdb.net/v1/Games/ByGameName?apikey=" + StaticFields.THEGAMESDB_PUBLIC_KEY + "&name=" + query.replaceAll(" ", "+").toLowerCase() + "&fields=publishers%2Cgenres%2Coverview%2Crating&include=boxart").openStream());
        } catch (IOException e) {
            Toast.makeText(context, R.string.failedToConnectExtDB, Toast.LENGTH_SHORT).show();
        }
        ArrayList<Game> games = new ArrayList<>();
        if (isr != null){
            JsonElement json = gson.fromJson(isr, JsonElement.class);
            JsonObject jsonObject = json.getAsJsonObject();
            JsonArray jsonGames = jsonObject.get("data").getAsJsonObject().get("games").getAsJsonArray();
            GameDbHelper db = GameDbHelper.getInstance(context);
            if (jsonGames.size() > 0){
                for (JsonElement jsonElement : jsonGames) {
                    JsonObject gameObject = jsonElement.getAsJsonObject();
                    Game game = new Game();
                    game.setId(gameObject.get("id").getAsString());
                    game.setCover("URL");
                    game.setName(gameObject.get("game_title").getAsString());
                    game.setPlatform(db.getPlatforms(checkNull(context, R.string.noPlatformAvailable, gameObject.get("platform"))));
                    game.setDevelopers(db.getDevelopers(checkNull(context, R.string.noDeveloperAvailable, gameObject.get("developers"))));
                    game.setPublishers(db.getPublishers(checkNull(context, R.string.noPublisherAvailable, gameObject.get("publishers"))));
                    game.setReleaseDate(checkNull(context, R.string.noReleaseDateAvailable, gameObject.get("release_date")));
                    game.setDescription(checkNull(context, R.string.noDescriptionIsAvailable, gameObject.get("overview")));
                    game.setRating(checkNull(context, R.string.noRatingAvailable, gameObject.get("rating")));
                    game.setGenres(db.getGenres(checkNull(context, R.string.noGenresAvailable, gameObject.get("genres"))));
                    games.add(game);
                }

                // Lectura de imágenes
                JsonElement include = json.getAsJsonObject().get("include").getAsJsonObject().get("boxart");
                JsonObject baseUrls = include.getAsJsonObject().get("base_url").getAsJsonObject();
                String[] urls = {baseUrls.get("thumb").getAsString(), baseUrls.get("medium").getAsString(), baseUrls.get("original").getAsString(), baseUrls.get("large").getAsString()};
                ArrayList<String> boxartUrlList = new ArrayList<>();
                JsonObject data = include.getAsJsonObject().get("data").getAsJsonObject();
                for (int j = 0; j < games.size(); j++) {
                    JsonElement dataImages = data.get(games.get(j).getId());
                    if (dataImages == null) {
                        boxartUrlList.add(null);
                    } else {
                        JsonArray jsonArray = dataImages.getAsJsonArray();
                        for (int i = 0; i < jsonArray.size(); i++) {
                            String filename = jsonArray.get(i).getAsJsonObject().get("filename").getAsString();
                            if (filename.startsWith("boxart/front"))
                                boxartUrlList.add(filename);
                        }
                    }
                }
                for (String boxartUrl : boxartUrlList) {
                    String[] formedUrls;
                    if (boxartUrl != null){
                        formedUrls = new String[urls.length];
                        int cont = 0;
                        for (String url : urls) {
                            formedUrls[cont++] = url + boxartUrl ;
                        }
                    } else {
                        formedUrls = null;
                    }
                    GameSearchActivity.urls.add(formedUrls); // Se guardan en un array del Activity original
                }
                GameSearchActivity.nextPage = checkNull(context, 0, jsonObject.get("pages").getAsJsonObject().get("next"));
            }
        }
        return games;
    }

    /**
     * Comprueba que el elemento JSON entrante no es nulo, y si no lo es devuelve sus contenidos en forma de cadena
     * @param context el contexto de la aplicación
     * @param stringIdType el ID del mensaje por defecto en caso de ser nulo
     * @param element el <code>JsonElement</code> leído
     * @return hay tres opciones:
     *      - la frase con los datos, si no es nulo
     *      - el mensaje por defecto en caso de haber sido indicado y ser el elemento nulo
     *      - nulo si no hay ni mensaje por defecto ni datos
     */
    private static String checkNull(Context context, int stringIdType, JsonElement element) {
        if (!element.isJsonNull()) {
            if (element.isJsonArray()) {
                StringBuilder elements = new StringBuilder();
                for (JsonElement genre : element.getAsJsonArray()) {
                    elements.append(genre.getAsString()).append(" ");
                }
                return elements.toString();
            } else {
                return element.getAsString();
            }
        } else {
            if (stringIdType == 0)
                return null;
            return context.getString(stringIdType);
        }
    }
}
