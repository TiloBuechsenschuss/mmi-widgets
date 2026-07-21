package com.example.mmi_widgets.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.example.mmi_widgets.MmiDispatchActivity;
import com.example.mmi_widgets.R;
import com.example.mmi_widgets.data.MmiActions;
import com.example.mmi_widgets.data.SettingsStore;
import com.example.mmi_widgets.model.MmiAction;

/**
 * Home-screen widget that toggles one MMI function with a single tap.
 *
 * <p>For the skeleton every instance of this widget controls the same action
 * ({@link MmiActions#ID_CALL_FORWARDING}). The label reflects the last known on/off state stored in
 * {@link SettingsStore}. Tapping starts {@link MmiDispatchActivity} in toggle mode, which dials the
 * code, updates the stored state and calls {@link #refreshAll(Context)} to redraw the widget.</p>
 *
 * <p>To support per-widget actions later, store the chosen action id keyed by {@code appWidgetId}
 * (typically via a widget configuration activity) and read it in {@link #updateWidget}.</p>
 */
public final class MmiWidgetProvider extends AppWidgetProvider {

    /** The action this widget controls. Kept simple for the skeleton. */
    private static final String WIDGET_ACTION_ID = MmiActions.ID_CALL_FORWARDING;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private static void updateWidget(@NonNull Context context,
                                     @NonNull AppWidgetManager appWidgetManager,
                                     int appWidgetId) {
        SettingsStore settings = new SettingsStore(context);
        MmiAction action = MmiActions.byId(WIDGET_ACTION_ID);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_mmi);

        if (action == null) {
            views.setTextViewText(R.id.widget_label, context.getString(R.string.error_unknown_action));
            appWidgetManager.updateAppWidget(appWidgetId, views);
            return;
        }

        boolean enabled = settings.isEnabled(action.getId());
        views.setTextViewText(R.id.widget_label, action.getLabel());
        views.setTextViewText(R.id.widget_state,
                context.getString(enabled ? R.string.state_on : R.string.state_off));

        Intent dispatch = MmiDispatchActivity.createIntent(
                context, action.getId(), MmiDispatchActivity.MODE_TOGGLE);
        // Use the widget id as the request code so multiple widgets get distinct PendingIntents.
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                dispatch,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /** Redraws every instance of this widget. Call after the stored state changes. */
    public static void refreshAll(@NonNull Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, MmiWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(provider);
        for (int id : ids) {
            updateWidget(context, manager, id);
        }
    }
}
