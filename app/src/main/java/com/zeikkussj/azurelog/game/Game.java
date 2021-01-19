package com.zeikkussj.azurelog.game;

import android.content.ContentValues;

import com.zeikkussj.azurelog.game.GameConstants.GameEntry;
import com.zeikkussj.azurelog.util.StaticFields;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

public class Game {
    // ID
    private String id;

    // General game data
    private String cover;
    private String game_title;
    private String platform;
    private String developers;
    private String publishers;
    private String release_date;
    private String overview;
    private String rating;
    private String genres;

    // Game-specific data
    private int score;
    private int status;
    private String comment;
    private boolean favourite;
    private String startDate;
    private String finishDate;
    private double playtime;
    private boolean replaying;


    /**
     * Construye un nuevo juego según los parametros indicados
     * @param id el id del juego
     * @param cover el enlace de la carátula del juego
     * @param name el nombre común del juego
     * @param platform la plataforma del juego
     * @param developer el desarrollador del juegi
     * @param publisher el publicador del juego
     * @param releaseDate la fecha de salida del juego
     * @param description la descripción del juego
     * @param rating la clasificación del juego
     * @param genres los géneros del juego
     */
    private Game(String id, String cover, String name, String platform, String developer, String publisher, String releaseDate, String description, String rating, String genres){
        this.id = id;
        this.cover = cover;
        this.game_title = name;
        this.platform = platform;
        this.developers = developer;
        this.publishers = publisher;
        this.release_date = releaseDate;
        this.overview = description;
        this.rating = rating;
        this.genres = genres;
        status = StaticFields.STATUSES_IDS[0];
        score = 0;
        comment = "None";
        favourite = false;
        this.startDate = null;
        this.finishDate = null;
        this.playtime = 0d;
        this.replaying = false;
    }

    /**
     * Construye un juego con parámetros más simples, usado en {@link com.zeikkussj.azurelog.search.NewGameActivity}
     * @param id el id del juego
     * @param cover el enlace de la carátula del juego, en caso de tenerlo
     * @param data los datos extra del juego
     */
    public Game(String id, String cover, String[] data){
        this(id, cover, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]);
    }

    /**
     * Construye un juego con los datos de otro de base
     * @param data los datos de un juego
     */
    public Game(String[] data){
        this(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
    }

    /**
     * Construye un juego con datos por defecto
     */
    public Game(){
        status = StaticFields.STATUSES_IDS[0];
        score = 0;
        comment = "None";
        favourite = false;
        this.startDate = null;
        this.finishDate = null;
        this.playtime = 0d;
        this.replaying = false;
    }

    public String getName() {
        return game_title;
    }

    public String getId() {
        return id;
    }

    public String getPlatform() {
        return platform;
    }

    public String getRelease_date() {
        return release_date;
    }

    public String getCover() {
        return cover;
    }

    public int getStatus() {
        return status;
    }

    public int getScore() {
        return score;
    }

    public String getFinishDate() {
        return finishDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getComment() {
        return comment;
    }

    public double getPlaytime() {
        return playtime;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public boolean isReplaying() {
        return replaying;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.game_title = name;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setDescription(String description) {
        this.overview = description;
    }

    public void setDevelopers(String developers) {
        this.developers = developers;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setPublishers(String publishers) {
        this.publishers = publishers;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setReleaseDate(String releaseDate) {
        this.release_date = releaseDate;
    }

    public void setScore(int score) {
        this.score = score;
    }

    void setComment(String comment) {
        this.comment = comment;
    }

    void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    void setFinishDate(String finishDate) {
        this.finishDate = finishDate;
    }

    void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    void setPlaytime(double playtime) {
        this.playtime = playtime;
    }

    void setReplaying(boolean replaying) {
        this.replaying = replaying;
    }

    /**
     * Transforma un juego a {@link ContentValues} para ser insertado
     * o actualizado
     * @return el juego en forma de {@link ContentValues}
     */
    ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(GameEntry.ID, id);
        cv.put(GameEntry.COVER, cover);
        cv.put(GameEntry.NAME, game_title);
        cv.put(GameEntry.PLATFORM, platform);
        cv.put(GameEntry.DEVELOPER, developers);
        cv.put(GameEntry.PUBLISHER, publishers);
        cv.put(GameEntry.RELEASEDATE, release_date);
        cv.put(GameEntry.DESCRIPTION, overview);
        cv.put(GameEntry.RATING, rating);
        cv.put(GameEntry.GENRES, genres);
        cv.put(GameEntry.SCORE, score);
        cv.put(GameEntry.STATUS, status);
        cv.put(GameEntry.COMMENT, comment);
        cv.put(GameEntry.FAVOURITE, favourite);
        cv.put(GameEntry.START_DATE, startDate);
        cv.put(GameEntry.FINISH_DATE, finishDate);
        cv.put(GameEntry.PLAYTIME, playtime);
        cv.put(GameEntry.REPLAYING, replaying);
        return cv;
    }

    /**
     * Transforma un juego a un array de {@link String} para mostrar
     * sus datos
     * @return los datos del juego como array
     */
    public String[] toArray(){
        return new String[]{id, cover, game_title, platform, developers, publishers, release_date, overview, rating, genres, String.valueOf(score), String.valueOf(status), comment, String.valueOf(favourite), startDate, finishDate, new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(playtime), String.valueOf(replaying)};
    }
}
