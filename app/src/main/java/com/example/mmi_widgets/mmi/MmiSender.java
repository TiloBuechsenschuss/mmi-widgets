package com.example.mmi_widgets.mmi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * Turns an MMI code string into a dialed request.
 *
 * <p>MMI / supplementary-service codes (call forwarding, call waiting, ...) are executed by
 * dialing them like a phone number. We build a {@code tel:} URI with {@link Uri#fromParts} so the
 * {@code #} characters are percent-encoded correctly, then fire {@link Intent#ACTION_CALL}.</p>
 *
 * <p>{@code ACTION_CALL} requires the {@link Manifest.permission#CALL_PHONE} runtime permission.
 * Callers should use {@link #hasCallPermission(Context)} first and request it if needed &mdash;
 * see {@link com.example.mmi_widgets.MmiDispatchActivity}.</p>
 */
public final class MmiSender {

    private MmiSender() {
    }

    /** Whether the app currently holds the runtime permission needed to dial MMI codes. */
    public static boolean hasCallPermission(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Dials the given MMI code.
     *
     * @param context any context; {@code FLAG_ACTIVITY_NEW_TASK} is added so this also works from
     *                a non-activity context such as a widget broadcast.
     * @param code    a fully resolved MMI code, e.g. {@code **21*123456#}.
     * @throws SecurityException if {@link Manifest.permission#CALL_PHONE} is not granted.
     */
    public static void dial(@NonNull Context context, @NonNull String code) {
        Uri uri = Uri.fromParts("tel", code, null);
        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
