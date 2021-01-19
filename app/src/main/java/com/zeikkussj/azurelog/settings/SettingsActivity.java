package com.zeikkussj.azurelog.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.util.Sync;
import com.zeikkussj.azurelog.util.Util;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

public class SettingsActivity extends AppCompatActivity {
    private static SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        Util.openToolbar(this, R.id.my_toolbar);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        SwitchPreference backupDaily;
        ListPreference backupDailyHour;
        Preference syncSend;
        EditTextPreference syncIP;
        EditTextPreference syncUsername;
        EditTextPreference syncPassword;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            // Obtención de las preferecias necesarias
            backupDailyHour = findPreference("backupDailyHour");
            backupDaily = findPreference("backupDaily");
            syncSend = findPreference("syncSend");
            syncIP = findPreference("syncIP");
            syncUsername = findPreference("syncUsername");
            syncPassword = findPreference("syncPassword");

            lockSettings(preferences.getBoolean("backupDaily", false));
            backupDaily.setOnPreferenceChangeListener((preference, newValue) -> {
                lockSettings((boolean) newValue);
                return true;
            });

            if (syncIP.getText() == null || syncIP.getText().isEmpty()) // Si no hay IP de Silent Azure, se incluye la de por defecto
                syncIP.setText("silentazurenet.ddns.net");

            syncSend.setOnPreferenceClickListener((preference) ->{
                Sync.sendDatabaseChanges(getActivity(), getContext(), syncIP.getText(), syncUsername.getText(), syncPassword.getText());
                return true;
            });
        }

        /**
         * Bloquea la hora de la copia de seguridad según el estado de la opción <code>backupDaily</code>
         * @param activated el estado de <code>backupDaily</code>
         */
        private void lockSettings(boolean activated){
            backupDailyHour.setEnabled(activated);
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Util.onOptionItemSelected(item, this, null);
        return super.onOptionsItemSelected(item);
    }

    /**
     * Impide la salida del Activity si la opción de <code>backupDaily</code> está activada
     * sin una IP y hora ajustadas
     */
    @Override
    public void onBackPressed() {

        boolean backUpDaily = preferences.getBoolean("backupDaily", false);
        if (backUpDaily){
            String ip = preferences.getString("backupIP", "");
            String backUpDailyHour = preferences.getString("backupDailyHour", "");
            if (ip.isEmpty() || backUpDailyHour.isEmpty()){
                Toast.makeText(this, R.string.ifYouWantABackupDaily, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        super.onBackPressed();
    }
}