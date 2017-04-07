package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Activities;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.Toast;

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.R;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.Helper;
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
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference instanceof EditTextPreference) {
                EditTextPreference e = (EditTextPreference) preference;
                String title = e.getTitle().toString();
                if (title.contains("#")) {
                    if ("".equals(e.getText()) || e.getText() == null) {
                        preference.setSummary(mAppContext.getString(R.string.pref_email_summary_nomailset));
                    } else {
                        if (Helper.isValidEmailAddress(e.getText())) {
                            preference.setSummary(e.getText());
                        } else {
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

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
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

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
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
