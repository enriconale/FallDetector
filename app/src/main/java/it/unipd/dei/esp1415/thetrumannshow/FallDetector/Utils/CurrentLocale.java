package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils;

import android.content.Context;
import android.os.Build;

import java.util.Locale;

/**
 * @author Enrico Naletto
 */

public class CurrentLocale {
    public static Locale getCurrentLocale(Context applicationContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return applicationContext.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            return applicationContext.getResources().getConfiguration().locale;
        }
    }
}
