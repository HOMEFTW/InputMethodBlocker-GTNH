package com.github.skystardust.inputmethodblockergtnh.client;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

import com.github.skystardust.inputmethodblockergtnh.InputMethodBlockerGTNH;
import com.github.skystardust.inputmethodblockergtnh.focus.InputFocusService;
import com.github.skystardust.inputmethodblockergtnh.ime.ImeBridge;

public class ClientEventHandler {

    private final ImeBridge bridge;
    private final InputFocusService focusService;
    private String lastScreenClassName = "<uninitialized>";
    private String lastDetectorName = "<uninitialized>";
    private Boolean lastDesiredState;

    public ClientEventHandler(ImeBridge bridge, InputFocusService focusService) {
        this.bridge = bridge;
        this.focusService = focusService;
    }

    public void initialize() {
        bridge.initialize();
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase != Phase.END) {
            return;
        }

        onScreenTick(Minecraft.getMinecraft().currentScreen);
    }

    public void onScreenTick(Object currentScreen) {
        if (currentScreen == null) {
            logScreenState(null, false, null);
            focusService.reset(bridge::setImeActive);
            return;
        }

        String matchingDetector = focusService.findMatchingDetector(currentScreen);
        logScreenState(currentScreen.getClass().getName(), matchingDetector != null, matchingDetector);
        focusService.update(currentScreen, bridge::setImeActive);
    }

    private void logScreenState(String screenClassName, boolean desiredActive, String matchingDetector) {
        String normalizedScreenClass = screenClassName == null ? "<null>" : screenClassName;
        String normalizedDetector = matchingDetector == null ? "<none>" : matchingDetector;

        if (!normalizedScreenClass.equals(lastScreenClassName) || !normalizedDetector.equals(lastDetectorName)
            || !java.util.Objects.equals(lastDesiredState, desiredActive)) {
            InputMethodBlockerGTNH.LOG.info(
                "screen={} detector={} desiredImeActive={} bridgeAvailable={}",
                normalizedScreenClass,
                normalizedDetector,
                desiredActive,
                bridge.isAvailable());
            lastScreenClassName = normalizedScreenClass;
            lastDetectorName = normalizedDetector;
            lastDesiredState = desiredActive;
        }
    }
}
