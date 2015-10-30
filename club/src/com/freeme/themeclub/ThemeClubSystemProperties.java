package com.freeme.themeclub;

import android.os.SystemProperties;

public class ThemeClubSystemProperties {
    public static final boolean DROI_ADROI_SUPPORT = isPropertyEnabled("ro.droi_adroi_support");
    
    private static boolean isPropertyEnabled(String propertyString) {
        return SystemProperties.get(propertyString).equals("1");
    }
}
