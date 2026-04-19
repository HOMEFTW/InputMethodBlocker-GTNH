#include <jni.h>
#include <windows.h>
#include <imm.h>

#include <mutex>

#pragma comment(lib, "imm32.lib")
#pragma comment(lib, "user32.lib")

namespace {

struct ImeState {
    HWND window;
    HIMC detachedContext;
    bool detached;
};

ImeState g_state = {nullptr, nullptr, false};
std::mutex g_mutex;

HWND resolveTargetWindow() {
    HWND hwnd = GetForegroundWindow();
    if (hwnd == nullptr) return nullptr;

    DWORD processId = 0;
    GetWindowThreadProcessId(hwnd, &processId);
    return processId == GetCurrentProcessId() ? hwnd : nullptr;
}

void resetState() {
    g_state.window = nullptr;
    g_state.detachedContext = nullptr;
    g_state.detached = false;
}

void closeImeOnContext(HIMC context) {
    if (context == nullptr) return;
    ImmNotifyIME(context, NI_COMPOSITIONSTR, CPS_CANCEL, 0);
    ImmSetOpenStatus(context, FALSE);
}

void closeImeOnWindow(HWND hwnd) {
    if (hwnd == nullptr) return;

    HIMC context = ImmGetContext(hwnd);
    if (context == nullptr) return;

    closeImeOnContext(context);
    ImmReleaseContext(hwnd, context);
}

void restoreDetachedWindow() {
    if (!g_state.detached) {
        resetState();
        return;
    }

    if (g_state.window != nullptr && IsWindow(g_state.window) && g_state.detachedContext != nullptr) {
        ImmAssociateContext(g_state.window, g_state.detachedContext);
    }

    resetState();
}

void detachImeFromWindow(HWND hwnd) {
    if (hwnd == nullptr) return;

    if (g_state.detached && g_state.window == hwnd) {
        closeImeOnWindow(hwnd);
        return;
    }

    if (g_state.detached && g_state.window != hwnd) {
        restoreDetachedWindow();
    }

    closeImeOnWindow(hwnd);

    HIMC previousContext = ImmAssociateContext(hwnd, nullptr);
    if (previousContext != nullptr) {
        g_state.window = hwnd;
        g_state.detachedContext = previousContext;
        g_state.detached = true;
    } else {
        resetState();
    }
}

void attachImeToWindow(HWND hwnd) {
    if (hwnd == nullptr) return;

    if (g_state.detached && g_state.detachedContext != nullptr) {
        ImmAssociateContext(hwnd, g_state.detachedContext);
    }

    HIMC context = ImmGetContext(hwnd);
    if (context != nullptr) {
        ImmSetOpenStatus(context, TRUE);
        ImmReleaseContext(hwnd, context);
    }

    resetState();
}

} // namespace

extern "C" {

JNIEXPORT void JNICALL Java_com_github_skystardust_InputMethodBlocker_NativeUtils_inactiveInputMethod(
    JNIEnv*,
    jclass,
    jstring) {
    std::lock_guard<std::mutex> guard(g_mutex);
    HWND hwnd = resolveTargetWindow();
    if (hwnd == nullptr) return;
    detachImeFromWindow(hwnd);
}

JNIEXPORT void JNICALL Java_com_github_skystardust_InputMethodBlocker_NativeUtils_activeInputMethod(
    JNIEnv*,
    jclass,
    jstring) {
    std::lock_guard<std::mutex> guard(g_mutex);
    HWND hwnd = resolveTargetWindow();
    if (hwnd == nullptr) {
        if (g_state.detached && g_state.window != nullptr && IsWindow(g_state.window)) {
            hwnd = g_state.window;
        } else {
            return;
        }
    }
    attachImeToWindow(hwnd);
}

} // extern "C"
