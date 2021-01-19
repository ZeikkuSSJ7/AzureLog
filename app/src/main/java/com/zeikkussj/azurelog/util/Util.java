package com.zeikkussj.azurelog.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.zeikkussj.azurelog.LoadingActivity;
import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.activities.CompletedActivity;
import com.zeikkussj.azurelog.developer.DeveloperActivity;
import com.zeikkussj.azurelog.activities.DroppedActivity;
import com.zeikkussj.azurelog.activities.MasteredActivity;
import com.zeikkussj.azurelog.activities.OnHoldActivity;
import com.zeikkussj.azurelog.activities.PlanToPlayActivity;
import com.zeikkussj.azurelog.activities.PlayingActivity;
import com.zeikkussj.azurelog.activities.AllGamesActivity;
import com.zeikkussj.azurelog.search.AdvancedSearchActivity;
import com.zeikkussj.azurelog.game.Game;
import com.zeikkussj.azurelog.game.GameAdapter;
import com.zeikkussj.azurelog.game.GameDbHelper;
import com.zeikkussj.azurelog.search.GameSearchActivity;
import com.zeikkussj.azurelog.settings.ProfileActivity;
import com.zeikkussj.azurelog.settings.SettingsActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import static android.app.Activity.RESULT_CANCELED;
import static com.zeikkussj.azurelog.game.GameConstants.*;

public abstract class Util {
    static final int FLAG_RANDOM_PLAN_TO_PLAY_GAMES = 1;
    public static final int FLAG_DEFAULT_QUERY = 0;

    /**
     * Establece el evento del <code>Drawer</code> para inciar cada nuevo <code>Activity</code>
     * @param nav el nav del <code>Drawer</code>
     * @param homeActivity la instancia original del <code>PlayingActivity</code>
     */
    public static void setNavItems(NavigationView nav, PlayingActivity homeActivity) {
        nav.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()){
                case R.id.menuSearchGame:
                    startActivity(homeActivity, GameSearchActivity.class);
                    break;
                case R.id.menuCompleted:
                    startActivity(homeActivity, CompletedActivity.class);
                    break;
                case R.id.menuPlanToPlay:
                    startActivity(homeActivity, PlanToPlayActivity.class);
                    break;
                case R.id.menuMastered:
                    startActivity(homeActivity, MasteredActivity.class);
                    break;
                case R.id.menuOnHold:
                    startActivity(homeActivity, OnHoldActivity.class);
                    break;
                case R.id.menuDropped:
                    startActivity(homeActivity, DroppedActivity.class);
                    break;
                case R.id.menuAllGames:
                    startActivity(homeActivity, AllGamesActivity.class);
                    break;
                case R.id.menuSettings:
                    startActivity(homeActivity, SettingsActivity.class);
                    break;
                case R.id.menuProfile:
                    startActivity(homeActivity, ProfileActivity.class);
                    break;
                case R.id.menuDeveloper:
                    startActivity(homeActivity, DeveloperActivity.class);
                    break;
                case R.id.menuSearchGameAdvanced:
                    startActivity(homeActivity, AdvancedSearchActivity.class);
                    break;
            }
            return false;
        });
    }

    /**
     * Inicia el <code>Activity</code> indicado por los argumentos desde el
     * <code>PlayingActivity</code> de origen
     * @param home la instancia original del <code>PlayingActivity</code>
     * @param toClass el nuevo <code>Activity</code> a iniciar
     */
    private static void startActivity(PlayingActivity home, Class toClass){
        Intent intent = new Intent(home, toClass);
        home.startActivityForResult(intent, RESULT_CANCELED);
    }

    /**
     * Inicializa la base de datos en el primer inicio de la aplicación
     * @param activity el <code>LoadingActivity</code> que carga los datos
     * @throws IOException si ocurre un error al leer los datos de los assets
     */
    public static void initDB(LoadingActivity activity) throws IOException {
        if (!new File(activity.getFilesDir() + StaticFields.JSON_DATA_DIRECTORY).exists()) {
            AssetManager mgr = activity.getAssets();
            JsonObject developers = new Gson().fromJson(new InputStreamReader(mgr.open("developers.json")), JsonObject.class);
            JsonObject publishers = new Gson().fromJson(new InputStreamReader(mgr.open("publishers.json")), JsonObject.class);
            JsonObject genres = new Gson().fromJson(new InputStreamReader(mgr.open("genres.json")), JsonObject.class);
            JsonObject platforms = new Gson().fromJson(new InputStreamReader(mgr.open("platforms.json")), JsonObject.class);
            _initDB(developers, "developers", activity);
            _initDB(publishers, "publishers", activity);
            _initDB(genres, "genres", activity);
            _initDB(platforms, "platforms", activity);
        }
    }

    /**
     * Inicializa la base de datos con los argumentos dados
     * @param json el JSON con los datos
     * @param table la tabla a crear y popular
     * @param activity el <code>LoadingActivity</code> que carga los datos
     */
    private static void _initDB(JsonObject json, String table, LoadingActivity activity){
        HashMap<Integer, String> data = new HashMap<>();
        JsonObject obj = json.get("data").getAsJsonObject();
        JsonObject kinds = obj.get(table).getAsJsonObject();
        for (int i = 0; i < 50000; i++) {
            JsonObject kind = kinds.get("" + i) == null ? null : kinds.get("" + i).getAsJsonObject();
            if (kind != null) {
                data.put(kind.get("id").getAsInt(), kind.get("name").getAsString().replaceAll("'", ""));
            }
        }
        GameDbHelper.getInstance(activity).initDB(table, data);
    }

    /**
     * Constuye una consulta en base a los datos de los argumentos
     * @param activity el <code>Activity</code> correspondiente
     * @param flag sólo si se necesita para obtener un juego aleatorio
     * @return la consulta compilada en forma de array de <code>String</code>, en la posición 0
     * están los datos de consulta y en la posición 1 están los whereArgs
     */
    private static String[] getFromDB(Activity activity, int flag) {
        String query = "select * from " + GameEntry.TABLE_NAME + " where " + GameEntry.STATUS + "=? order by " + GameEntry.NAME;
        String whereArgs = null;
        if (flag == FLAG_RANDOM_PLAN_TO_PLAY_GAMES || activity instanceof PlanToPlayActivity)
            whereArgs = "0";
        else if (activity instanceof PlayingActivity)
            whereArgs = "1";
        else if (activity instanceof OnHoldActivity)
            whereArgs = "2";
        else if (activity instanceof DroppedActivity)
            whereArgs = "3";
        else if (activity instanceof CompletedActivity)
            whereArgs = "4";
        else if (activity instanceof MasteredActivity)
            whereArgs = "5";
        else if (activity instanceof AllGamesActivity)
            query = "select * from " + GameEntry.TABLE_NAME + " order by " + GameEntry.NAME;
        return new String[]{query, whereArgs};
    }

    /**
     * Obtiene los juegos de la base de datos conformados a los argumentos
     * @param activity el <code>Activity</code> que lo llama
     * @param flag sólo si se necesita para obtener un juego aleatorio
     * @return los juegos conformados a los argumentos en forma de lista
     */
    public synchronized static ArrayList<Game> getDataFromDB(Activity activity, int flag) {
        String[] queryBuildedParts = getFromDB(activity, flag);
        return GameDbHelper.getInstance(activity).getGames(queryBuildedParts[0], queryBuildedParts[1] != null ? new String[]{queryBuildedParts[1]} : null);
    }
    /**
     * Añade una barra de progreso a la UI en el <code>RelativeLayout</code>
     * @param context el <code>Activity</code> usado para añadirlo en el hilo de la UI
     * @param parent el contenedor de la barra de progreso
     * @return la barra de progreso, para cerrarla más adelante
     */
    public static RelativeLayout openProgressBar(AppCompatActivity context, RelativeLayout parent){
        RelativeLayout progress = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.progress_bar_base, parent, false);
        context.runOnUiThread(() -> parent.addView(progress));
        return progress;
    }

    /**
     * Cierra (hace desaparecer) la barra de progreso abierta anteriormente con
     * {@link #openProgressBar(AppCompatActivity, RelativeLayout)}
     * @param activity el <code>Activity</code> usado para cerrarlo en el hilo de la UI
     * @param pb la barra de progreso a cerrar
     */
    public static void closeProgressBar(AppCompatActivity activity, RelativeLayout pb){
        activity.runOnUiThread(() -> pb.setVisibility(View.GONE));
    }

    /**
     * Abre la barra de acción en el <code>Activity</code> especificad0
     * @param activity el <code>Activity</code> especificado
     * @param toolbarResID la barra de acción a abrir
     *
     * @see #onCreateOptionsMenu(AppCompatActivity, Menu, GridView)
     */
    public static void openToolbar(AppCompatActivity activity, int toolbarResID){
        Toolbar myToolbar = activity.findViewById(toolbarResID);
        activity.setSupportActionBar(myToolbar);
        if (!(activity instanceof PlayingActivity)){
            ActionBar ab = activity.getSupportActionBar();
            Objects.requireNonNull(ab).setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Activa los eventos de la barra de búsqueda en todos los <code>Activity</code> con ayuda
     * de {@link #openToolbar(AppCompatActivity, int)}.
     * El <code>onQueryTextSubmit</code> quita el foco de la barra de busqueda {@link SearchView}
     * El <code>onQueryTextChange</code> filtra en la lista de juegos los que coincidan con
     * la cadena original
     * @param activity el <code>Activity</code> que contiene la barra de búsqueda
     * @param menu el menú de la barra de acción
     * @param tableGrid el {@link GridView} para obtener el adaptador
     */
    public static void onCreateOptionsMenu(AppCompatActivity activity, Menu menu, GridView tableGrid){
        activity.getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        SearchView sv = (SearchView) menu.findItem(R.id.toolbar_search).getActionView();
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                sv.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                GameAdapter adapter = (GameAdapter) tableGrid.getAdapter();
                Handler mHandler = new Handler();
                mHandler.post(() -> adapter.getFilter().filter(newText.trim()));
                return true;
            }
        });
    }

    /**
     * Usado en toda la aplicación, mantiene la fución del botón 'atrás' o del menu 'hamburger'  en la
     * esquina superior izquierda
     * @param item
     * @param activity
     */
    public static void onOptionItemSelected(MenuItem item, AppCompatActivity activity, @Nullable DrawerLayout nav){
        if (!(activity instanceof PlayingActivity)){
            if (item.getItemId() != R.id.toolbar_search){
                activity.onBackPressed();
            }
        } else {
            if (item.getItemId() == 16908332) { // hamburger
                nav.openDrawer(Gravity.START);
            }
        }
    }

    /**
     * Usado en todos los <code>Activity</code> de categorías menos <code>PlayingActivity</code>,
     * borra un juego de la lista al volver al <code>Activity</code> si su estado ha cambiado
     * del original
     * @param adapter el adaptador de los juegos
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public static void onActivityResult(GameAdapter adapter, int requestCode, int resultCode, Intent data){
        if (requestCode == RESULT_CANCELED){
            if (resultCode == 1){
                int gamePosition = data.getIntExtra(StaticFields.KEY_GAME_POS, -1);
                adapter.remove(gamePosition);
            }
        }
    }

    /**
     * Muestra un pequeño <code>TextView</code> con un mensaje de error para el usuario
     * @param context el contexto de la aplicación
     * @param message el mensaje de error
     * @return el <code>TextView</code> formateado
     */
    public static TextView errorText(Context context, String message){
        TextView tv = new TextView(context);
        tv.setTextSize(16);
        tv.setPadding(7,7,7,7);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setText(message);
        return tv;
    }

    /**
     * Formatea el mes, dia, hora, minuto o segundo en el patrón correcto
     * @param in el mes, dia, hora, minuto o segundo
     * @return el valor con un 0 si era un dígito sólo, igual si tiene 2 dígitos
     */
    static String correctPattern(int in){
        return correctPattern(String.valueOf(in));
    }

    /**
     * Comprueba si la cadena está inicializada y no está vacía
     * @param input la cadena de entrada
     * @return true si cumple, false en otro caso
     */
    public static boolean isSet(String input){
        return input != null && !input.trim().isEmpty();
    }

    /**
     * Formatea el mes, dia, hora, minuto o segundo en el patrón correcto
     * @param num el mes, dia, hora, minuto o segundo
     * @return el valor con un 0 si era un dígito sólo, igual si tiene 2 dígitos
     */
    public static String correctPattern(String num) {
        if (num.length() < 2) {
            return "0" + num;
        }
        return num;
    }

    private static SharedPreferences sharedPreferences;

    /**
     * @return el tiempo en milis hasta la hora elegida por el usuario
     */
    private static long getBackupHour() {
        String hourOfBackup = sharedPreferences.getString("backupDailyHour", "00:00");
        Calendar post = Calendar.getInstance();
        return getCorrectHour(post, Integer.parseInt(hourOfBackup.split(":")[0]), Integer.parseInt(hourOfBackup.split(":")[1]));
    }

    /**
     * Obtiene el tiempo en milis hasta la hora elegida por el usuario en el formato correcto
     * @param post el calendario usado
     * @param targetHour la hora a la que se realiza
     * @param targetMinute el minuto en el que se realiza
     * @return el tiempo en milis hasta la hora elegida por el usuario en el formato correcto
     */
    private static long getCorrectHour(Calendar post, int targetHour, int targetMinute) {
        int currMinute = post.get(Calendar.MINUTE);
        int currHour = post.get(Calendar.HOUR_OF_DAY);
        if (currHour <= targetHour ){
            post.set(Calendar.HOUR_OF_DAY, targetHour);
            post.set(Calendar.MINUTE, targetMinute);
            if (currHour == targetHour){
                if (currMinute > targetMinute)
                    post.set(Calendar.DAY_OF_MONTH, post.get(Calendar.DAY_OF_MONTH) + 1);
            }
        } else{
            post.set(Calendar.DAY_OF_MONTH, post.get(Calendar.DAY_OF_MONTH) + 1);
            post.set(Calendar.HOUR_OF_DAY, targetHour);
            post.set(Calendar.MINUTE, targetMinute);
        }
        post.set(Calendar.SECOND, 0);
        return post.getTimeInMillis();
    }

    /**
     * Crea una alarma diaria según los parámetros
     * @param context el contexto de la aplicación
     * @param activity la clase del <code>Activity</code> que contiene el <code>BroadcastReciever</code>
     * @param setting la preferencia a consultar para poder reaizarlo
     * @param interval el intervalo entre activación y activación
     */
    private static void createAlarm(Context context, Class activity, String setting, long interval){
        Intent intent = new Intent(context, activity);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (manager != null) {
            manager.cancel(pendingIntent);
            if (sharedPreferences.getBoolean(setting, false)){
                manager.setRepeating(AlarmManager.RTC, interval, AlarmManager.INTERVAL_DAY, pendingIntent);
            }
        }
    }

    /**
     * Crea la alarma que realiza la copia de seguridad diaria
     * @param context el contexto de la aplicación
     *
     * @see BackupAlarmHandler
     */
    public static void createBackupJob(Context context){
        createAlarm(context, BackupAlarmHandler.class, "backupDaily", getBackupHour());
    }

    /**
     * Crea la alarma de 'Hoy terminaste..'. Se lanza cada día a las 00:00
     * @param context el contexto de la aplicación
     *
     * @see OnThisDayAlarmHandler
     */
    public static void createOnThisDayJob(Context context){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        createAlarm(context, OnThisDayAlarmHandler.class, "notificationOnThisDay", c.getTimeInMillis());
    }

    /**
     * Crea el canal de notificación de la aplicación en Android >= Oreo
     * @param context el contexto de la aplicación
     */
    public static void createNotificationChannel(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notificationChannelName);
            String description = context.getString(R.string.notificationChannelDescription);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(StaticFields.NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }
    }

    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    /**
     * Valida la IP indicada
     * @param ip la IP
     * @return true si coincide con el patro, false en otro caso
     */
    public static boolean validateIP(String ip){
        if (ip.matches(IPADDRESS_PATTERN)){
            return !ip.equals("0.0.0.0") && !ip.equals("255.255.255.255");
        } else {
            return ip.matches("[a-zA-Z]+");
        }
    }
}
