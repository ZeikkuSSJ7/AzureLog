package com.zeikkussj.azurelog.game;

import android.provider.BaseColumns;

/**
 * La clase GameConstants contiene todas los nombres de tabla de la base de datos
 */
public class GameConstants {

    public static class GameEntry implements BaseColumns {
        public static final String TABLE_NAME = "game";
        public static final String ID = "id";

        public static final String COVER = "cover";
        public static final String NAME = "name";
        public static final String PLATFORM = "platform";
        public static final String DEVELOPER = "developer";
        public static final String PUBLISHER = "publisher";
        public static final String RELEASEDATE = "releaseDate";
        public static final String DESCRIPTION = "description";
        public static final String RATING = "rating";
        public static final String GENRES = "genres";

        public static final String SCORE = "score";
        public static final String STATUS = "status";
        public static final String COMMENT = "comment";
        public static final String FAVOURITE = "favourite";
        public static final String START_DATE = "startDate";
        public static final String FINISH_DATE = "finishDate";
        public static final String PLAYTIME = "playtime";
        public static final String REPLAYING = "replaying";
    }
}
