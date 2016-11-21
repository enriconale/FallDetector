package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils;

import android.content.SharedPreferences;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Activities.SettingsActivity;

/**
 * @author Enrico Naletto
 *         Helper class
 */

public class Helper {
    public static boolean isValidEmailAddress(String email) {
        String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    public static String getFormattedSessionName(String sessionName) {
        int maxCharactersToShow = 30;
        if (sessionName.length() > maxCharactersToShow) {
            return sessionName.substring(0, maxCharactersToShow) + "...";
        }

        return sessionName;
    }

    public static boolean hasIncorrectEmailSettings(SharedPreferences sharedPreferences) {
        String emailString1 = sharedPreferences.getString(SettingsActivity
                .PREF_EMAIL_ADDRESS1, "");
        String emailString2 = sharedPreferences.getString(SettingsActivity
                .PREF_EMAIL_ADDRESS2, "");
        String emailString3 = sharedPreferences.getString(SettingsActivity
                .PREF_EMAIL_ADDRESS3, "");
        String emailString4 = sharedPreferences.getString(SettingsActivity
                .PREF_EMAIL_ADDRESS4, "");
        String emailString5 = sharedPreferences.getString(SettingsActivity
                .PREF_EMAIL_ADDRESS5, "");

        return ((!Helper.isValidEmailAddress(emailString1))
                && (!Helper.isValidEmailAddress(emailString2))
                && (!Helper.isValidEmailAddress(emailString3))
                && (!Helper.isValidEmailAddress(emailString4))
                && (!Helper.isValidEmailAddress(emailString5)));
    }
}
