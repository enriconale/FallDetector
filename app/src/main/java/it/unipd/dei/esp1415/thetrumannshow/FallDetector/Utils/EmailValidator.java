package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils;

import android.content.SharedPreferences;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Activities.SettingsActivity;

/**
 * @author Enrico Naletto
 *         EmailValidator class
 */

public class EmailValidator {
    public static boolean isValidEmailAddress(String email) {
        String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
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

        return ((!EmailValidator.isValidEmailAddress(emailString1))
                && (!EmailValidator.isValidEmailAddress(emailString2))
                && (!EmailValidator.isValidEmailAddress(emailString3))
                && (!EmailValidator.isValidEmailAddress(emailString4))
                && (!EmailValidator.isValidEmailAddress(emailString5)));
    }
}
