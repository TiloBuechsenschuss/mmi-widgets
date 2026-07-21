package com.example.mmi_widgets.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

/**
 * Small wrapper around {@link SharedPreferences} for everything the app needs to persist:
 * the user's forwarding number and the last known on/off state of each action.
 *
 * <p>The stored on/off state is the app's <em>optimistic</em> view. Sending an MMI code does not
 * give a reliable success callback, so the app assumes the toggle worked and flips the flag. If the
 * two drift apart, the user can correct it from the main screen. A future improvement could query
 * the network state instead of trusting this flag.</p>
 */
public final class SettingsStore {

    private static final String PREFS_NAME = "mmi_widgets_prefs";
    private static final String KEY_NUMBER = "forward_number";
    private static final String KEY_ENABLED_PREFIX = "enabled_";

    private final SharedPreferences prefs;

    public SettingsStore(@NonNull Context context) {
        // Use application context so this can be created from an Activity, a widget or a service.
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** The phone number MMI codes forward to. Empty string when unset. */
    @NonNull
    public String getForwardNumber() {
        return prefs.getString(KEY_NUMBER, "");
    }

    public void setForwardNumber(@NonNull String number) {
        prefs.edit().putString(KEY_NUMBER, number.trim()).apply();
    }

    /** Last known state for an action id. Defaults to {@code false} (off). */
    public boolean isEnabled(@NonNull String actionId) {
        return prefs.getBoolean(KEY_ENABLED_PREFIX + actionId, false);
    }

    public void setEnabled(@NonNull String actionId, boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLED_PREFIX + actionId, enabled).apply();
    }
}
