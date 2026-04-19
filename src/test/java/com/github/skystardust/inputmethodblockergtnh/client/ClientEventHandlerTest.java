package com.github.skystardust.inputmethodblockergtnh.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.skystardust.inputmethodblockergtnh.focus.FocusDetector;
import com.github.skystardust.inputmethodblockergtnh.focus.InputFocusService;
import com.github.skystardust.inputmethodblockergtnh.ime.ImeBridge;

class ClientEventHandlerTest {

    @Test
    void togglesBridgeOnlyWhenFocusChanges() {
        RecordingBridge bridge = new RecordingBridge();
        InputFocusService focusService = new InputFocusService(Arrays.<FocusDetector>asList(screen -> Boolean.TRUE.equals(screen)));
        ClientEventHandler handler = new ClientEventHandler(bridge, focusService);

        handler.onScreenTick(Boolean.TRUE);
        handler.onScreenTick(Boolean.TRUE);
        handler.onScreenTick(Boolean.FALSE);

        assertEquals(2, bridge.calls);
    }

    private static final class RecordingBridge implements ImeBridge {

        private int calls;

        @Override
        public void initialize() {}

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public void setImeActive(boolean active) {
            calls++;
        }
    }
}
