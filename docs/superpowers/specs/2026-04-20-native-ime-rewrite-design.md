# InputMethodBlocker-GTNH Native IME Rewrite Design

Date: 2026-04-20
Project: InputMethodBlocker-GTNH
Scope: Replace the legacy Windows JNI DLL with a new Windows x64-only native implementation that works under GTNH on Java 17-25.

## Background

The current Java-side behavior is already doing the right thing:

- White-list based text input detection is active.
- Non-white-list screens continuously request `desiredImeActive=false`.
- Diagnostics show JNI calls to `inactiveInputMethod` are being made repeatedly.

This isolates the remaining failure to the legacy native DLL behavior. The old DLL is not reliably preventing IME activation in the user's GTNH environment even though Java is repeatedly requesting the inactive state.

## Goal

When no white-list text field is focused, IME must be effectively blocked at the window level so that pressing common IME toggle keys such as `Shift` or `Ctrl+Space` does not leave IME active for the Minecraft window.

When a white-list text field is focused, IME must become available again so the user can input Chinese text normally.

## Non-Goals

- No support for generic screen scanning or reflection-based focus detection.
- No support for non-Windows platforms.
- No support for Windows x86 as a first-class target.
- No keyboard hook or global hotkey interception.
- No redesign of the current Java focus-detection architecture.

## Constraints

- Preserve the existing Java JNI API:
  - `NativeUtils.inactiveInputMethod(String windowName)`
  - `NativeUtils.activeInputMethod(String windowName)`
- Keep the current Java bridge architecture intact so only the packaged DLL is replaced.
- The implementation must fail safely and must never crash the client when native APIs are unavailable or return unexpected results.
- The implementation must only operate on the current Java process window and must never interfere with unrelated foreground applications.

## Approaches Considered

### 1. Soft IME close only

Call `ImmSetOpenStatus(FALSE)` and cancel active composition every time Java requests IME inactive.

Pros:

- Smallest native implementation.
- Minimal change from old behavior.

Cons:

- Still depends on repeated close requests winning against the OS/IME stack.
- Most likely to reproduce the current "Java keeps calling inactive but IME still toggles back on" failure mode.

Decision:

- Rejected as insufficiently robust.

### 2. Window-level context detach and restore

Detach the IME context from the Minecraft window while IME should be blocked, and restore the original context when IME should be allowed.

Pros:

- Blocks IME at the window association level instead of only toggling open status.
- Preserves the Java API and current focus-detection architecture.
- Keeps game key input behavior intact without trying to suppress physical key presses.

Cons:

- Requires careful native state management across window focus and window handle changes.

Decision:

- Selected.

### 3. Keyboard hook or system hotkey interception

Install a low-level keyboard hook and suppress keys that may toggle IME.

Pros:

- Can be aggressive.

Cons:

- High risk of breaking normal in-game controls.
- IME toggle behavior varies by user configuration and IME vendor.
- Significantly more invasive and harder to maintain.

Decision:

- Rejected unless the selected approach proves fundamentally insufficient.

## Selected Design

### High-level architecture

The Java layer remains unchanged in shape:

- `WindowsImeBridge` continues to initialize a native library and call two JNI functions through `LegacyNativeImeBindings`.
- The new DLL exports the same JNI symbols currently declared in the generated JNI header.
- The packaged jar still contains `InputMethodBlocker-Natives-x64.dll`, but the binary is replaced by a new implementation.

This keeps the change isolated to the native implementation and avoids reworking the Java-side detector and bridge code that is already behaving correctly.

### Windows target

The rewritten native layer officially supports:

- Windows x64
- GTNH on Java 17-25

Windows x86 is no longer a target for the new implementation. Existing Java code and resources should be adjusted so x64 is the supported packaged runtime path.

## Native state model

The DLL maintains one small process-local state structure:

- `HWND minecraftWindow`
- `HIMC detachedContext`
- `bool imeDetached`
- `DWORD ownerProcessId`

The model intentionally supports only one managed Minecraft window at a time. This keeps state simple and aligned with the user's runtime.

Two stable native states are allowed:

1. Attached
   - No context is currently detached.
   - `imeDetached == false`
   - `detachedContext == NULL`

2. Detached with saved context
   - The window's original IME context has been removed and saved for later restore.
   - `imeDetached == true`
   - `detachedContext != NULL`

No pool of contexts and no multi-window tracking will be implemented in the first version.

## Window selection

The native implementation identifies the target window by:

1. Calling `GetForegroundWindow()`
2. Resolving the owning process ID with `GetWindowThreadProcessId`
3. Comparing that process ID to the current process ID

If the foreground window does not belong to the current Java process, the JNI call becomes a no-op.

The `windowName` JNI string parameter remains for ABI compatibility, but it is not used as the primary matching mechanism. Window-title matching is intentionally avoided because it is brittle across launchers, wrappers, and GTNH runtime variants.

## inactiveInputMethod flow

When Java requests IME inactive:

1. Resolve the current foreground Minecraft window.
2. If no valid current-process window is found, return safely.
3. If a different managed window was previously detached, attempt to restore the old window first, then switch state to the new window.
4. If the current target window is not already detached:
   - Read its current `HIMC`
   - Save it into `detachedContext`
   - Call `ImmAssociateContext(hwnd, NULL)` to detach IME from the window
   - Mark `imeDetached = true`
5. Whether first-time or repeated inactive call:
   - Attempt `ImmGetContext`
   - If a context is available, call `ImmSetOpenStatus(FALSE)`
   - Cancel composition with `ImmNotifyIME(...CANCEL...)` where applicable
   - Release any acquired context

The key behavior is that IME is not merely "closed"; it is detached from the Minecraft window so toggle keys cannot persistently re-enable IME for that window while blocking is active.

## activeInputMethod flow

When Java requests IME active:

1. Resolve the current foreground Minecraft window.
2. If a saved detached context exists and a valid current-process window is available:
   - Reattach the saved `HIMC` using `ImmAssociateContext`
3. If a context is then available:
   - Call `ImmSetOpenStatus(TRUE)` to allow IME usage again
4. Clear saved state back to Attached

If restore cannot be completed, the native layer fails safely and clears state conservatively rather than repeatedly throwing or leaving Java in an unstable loop.

## Window-handle change handling

The design must survive:

- Alt-Tab out and back
- Display mode changes
- Minecraft/LWJGL window recreation

If `inactiveInputMethod` observes a different valid Minecraft `HWND` while a previous window is detached:

1. Attempt to restore the old detached window using the saved `HIMC`
2. Reset native state
3. Apply detach logic to the new window

This prevents leaking a detached IME context onto a stale or destroyed window handle.

## Shutdown and unload behavior

If the DLL is unloaded or the JVM is exiting while a context is still detached, the implementation should attempt a best-effort restore of the saved context to the last known window.

This is a safety cleanup step only. Failure here must remain non-fatal.

## Error handling

The native layer must prioritize safety over strictness:

- JNI exports must not throw native exceptions across the boundary.
- Failed Win32 or IMM calls become safe no-ops.
- Null handles or null contexts are tolerated.
- Any optional debug logging must never be required for correctness.

The result should be:

- No client hard-freeze
- No crash during main menu or in-game
- No interference with unrelated applications

## Java-side changes required

The Java side should only receive targeted updates required by the new native contract:

- Update platform/resource expectations to reflect Windows x64 as the supported runtime target.
- Keep JNI class and method names unchanged.
- Keep `WindowsImeBridge` initialization and repeated inactive calls intact, since they remain useful with the new native implementation.
- Replace packaged DLL resources with the new x64 binary.

No change is planned for detector registration, white-list policy, or compat layering as part of this native rewrite.

## Verification plan

The implementation is considered successful only if all of the following work:

1. On the main menu and in-game without a white-list text box focused, pressing `Shift` does not leave IME active for the Minecraft window.
2. In vanilla white-list text fields, IME is restored and Chinese input works normally.
3. In existing compat white-list fields for MUI1, MUI2, AE2, NEI, Angelica, and ServerUtilities, IME is restored and input works normally.
4. Leaving a white-list text field immediately returns the game to blocked IME state.
5. Alt-Tab and window focus changes do not hard-freeze the client or leave the native layer in a broken loop.

## Testing plan

### Java tests

Add or update Java tests for:

- Platform support assumptions for Windows x64
- Resource selection behavior
- Bridge initialization behavior that still expects successful native setup and repeated inactive calls

These tests do not validate Win32 behavior directly, but they protect the Java-side contract and packaging assumptions.

### Manual runtime validation

Manual validation is required on Windows x64:

1. Launch the modded client to main menu.
2. Confirm repeated IME toggle attempts do not leave IME active while no input field is focused.
3. Enter a vanilla white-list field and confirm IME input works.
4. Enter MUI1 and MUI2 white-list fields and confirm IME input works.
5. Enter AE2, Angelica, NEI, and ServerUtilities white-list fields and confirm IME input works.
6. Exit each field and confirm IME is blocked again.
7. Alt-Tab during both blocked and allowed states and confirm the client remains responsive.

## Delivery

The implementation deliverables are:

- New native source files inside the `InputMethodBlocker-GTNH` project
- A build path that produces a replacement `InputMethodBlocker-Natives-x64.dll`
- Updated jar packaging that embeds the new DLL
- A rebuilt mod jar ready for user testing

## Scope guardrails

This rewrite intentionally does not include:

- Expanding the white-list detector set
- Adding generic UI scanning
- Hooking keyboard events globally
- Reworking compat detectors
- Cross-platform native support

If the selected detach-and-restore strategy proves insufficient, that should be treated as a new design round rather than silently broadening scope during implementation.
