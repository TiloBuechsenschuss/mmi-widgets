package com.example.mmi_widgets;

import static org.junit.Assert.assertEquals;

import com.example.mmi_widgets.model.MmiAction;

import org.junit.Test;

/**
 * Pure-JVM tests for {@link MmiAction} code resolution. These run without a device/emulator
 * ({@code ./gradlew test}) and are the fastest thing to iterate on.
 */
public class MmiActionTest {

    private final MmiAction forwarding = new MmiAction(
            "call_forwarding_unconditional",
            "Call forwarding (all)",
            "**21*" + MmiAction.NUMBER_PLACEHOLDER + "#",
            "##21#",
            true);

    @Test
    public void enable_substitutesNumber() {
        assertEquals("**21*123456#", forwarding.resolveCode(true, "123456"));
    }

    @Test
    public void disable_ignoresNumber() {
        assertEquals("##21#", forwarding.resolveCode(false, "123456"));
    }
}
