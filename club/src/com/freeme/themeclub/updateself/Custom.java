package com.freeme.themeclub.updateself;

public class Custom {
    public static final String APP_ID = "kbmh00001";
    public static final String CHANNEL_ID = "koobee";
    public static final String UPDATE_DIR_NAME = "UpdateSelf";
    public static final String UPDATE_DIR_PATH = "/FreemeThemeClub/" + UPDATE_DIR_NAME;

    private static final String WIDGET_UPDATE_QUERY_URL = "http://update-osmarket.tt286.com:2520";

    public static String getUpdateQueryUrl() {
        return WIDGET_UPDATE_QUERY_URL;
    }
}
