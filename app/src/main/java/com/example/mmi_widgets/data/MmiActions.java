package com.example.mmi_widgets.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mmi_widgets.model.MmiAction;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of the MMI functions the app knows about.
 *
 * <p>This is intentionally a hard-coded list for the skeleton. The standard GSM supplementary
 * service codes below work on most networks, but carriers differ - treat these as sane
 * defaults that can later be made editable by the user.</p>
 *
 * <p>To add a new toggle, add another {@link MmiAction} here and (if it should have its own
 * home-screen widget) reuse the same widget provider. Nothing else needs to change.</p>
 */
public final class MmiActions {

    /** Unconditional call forwarding: forward every incoming call to the saved number. */
    public static final String ID_CALL_FORWARDING = "call_forwarding_unconditional";

    /** Call forwarding when busy. */
    public static final String ID_FORWARD_BUSY = "call_forwarding_busy";

    /** Call forwarding when unanswered. */
    public static final String ID_FORWARD_NO_REPLY = "call_forwarding_no_reply";

    /** Interrogation code: ask the network to show the current call-forwarding configuration. */
    public static final String ID_CHECK_FORWARDING = "call_forwarding_status";

    private static final Map<String, MmiAction> ACTIONS = buildRegistry();

    private MmiActions() {
    }

    private static Map<String, MmiAction> buildRegistry() {
        Map<String, MmiAction> map = new LinkedHashMap<>();

        put(map, new MmiAction(
                ID_CALL_FORWARDING,
                "Call forwarding (all)",
                "**21*" + MmiAction.NUMBER_PLACEHOLDER + "#",
                "##21#",
                true));

        put(map, new MmiAction(
                ID_FORWARD_BUSY,
                "Forward when busy",
                "**67*" + MmiAction.NUMBER_PLACEHOLDER + "#",
                "##67#",
                true));

        put(map, new MmiAction(
                ID_FORWARD_NO_REPLY,
                "Forward when no reply",
                "**61*" + MmiAction.NUMBER_PLACEHOLDER + "#",
                "##61#",
                true));

        // *#002# interrogates all call-forwarding settings at once. The network responds with a
        // system dialog; the app cannot read the result, so this is a manual verification aid.
        put(map, MmiAction.oneShot(
                ID_CHECK_FORWARDING,
                "Check forwarding status",
                "*#002#"));

        return Collections.unmodifiableMap(map);
    }

    private static void put(Map<String, MmiAction> map, MmiAction action) {
        map.put(action.getId(), action);
    }

    /** All known actions, in a stable display order. */
    @NonNull
    public static List<MmiAction> all() {
        return Collections.unmodifiableList(new java.util.ArrayList<>(ACTIONS.values()));
    }

    /** Looks up an action by id, or {@code null} if it is unknown. */
    @Nullable
    public static MmiAction byId(@Nullable String id) {
        if (id == null) {
            return null;
        }
        return ACTIONS.get(id);
    }
}
