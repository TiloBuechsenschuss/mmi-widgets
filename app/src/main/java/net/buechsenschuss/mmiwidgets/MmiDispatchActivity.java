package net.buechsenschuss.mmiwidgets;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import net.buechsenschuss.mmiwidgets.data.SettingsStore;
import net.buechsenschuss.mmiwidgets.mmi.MmiSender;
import net.buechsenschuss.mmiwidgets.model.MmiAction;
import net.buechsenschuss.mmiwidgets.data.MmiActions;
import net.buechsenschuss.mmiwidgets.widget.MmiToggleIconLabelWidgetProvider;
import net.buechsenschuss.mmiwidgets.widget.MmiToggleIconWidgetProvider;
import net.buechsenschuss.mmiwidgets.widget.MmiWidgetProvider;

/**
 * Invisible activity that actually executes an MMI code and then finishes.
 *
 * <p>Everything that wants to fire a code &mdash; the main screen and the home-screen widget &mdash;
 * routes through here so there is a single place that:
 * <ul>
 *     <li>resolves the code (enable vs disable, number substitution),</li>
 *     <li>handles the {@link Manifest.permission#CALL_PHONE} runtime permission,</li>
 *     <li>updates the persisted on/off state and refreshes widgets.</li>
 * </ul>
 *
 * <p>It has no layout and uses a translucent theme, so from the user's point of view tapping a
 * widget just triggers the dialer.</p>
 */
public final class MmiDispatchActivity extends Activity {

    /** Extra (String): the {@link MmiAction} id to operate on. */
    public static final String EXTRA_ACTION_ID = "action_id";

    /**
     * Extra (String): what to do. One of {@link #MODE_TOGGLE}, {@link #MODE_ENABLE},
     * {@link #MODE_DISABLE}. Defaults to toggle.
     */
    public static final String EXTRA_MODE = "mode";

    public static final String MODE_TOGGLE = "toggle";
    public static final String MODE_ENABLE = "enable";
    public static final String MODE_DISABLE = "disable";

    private static final int REQUEST_CALL_PERMISSION = 1;

    private SettingsStore settings;
    private MmiAction action;
    private boolean targetEnabled;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new SettingsStore(this);

        String actionId = getIntent().getStringExtra(EXTRA_ACTION_ID);
        action = MmiActions.byId(actionId);
        if (action == null) {
            Toast.makeText(this, R.string.error_unknown_action, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (action.isOneShot()) {
            // No target state and no number needed; just dial the single code.
            targetEnabled = true;
        } else {
            targetEnabled = resolveTargetState(getIntent().getStringExtra(EXTRA_MODE), action);

            if (action.requiresNumber() && targetEnabled && settings.getForwardNumber().isEmpty()) {
                Toast.makeText(this, R.string.error_no_number, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        if (MmiSender.hasCallPermission(this)) {
            performDispatch();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        }
    }

    private boolean resolveTargetState(@Nullable String mode, @NonNull MmiAction action) {
        if (MODE_ENABLE.equals(mode)) {
            return true;
        }
        if (MODE_DISABLE.equals(mode)) {
            return false;
        }
        // Default: toggle relative to the last known state.
        return !settings.isEnabled(action.getId());
    }

    private void performDispatch() {
        String code = action.resolveCode(targetEnabled, settings.getForwardNumber());
        try {
            MmiSender.dial(this, code);
            if (!action.isOneShot()) {
                // Optimistically record the new state and refresh any widgets.
                settings.setEnabled(action.getId(), targetEnabled);
                MmiWidgetProvider.refreshAll(this);
                MmiToggleIconWidgetProvider.refreshAll(this);
                MmiToggleIconLabelWidgetProvider.refreshAll(this);
            }
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.error_call_permission, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_dial_failed, e.getMessage()),
                    Toast.LENGTH_LONG).show();
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            performDispatch();
        } else {
            Toast.makeText(this, R.string.error_call_permission, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /** Builds an intent that dispatches the given action in the given mode. */
    @NonNull
    public static Intent createIntent(@NonNull Context context,
                                      @NonNull String actionId,
                                      @NonNull String mode) {
        Intent intent = new Intent(context, MmiDispatchActivity.class);
        intent.putExtra(EXTRA_ACTION_ID, actionId);
        intent.putExtra(EXTRA_MODE, mode);
        return intent;
    }
}
