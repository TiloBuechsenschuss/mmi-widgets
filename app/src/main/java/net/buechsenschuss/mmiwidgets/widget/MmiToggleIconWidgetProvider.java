package net.buechsenschuss.mmiwidgets.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import net.buechsenschuss.mmiwidgets.MmiDispatchActivity;
import net.buechsenschuss.mmiwidgets.R;
import net.buechsenschuss.mmiwidgets.data.MmiActions;
import net.buechsenschuss.mmiwidgets.data.SettingsStore;
import net.buechsenschuss.mmiwidgets.model.MmiAction;

/**
 * Compact 1x1 home-screen widget that toggles unconditional call forwarding with a single tap.
 *
 * <p>Same behaviour as {@link MmiWidgetProvider} (both control
 * {@link MmiActions#ID_CALL_FORWARDING} via {@link MmiDispatchActivity}), but instead of a text
 * label this widget shows a phone-handset icon whose colour reflects the last known state: green
 * when forwarding is on, red when it is off. The icon is a single white drawable recoloured at
 * runtime with {@code setColorFilter}, so only one glyph asset is needed.</p>
 */
public final class MmiToggleIconWidgetProvider extends AppWidgetProvider {

    /** The action this widget controls. Matches the 2x1 widget. */
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

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_mmi_1x1);

        if (action == null) {
            // No label view here; leave the icon red and skip the click intent.
            views.setInt(R.id.widget_icon, "setColorFilter",
                    ContextCompat.getColor(context, R.color.phone_off));
            appWidgetManager.updateAppWidget(appWidgetId, views);
            return;
        }

        boolean enabled = settings.isEnabled(action.getId());
        views.setInt(R.id.widget_icon, "setColorFilter",
                ContextCompat.getColor(context, enabled ? R.color.phone_on : R.color.phone_off));
        views.setContentDescription(R.id.widget_icon, context.getString(
                enabled ? R.string.widget_forwarding_on : R.string.widget_forwarding_off));

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
        ComponentName provider = new ComponentName(context, MmiToggleIconWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(provider);
        for (int id : ids) {
            updateWidget(context, manager, id);
        }
    }
}
