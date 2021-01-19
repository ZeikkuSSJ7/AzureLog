package com.zeikkussj.azurelog.settings;

/**
 * User
 */
public class User {
    public String name;
    String joined;
    public int totalGames;
    public int playing;
    public int planToPlay;
    public int onHold;
    public int dropped;
    public int completed;
    public int mastered;
    public int totalCompletedGames;
    public float meanScore;

    /**
     * Crea un usuario con todas las opciones
     * @param name el nombre de usuario
     * @param joined la fecha en la que se unió
     * @param playing los juegos que está jugando
     * @param planToPlay los juegos que quiere jugar
     * @param onHold los juegos que tiene en espera
     * @param dropped los juegos que ha abandonado
     * @param completed los juegos que ha completado
     * @param mastered los juegos que ha dominado
     * @param meanScore la media de nota de los juegos que ha terminado
     */
    private User(String name, String joined, int playing, int planToPlay, int onHold, int dropped, int completed, int mastered, float meanScore) {
        this.completed = completed;
        this.dropped = dropped;
        this.joined = joined;
        this.mastered = mastered;
        this.name = name;
        this.onHold = onHold;
        this.planToPlay = planToPlay;
        this.playing = playing;
        totalGames = planToPlay + playing + completed + mastered + dropped + onHold;
        totalCompletedGames = completed + mastered;
        this.meanScore = meanScore;
    }

    /**
     * Crea un usuario por defecto
     * @param name el nombre de usuario
     * @param joined la fecha en la que se unió
     */
    public User(String name, String joined){
        this(name, joined, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Limpia los valores de un usuario ya creado. Usado para reconstruir sus valores.
     */
    public void putValuesToZero(){
        totalGames = 0;
        playing = 0;
        planToPlay = 0;
        onHold = 0;
        dropped = 0;
        completed = 0;
        mastered = 0;
        totalCompletedGames = 0;
        meanScore = 0;
    }
}