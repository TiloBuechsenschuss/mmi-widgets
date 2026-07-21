package com.example.mmi_widgets.model;

import androidx.annotation.NonNull;

/**
 * Describes a single toggleable telephony function that is controlled through an MMI code
 * (for example unconditional call forwarding).
 *
 * <p>MMI (Man-Machine Interface) codes are the {@code *}, {@code #} sequences you normally type
 * into the dialer, e.g. {@code **21*<number>#} to enable unconditional call forwarding and
 * {@code ##21#} to disable it. This class stores the "enable" and "disable" templates for one
 * function so the rest of the app can send whichever one is appropriate.</p>
 *
 * <p>Templates may contain the placeholder {@link #NUMBER_PLACEHOLDER}. It is substituted with the
 * phone number the user saved in the app before the code is dialed.</p>
 */
public final class MmiAction {

    /** Placeholder inside a code template that is replaced with the user's saved number. */
    public static final String NUMBER_PLACEHOLDER = "{number}";

    private final String id;
    private final String label;
    private final String enableCode;
    private final String disableCode;
    private final boolean requiresNumber;
    private final boolean oneShot;

    /** Creates a toggle action with distinct enable/disable codes and persisted on/off state. */
    public MmiAction(@NonNull String id,
                     @NonNull String label,
                     @NonNull String enableCode,
                     @NonNull String disableCode,
                     boolean requiresNumber) {
        this(id, label, enableCode, disableCode, requiresNumber, false);
    }

    private MmiAction(@NonNull String id,
                      @NonNull String label,
                      @NonNull String enableCode,
                      @NonNull String disableCode,
                      boolean requiresNumber,
                      boolean oneShot) {
        this.id = id;
        this.label = label;
        this.enableCode = enableCode;
        this.disableCode = disableCode;
        this.requiresNumber = requiresNumber;
        this.oneShot = oneShot;
    }

    /**
     * Creates a one-shot action: it dials a single code and keeps <em>no</em> on/off state. Used
     * for interrogation / status-check codes such as {@code *#002#}, which ask the network to show
     * the current call-forwarding configuration.
     */
    @NonNull
    public static MmiAction oneShot(@NonNull String id,
                                    @NonNull String label,
                                    @NonNull String code) {
        return new MmiAction(id, label, code, code, false, true);
    }

    /** Stable identifier used as a preferences key and in intents. */
    @NonNull
    public String getId() {
        return id;
    }

    /** Human-readable name shown in the UI and on widgets. */
    @NonNull
    public String getLabel() {
        return label;
    }

    /** Raw enable template, may contain {@link #NUMBER_PLACEHOLDER}. */
    @NonNull
    public String getEnableCode() {
        return enableCode;
    }

    /** Raw disable template, may contain {@link #NUMBER_PLACEHOLDER}. */
    @NonNull
    public String getDisableCode() {
        return disableCode;
    }

    /** Whether {@link #getEnableCode()} needs a saved number to be usable. */
    public boolean requiresNumber() {
        return requiresNumber;
    }

    /**
     * Whether this is a one-shot action (dials a single code, no on/off state). One-shot actions
     * are shown with a "Check" button instead of a toggle and never touch persisted state.
     */
    public boolean isOneShot() {
        return oneShot;
    }

    /**
     * Returns the code to dial for the requested target state, with the number placeholder
     * substituted.
     *
     * @param enable {@code true} to enable the function, {@code false} to disable it.
     * @param number the user's saved number; may be empty when {@link #requiresNumber()} is false.
     * @return the concrete MMI code ready to be dialed.
     */
    @NonNull
    public String resolveCode(boolean enable, @NonNull String number) {
        String template = enable ? enableCode : disableCode;
        return template.replace(NUMBER_PLACEHOLDER, number);
    }
}
