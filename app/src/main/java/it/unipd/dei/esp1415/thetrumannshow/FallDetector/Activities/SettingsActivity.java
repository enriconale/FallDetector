package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.Toast;

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.R;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.EmailValidator;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.SessionsLab;

public class SettingsActivity extends AppCompatPreferenceActivity {
    public static final String PREF_DAILY_NOTIFICATION = "pref_daily_notification";
    public static final String PREF_ONGOING_NOTIFICATION = "pref_ongoing_notification";
    public static final String PREF_ACCELEROMETER_RATE = "pref_accelerometer_rate";
    public static final String PREF_EMAIL_ADDRESS1 = "pref_email_address1";
    public static final String PREF_EMAIL_ADDRESS2 = "pref_email_address2";
    public static final String PREF_EMAIL_ADDRESS3 = "pref_email_address3";
    public static final String PREF_EMAIL_ADDRESS4 = "pref_email_address4";
    public static final String PREF_EMAIL_ADDRESS5 = "pref_email_address5";
    public static final String PREF_SESSION_DURATION = "pref_session_duration";
    private static Context mAppContext;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference instanceof EditTextPreference) {
                EditTextPreference e = (EditTextPreference) preference;
                String title = e.getTitle().toString();
                if (title.contains("#")) {
                    String val = (String) value;
                    if ("".equals(val) || val == null) {
                        preference.setSummary(mAppContext.getString(R.string.pref_email_summary_nomailset));
                    } else {
                        if (EmailValidator.isValidEmailAddress(val)) {
                            preference.setSummary(val);
                        } else {
                            mAppContext.getSharedPreferences(e.getKey(), 0).edit().clear().apply();
                            preference.setSummary(R.string.pref_email_summary_nomailset);
                            ((EditTextPreference) preference).setText("");
                            Toast.makeText(mAppContext, R.string.pref_invalid_email, Toast.LENGTH_LONG)
                                    .show();
                        }

                    }
                } else if (title.contains(mAppContext.getString(R.string.pref_session_duration_title))) {
                    try {
                        int k = Integer.parseInt(e.getText());
                    } catch (NumberFormatException j) {
                        Toast.makeText(mAppContext, R.string.pref_invalid_session_duration,
                                Toast.LENGTH_LONG).show();
                        ((EditTextPreference) preference).setText("12");
                    }
                }
                if (SessionsLab.get(mAppContext).hasRunningSession()) {
                    SessionsLab.get(mAppContext).pauseCurrentlyRunningSession();
                    SessionsLab.get(mAppContext).resumeCurrentlyRunningSession();
                }
            }
            return true;
        }
    };

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        mAppContext = getApplicationContext();
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new GeneralPreferenceFragment()).commit();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(PREF_EMAIL_ADDRESS1));
            bindPreferenceSummaryToValue(findPreference(PREF_EMAIL_ADDRESS2));
            bindPreferenceSummaryToValue(findPreference(PREF_EMAIL_ADDRESS3));
            bindPreferenceSummaryToValue(findPreference(PREF_EMAIL_ADDRESS4));
            bindPreferenceSummaryToValue(findPreference(PREF_EMAIL_ADDRESS5));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), MainActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
