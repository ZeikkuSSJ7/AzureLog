package com.zeikkussj.azurelog.game;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

import static com.zeikkussj.azurelog.game.GameConstants.GameEntry;

public class GameDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "games.db";
    private static GameDbHelper gameDbHelper;
    private static SQLiteDatabase db;

    // Singleton
    private GameDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
    }

    public static GameDbHelper getInstance(Context context) {
        if (gameDbHelper == null){
            gameDbHelper = new GameDbHelper(context);
        }
        return gameDbHelper;
    }


    /**
     * Cierra la base de datos en su llamada para guardar los cambios realizados
     */
    @Override
    public synchronized void close() {
        gameDbHelper = null;
        db.close();
        super.close();
    }

    /**
     * Comprueba si el id está en la base de datos
     * @param id el id del juego
     * @return true si está, false en caso contrario
     */
    boolean isGameInDB(String id) {
        Cursor cursor = db.rawQuery("select " + GameEntry.ID + " from " + GameEntry.TABLE_NAME + " where " + GameEntry.ID + " =?", new String[]{id});
        boolean result = cursor.getCount() != 0;
        cursor.close();
        return result;
    }

    /**
     * Obtiene los datos de un juego concreto
     * @param id el id del juego
     * @return el juego con los datos opiconales
     */
    Game getOwnGameInfo(String id) {
        Cursor cursor = db.rawQuery("select * from " + GameEntry.TABLE_NAME + " where " + GameEntry.ID + "=?", new String[]{id});
        cursor.moveToFirst();
        Game game = new Game();
        game.setScore(cursor.getInt(cursor.getColumnIndex(GameEntry.SCORE)));
        game.setStatus(cursor.getInt(cursor.getColumnIndex(GameEntry.STATUS)));
        game.setComment(cursor.getString(cursor.getColumnIndex(GameEntry.COMMENT)));
        game.setStartDate(cursor.getString(cursor.getColumnIndex(GameEntry.START_DATE)));
        game.setFinishDate(cursor.getString(cursor.getColumnIndex(GameEntry.FINISH_DATE)));
        game.setPlaytime(cursor.getDouble(cursor.getColumnIndex(GameEntry.PLAYTIME)));
        game.setFavourite(cursor.getInt(cursor.getColumnIndex(GameEntry.FAVOURITE)) == 1);
        game.setReplaying(cursor.getInt(cursor.getColumnIndex(GameEntry.REPLAYING)) == 1);
        cursor.close();
        return game;
    }

    /**
     * Inserta un juego en la base de datos
     * @param game el juego a insertar
     */
    public void insertGame(Game game){
        db.insert(GameEntry.TABLE_NAME, null, game.toContentValues());
    }

    /**
     * Borra un juego de la base de datos
     * @param id el id del juego a borrar
     */
    public void deleteGame(String id){
        db.delete(GameEntry.TABLE_NAME, "id=?", new String[]{id});
    }

    /**
     * Actualiza un juego ya existente en la base de datos
     * @param data los nuevos datos del juego
     * @param name el nombre del juego
     * @param platform la plataforma del juego
     */
    public void updateGame(ContentValues data, String name, String platform){
        db.update(GameConstants.GameEntry.TABLE_NAME, data, GameConstants.GameEntry.NAME + "=?" + " and " + GameConstants.GameEntry.PLATFORM + "=?", new String[]{name, platform});
    }

    /**
     * Obtiene los datos de las diferentes tablas de la base de datos
     * @param id el id de la tabla
     * @param table la tabla
     * @param appendSeparator separador entre diferentes datos (sólo en el caso de los géneros)
     * @return la String construida con los datos
     */
    private String getFromDatabase(String id, String table, String appendSeparator){
        if (id.matches("[a-zA-Z ]*"))
            return id;
        String[] ids = id.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String s : ids){
            Cursor c = db.rawQuery("SELECT name FROM " + table + " WHERE id =?", new String[]{s});
            c.moveToFirst();
            sb.append(c.getString(c.getColumnIndex("name"))).append(appendSeparator);
            c.close();
        }
        return sb.toString();
    }

    public String getPlatforms(String id){
        return getFromDatabase(id, "platforms", "");
    }

    public String getDevelopers(String id){
        return getFromDatabase(id, "developers", "");
    }

    public String getPublishers(String id){
        return getFromDatabase(id, "publishers", "");
    }

    public String getGenres(String id){
        return getFromDatabase(id, "genres", " |");
    }

    /**
     * Inicializa la base de datos al instalar la aplicación por primera vez
     * @param table la tabla a crear y popular
     * @param data los datos de la tabla
     */
    public void initDB(String table, HashMap<Integer, String> data) {
        db.execSQL("CREATE TABLE " + table + "(id INTEGER PRIMARY KEY, name TEXT NOT NULL) ");
        for (HashMap.Entry<Integer, String> row : data.entrySet()) {
            Cursor c = db.rawQuery("SELECT id FROM " + table + " WHERE id = " + row.getKey(), null);
            if (!c.moveToNext())
                db.execSQL("INSERT INTO " + table + " VALUES (" + row.getKey() + ", '" + row.getValue() + "')");
            c.close();
        }
    }

    /**
     * Obtiene la media de las puntuaciones que hayas proporcionado a tus juegos
     * @return la media de las puntuaciones
     */
    public float getMeanScore() {
        Cursor cursor = db.rawQuery("select " + GameEntry.SCORE + " from " + GameEntry.TABLE_NAME, null);
        int sum = 0;
        int mean = 0;
        while (cursor.moveToNext()){
            int score = cursor.getInt(cursor.getColumnIndex(GameEntry.SCORE));
            if (score != 0){
                sum += score;
                mean++;
            }
        }
        cursor.close();
        return sum / mean;
    }

    /**
     * Obtiene todos tus juegos
     * @return todos tus juegos
     */
    public ArrayList<Game> getGames(){
        return getGames("select * from " + GameEntry.TABLE_NAME, null);
    }

    /**
     * Obtiene todos los juegos coincidiendo con tu SQL y argumentos
     * @param sql el SQL a consultar
     * @param whereArgs los argumentos del mismo
     * @return la lista de tus juegos coincidentes
     */
    public ArrayList<Game> getGames(String sql, String[] whereArgs){
        Cursor cursor = db.rawQuery(sql, whereArgs);
        ArrayList<Game> games = getGames(cursor);
        cursor.close();
        return games;
    }

    /**
     * Realiza una consulta que indiques
     * @param sql el SQL a usar
     * @return todos los juegos que coincidan con tus argumentos
     */
    public ArrayList<Game> customQuery(String sql){
        return getGames(sql, null);
    }

    /**
     * Devuelve una lista de juegos usando el cursor de entrada
     * @param cursor el cursor abierto de una consulta
     * @return la lista de juegos
     */
    private ArrayList<Game> getGames(Cursor cursor){
        ArrayList<Game> games = new ArrayList<>();
        while (cursor.moveToNext()){
            Game game = new Game();
            game.setComment(cursor.getString(cursor.getColumnIndex(GameEntry.COMMENT)));
            game.setCover(cursor.getString(cursor.getColumnIndex(GameEntry.COVER)));
            game.setDescription(cursor.getString(cursor.getColumnIndex(GameEntry.DESCRIPTION)));
            game.setDevelopers(cursor.getString(cursor.getColumnIndex(GameEntry.DEVELOPER)));
            game.setFavourite(cursor.getInt(cursor.getColumnIndex(GameEntry.FAVOURITE)) == 1);
            game.setGenres(cursor.getString(cursor.getColumnIndex(GameEntry.GENRES)));
            game.setId(cursor.getString(cursor.getColumnIndex(GameEntry.ID)));
            game.setName(cursor.getString(cursor.getColumnIndex(GameEntry.NAME)));
            game.setPlatform(cursor.getString(cursor.getColumnIndex(GameEntry.PLATFORM)));
            game.setPublishers(cursor.getString(cursor.getColumnIndex(GameEntry.PUBLISHER)));
            game.setRating(cursor.getString(cursor.getColumnIndex(GameEntry.RATING)));
            game.setReleaseDate(cursor.getString(cursor.getColumnIndex(GameEntry.RELEASEDATE)));
            game.setScore(cursor.getInt(cursor.getColumnIndex(GameEntry.SCORE)));
            game.setStatus(cursor.getInt(cursor.getColumnIndex(GameEntry.STATUS)));
            game.setStartDate(cursor.getString(cursor.getColumnIndex(GameEntry.START_DATE)));
            game.setFinishDate(cursor.getString(cursor.getColumnIndex(GameEntry.FINISH_DATE)));
            game.setPlaytime(cursor.getDouble(cursor.getColumnIndex(GameEntry.PLAYTIME)));
            game.setReplaying(cursor.getInt(cursor.getColumnIndex(GameEntry.REPLAYING)) == 1);
            games.add(game);
        }
        return games;
    }

    /**
     * Obtiene los juegos que has terminado hoy
     * @param wildcardDate la fecha de finalización en forma de wildcard (*-10-05 por ejemplo)
     * @return los juegos que terminaste en la fecha indicada
     */
    public ArrayList<Game> getGamesFinishedToday(String wildcardDate){
        Cursor cursor = db.rawQuery("select " + GameEntry.NAME + ", " + GameEntry.FINISH_DATE + " from " + GameEntry.TABLE_NAME + " where " + GameEntry.FINISH_DATE + " like '" + wildcardDate + "'", null);
        ArrayList<Game> games = new ArrayList<>();
        while (cursor.moveToNext()){
            Game game = new Game();
            game.setName(cursor.getString(cursor.getColumnIndex(GameEntry.NAME)));
            game.setFinishDate(cursor.getString(cursor.getColumnIndex(GameEntry.FINISH_DATE)));
            games.add(game);
        }
        cursor.close();
        return games;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + GameEntry.TABLE_NAME
                + " (" + GameEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + GameEntry.ID + " TEXT NOT NULL,"
                + GameEntry.COVER + " TEXT,"
                + GameEntry.NAME + " TEXT NOT NULL,"
                + GameEntry.PLATFORM + " TEXT,"
                + GameEntry.DEVELOPER + " TEXT,"
                + GameEntry.PUBLISHER + " TEXT,"
                + GameEntry.RELEASEDATE + " TEXT,"
                + GameEntry.DESCRIPTION + " TEXT,"
                + GameEntry.RATING + " TEXT,"
                + GameEntry.GENRES + " TEXT,"
                + GameEntry.SCORE + " INTEGER,"
                + GameEntry.STATUS + " INTEGER NOT NULL,"
                + GameEntry.COMMENT + " TEXT,"
                + GameEntry.FAVOURITE + " BOOLEAN NOT NULL DEFAULT 0,"
                + GameEntry.START_DATE + " DATE,"
                + GameEntry.FINISH_DATE + " DATE,"
                + GameEntry.PLAYTIME + " DECIMAL(5, 2),"
                + GameEntry.REPLAYING + " BOOLEAN NOT NULL DEFAULT 0,"
                + "UNIQUE (" + GameEntry.ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
}
