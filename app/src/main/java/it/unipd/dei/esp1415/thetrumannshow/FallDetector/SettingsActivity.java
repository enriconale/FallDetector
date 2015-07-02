package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Enrico Naletto
 */
public class SettingsActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String PREF_DAILY_NOTIFICATION = "pref_daily_notification";
    public static final String PREF_ONGOING_NOTIFICATION = "pref_ongoing_notification";
    public static final String PREF_ACCELEROMETER_RATE = "pref_accelerometer_rate";
    public static final String PREF_EMAIL_ADDRESS1 = "pref_email_address1";
    public static final String PREF_EMAIL_ADDRESS2 = "pref_email_address2";
    public static final String PREF_EMAIL_ADDRESS3 = "pref_email_address3";
    public static final String PREF_EMAIL_ADDRESS4 = "pref_email_address4";
    public static final String PREF_EMAIL_ADDRESS5 = "pref_email_address5";
    public static final String PREF_SESSION_DURATION = "pref_session_duration";

    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        PreferenceManager.setDefaultValues(getBaseContext(), R.xml.preferences, false);

        initSummary(getPreferenceScreen());
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Toolbar bar;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
            bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
            root.addView(bar, 0); // insert at top
        } else {
            ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            ListView content = (ListView) root.getChildAt(0);

            root.removeAllViews();

            bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);


            int height;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            } else {
                height = bar.getHeight();
            }

            content.setPadding(0, height, 0, 0);

            root.addView(content);
            root.addView(bar);
        }

        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        verifySettings(findPreference(key));
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            verifySettings(p);
        }
    }

    //Method needed to verify if the user set correctly the preferences
    private void verifySettings(Preference p) {
        if (p instanceof EditTextPreference) {
            EditTextPreference e = (EditTextPreference) p;
            String title = e.getTitle().toString();
            if (title.contains("#")) {
                if ("".equals(e.getText()) || e.getText()==null) {
                    p.setSummary(getApplicationContext().getString(R.string.pref_email_summary_nomailset));
                } else {
                    if (isValidEmailAddress(e.getText())) {
                        p.setSummary(e.getText());
                    } else {
                        p.setSummary(R.string.pref_email_summary_nomailset);
                        ((EditTextPreference) p).setText("");
                        Toast.makeText(getApplicationContext(), R.string.pref_invalid_email, Toast.LENGTH_LONG)
                                .show();
                    }

                }
            } else if (title.contains(getApplicationContext().getString(R.string.pref_session_duration_title))) {
                try {
                    int k = Integer.parseInt(e.getText());
                } catch (NumberFormatException j) {
                    Toast.makeText(getApplicationContext(), R.string.pref_invalid_session_duration,
                            Toast.LENGTH_LONG).show();
                    ((EditTextPreference) p).setText("12");
                }
            }

            SessionsLab.get(getApplicationContext()).pauseCurrentlyRunningSession();
            SessionsLab.get(getApplicationContext()).resumeCurrentlyRunningSession();
        }
    }

    private boolean isValidEmailAddress(String email) {
        String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
