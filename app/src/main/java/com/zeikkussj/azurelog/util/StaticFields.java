package com.zeikkussj.azurelog.util;

public abstract class StaticFields {

    // Intent keys

    public static final String KEY_INFO_DATA = "DATA";
    public static final String KEY_GAME_POS = "GAME_POS";

    // Game fields

    public static final String CUSTOM_GAME_SUFFIX = "custom";
    public static final String DEFAULT_COVER_EXTENSION = ".jpg";
    public static final int[] STATUSES_IDS = {
            0, // Plan to play
            1, // Playing
            2, // On hold
            3, // Dropped
            4, // Completed
            5 // Mastered
    };

    // Notification & channel IDs

    static final String NOTIFICATION_CHANNEL_ID = "5576";
    static final int BACKUP_NOTIFICATION_ID = -1;

    // Settings and Profile fields

    static final String FTP_BACKUP_FOLDER = "/databaseBackup/AzureLog";
    static final String FTP_USERNAME = "androidDatabaseUser";
    public static final String USER_DIRECTORY = "/userDir";
    private static final String JSON_DATA_FILE = "/user.json";
    public static final String JSON_DATA_DIRECTORY = USER_DIRECTORY + JSON_DATA_FILE;
    public static final String PROFILE_PIC = "/pic" + DEFAULT_COVER_EXTENSION;
    static final String BACKUP_IMAGES_ZIP_FILENAME = "/images.zip";
    static final String BACKUP_DATABASE_FILENAME = "/games.db";

    // API Keys

    static final String THEGAMESDB_PUBLIC_KEY = "38b7758ab9129e6ca4322e5b87310b52f528cff67a1cb8577fa1db260f20b247";

    // Permissions

    public static final int PERMISSION_OK = 0;

    // Image saving folder

    static final String IMAGE_FOLDER = "/AzureLog";

}
