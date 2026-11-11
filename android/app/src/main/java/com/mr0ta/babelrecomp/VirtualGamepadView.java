package com.mr0ta.babelrecomp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import org.libsdl.app.SDLActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * High-performance virtual touch gamepad overlay for SNES Recomp on Android.
 * Renders semi-transparent D-Pad, 4 action buttons, L/R shoulder triggers,
 * and Select/Start buttons.
 * Dispatches key down/up directly to SDLActivity.onNativeKeyDown/Up.
 */
public class VirtualGamepadView extends View {

    // Keycodes matching keybinds.ini
    private static final int KEY_UP = KeyEvent.KEYCODE_DPAD_UP;
    private static final int KEY_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
    private static final int KEY_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
    private static final int KEY_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;

    private static final int KEY_A = KeyEvent.KEYCODE_X;          // SNES A -> X
    private static final int KEY_B = KeyEvent.KEYCODE_Z;          // SNES B -> Z
    private static final int KEY_X = KeyEvent.KEYCODE_S;          // SNES X -> S
    private static final int KEY_Y = KeyEvent.KEYCODE_A;          // SNES Y -> A
    private static final int KEY_L = KeyEvent.KEYCODE_C;          // SNES L -> C
    private static final int KEY_R = KeyEvent.KEYCODE_V;          // SNES R -> V

    private static final int KEY_START = KeyEvent.KEYCODE_ENTER;  // SNES Start -> Return
    private static final int KEY_SELECT = KeyEvent.KEYCODE_SHIFT_RIGHT; // SNES Select -> Right Shift

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dpadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Active state of each key
    private final Map<Integer, Boolean> keyState = new HashMap<>();

    // Button geometry
    private float dpadCenterX, dpadCenterY, dpadRadius;
    private float actionCenterX, actionCenterY, btnRadius, btnSpacing;
    private final RectF btnL = new RectF();
    private final RectF btnR = new RectF();
    private final RectF btnSelect = new RectF();
    private final RectF btnStart = new RectF();
    private final RectF toggleBtn = new RectF();

    private boolean controlsVisible = true;

    public VirtualGamepadView(Context context) {
        super(context);
        init();
    }

    private void init() {
        int[] keys = {
            KEY_UP, KEY_DOWN, KEY_LEFT, KEY_RIGHT,
            KEY_A, KEY_B, KEY_X, KEY_Y,
            KEY_L, KEY_R, KEY_START, KEY_SELECT
        };
        for (int k : keys) {
            keyState.put(k, false);
        }

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(3f);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;

        float density = getResources().getDisplayMetrics().density;
        boolean isPortrait = h > w;

        if (isPortrait) {
            // PORTRAIT MODE:
            // Sizing: D-pad on bottom left, well separated from right
            dpadRadius = Math.min(w * 0.17f, 65 * density);
            dpadCenterX = dpadRadius + 18 * density;
            dpadCenterY = h - dpadRadius - 38 * density;

            // Action buttons on bottom right (A, B, X, Y)
            btnRadius = Math.min(w * 0.065f, 26 * density);
            btnSpacing = btnRadius * 1.55f;
            actionCenterX = w - btnSpacing - btnRadius - 18 * density;
            actionCenterY = dpadCenterY;

            // Shoulders (L and R) in the black area directly above D-pad and Action cluster
            float shW = 85 * density;
            float shH = 38 * density;
            float shoulderY = dpadCenterY - dpadRadius - shH - 14 * density;

            btnL.set(dpadCenterX - shW * 0.5f, shoulderY, dpadCenterX + shW * 0.5f, shoulderY + shH);
            btnR.set(actionCenterX - shW * 0.5f, shoulderY, actionCenterX + shW * 0.5f, shoulderY + shH);

            // Select and Start at bottom center
            float pillW = 56 * density;
            float pillH = 26 * density;
            float bottomY = h - pillH - 12 * density;
            btnSelect.set(w * 0.5f - pillW - 8 * density, bottomY, w * 0.5f - 8 * density, bottomY + pillH);
            btnStart.set(w * 0.5f + 8 * density, bottomY, w * 0.5f + pillW + 8 * density, bottomY + pillH);

            // Small toggle button at top center to hide/show overlay
            float togSize = 36 * density;
            toggleBtn.set(w * 0.5f - togSize * 0.5f, 12 * density, w * 0.5f + togSize * 0.5f, 12 * density + togSize);
        } else {
            // LANDSCAPE MODE:
            // Keep existing landscape layout unchanged!
            dpadRadius = Math.min(w * 0.16f, h * 0.28f);
            dpadCenterX = dpadRadius + 24 * density;
            dpadCenterY = h - dpadRadius - 24 * density;

            btnRadius = Math.min(w * 0.055f, h * 0.095f);
            btnSpacing = btnRadius * 1.55f;
            actionCenterX = w - btnSpacing - btnRadius - 24 * density;
            actionCenterY = h - btnSpacing - btnRadius - 16 * density;

            float shW = 85 * density;
            float shH = 42 * density;
            btnL.set(24 * density, 16 * density, 24 * density + shW, 16 * density + shH);
            btnR.set(w - 24 * density - shW, 16 * density, w - 24 * density, 16 * density + shH);

            float pillW = 60 * density;
            float pillH = 28 * density;
            float bottomY = h - 36 * density;
            btnSelect.set(w * 0.5f - pillW - 12 * density, bottomY, w * 0.5f - 12 * density, bottomY + pillH);
            btnStart.set(w * 0.5f + 12 * density, bottomY, w * 0.5f + pillW + 12 * density, bottomY + pillH);

            float togSize = 36 * density;
            toggleBtn.set(w * 0.5f - togSize * 0.5f, 12 * density, w * 0.5f + togSize * 0.5f, 12 * density + togSize);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw toggle button (icon 🎮 / circle)
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(controlsVisible ? 0x44FFFFFF : 0x88FFFFFF);
        canvas.drawCircle(toggleBtn.centerX(), toggleBtn.centerY(), toggleBtn.width() * 0.45f, bgPaint);
        strokePaint.setColor(0x88000000);
        canvas.drawCircle(toggleBtn.centerX(), toggleBtn.centerY(), toggleBtn.width() * 0.45f, strokePaint);
        textPaint.setColor(0xFF000000);
        textPaint.setTextSize(toggleBtn.height() * 0.5f);
        canvas.drawText(controlsVisible ? "PAD" : "ON", toggleBtn.centerX(), toggleBtn.centerY() + textPaint.getTextSize() * 0.35f, textPaint);

        if (!controlsVisible) return;

        // 1. Draw D-Pad
        drawDpad(canvas);

        // 2. Draw Action Buttons (A, B, X, Y)
        float xA = actionCenterX + btnSpacing;
        float yA = actionCenterY;
        drawActionButton(canvas, "A", xA, yA, btnRadius, isPressed(KEY_A), 0x77E53935, 0xCCE53935);

        float xB = actionCenterX;
        float yB = actionCenterY + btnSpacing;
        drawActionButton(canvas, "B", xB, yB, btnRadius, isPressed(KEY_B), 0x77FBC02D, 0xCCFBC02D);

        float xX = actionCenterX;
        float yX = actionCenterY - btnSpacing;
        drawActionButton(canvas, "X", xX, yX, btnRadius, isPressed(KEY_X), 0x771E88E5, 0xCC1E88E5);

        float xY = actionCenterX - btnSpacing;
        float yY = actionCenterY;
        drawActionButton(canvas, "Y", xY, yY, btnRadius, isPressed(KEY_Y), 0x7743A047, 0xCC43A047);

        // 3. Draw Shoulder Buttons (L, R)
        drawPillButton(canvas, "L", btnL, isPressed(KEY_L));
        drawPillButton(canvas, "R", btnR, isPressed(KEY_R));

        // 4. Draw Select and Start
        drawPillButton(canvas, "SELECT", btnSelect, isPressed(KEY_SELECT));
        drawPillButton(canvas, "START", btnStart, isPressed(KEY_START));
    }

    private void drawDpad(Canvas canvas) {
        // Base outer ring
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(0x33000000);
        canvas.drawCircle(dpadCenterX, dpadCenterY, dpadRadius, bgPaint);
        strokePaint.setColor(0x55FFFFFF);
        canvas.drawCircle(dpadCenterX, dpadCenterY, dpadRadius, strokePaint);

        float armW = dpadRadius * 0.36f;
        float armL = dpadRadius * 0.85f;

        // Up arm
        dpadPaint.setColor(isPressed(KEY_UP) ? 0xAAFFFFFF : 0x44FFFFFF);
        canvas.drawRoundRect(dpadCenterX - armW * 0.5f, dpadCenterY - armL, dpadCenterX + armW * 0.5f, dpadCenterY, 8f, 8f, dpadPaint);

        // Down arm
        dpadPaint.setColor(isPressed(KEY_DOWN) ? 0xAAFFFFFF : 0x44FFFFFF);
        canvas.drawRoundRect(dpadCenterX - armW * 0.5f, dpadCenterY, dpadCenterX + armW * 0.5f, dpadCenterY + armL, 8f, 8f, dpadPaint);

        // Left arm
        dpadPaint.setColor(isPressed(KEY_LEFT) ? 0xAAFFFFFF : 0x44FFFFFF);
        canvas.drawRoundRect(dpadCenterX - armL, dpadCenterY - armW * 0.5f, dpadCenterX, dpadCenterY + armW * 0.5f, 8f, 8f, dpadPaint);

        // Right arm
        dpadPaint.setColor(isPressed(KEY_RIGHT) ? 0xAAFFFFFF : 0x44FFFFFF);
        canvas.drawRoundRect(dpadCenterX, dpadCenterY - armW * 0.5f, dpadCenterX + armL, dpadCenterY + armW * 0.5f, 8f, 8f, dpadPaint);

        // Center hub
        bgPaint.setColor(0x55000000);
        canvas.drawCircle(dpadCenterX, dpadCenterY, armW * 0.55f, bgPaint);
    }

    private void drawActionButton(Canvas canvas, String label, float cx, float cy, float r, boolean pressed, int colorNormal, int colorPressed) {
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(pressed ? colorPressed : colorNormal);
        canvas.drawCircle(cx, cy, r, bgPaint);

        strokePaint.setColor(pressed ? 0xFFFFFFFF : 0x88FFFFFF);
        canvas.drawCircle(cx, cy, r, strokePaint);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(r * 0.95f);
        canvas.drawText(label, cx, cy + textPaint.getTextSize() * 0.35f, textPaint);
    }

    private void drawPillButton(Canvas canvas, String label, RectF rect, boolean pressed) {
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(pressed ? 0x99FFFFFF : 0x44000000);
        canvas.drawRoundRect(rect, rect.height() * 0.5f, rect.height() * 0.5f, bgPaint);

        strokePaint.setColor(pressed ? 0xFFFFFFFF : 0x66FFFFFF);
        canvas.drawRoundRect(rect, rect.height() * 0.5f, rect.height() * 0.5f, strokePaint);

        textPaint.setColor(pressed ? Color.BLACK : Color.WHITE);
        textPaint.setTextSize(rect.height() * 0.45f);
        canvas.drawText(label, rect.centerX(), rect.centerY() + textPaint.getTextSize() * 0.35f, textPaint);
    }

    private boolean isPressed(int key) {
        Boolean b = keyState.get(key);
        return b != null && b;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerCount = event.getPointerCount();

        // Check if toggle button tapped
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            int actionIndex = event.getActionIndex();
            float tx = event.getX(actionIndex);
            float ty = event.getY(actionIndex);
            if (toggleBtn.contains(tx, ty)) {
                controlsVisible = !controlsVisible;
                if (!controlsVisible) {
                    // Release all buttons
                    releaseAll();
                }
                invalidate();
                return true;
            }
        }

        if (!controlsVisible) return false;

        // Temporary target state for this touch frame
        Map<Integer, Boolean> nextState = new HashMap<>();
        for (Integer k : keyState.keySet()) nextState.put(k, false);

        boolean touchHandled = false;

        if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL) {
            for (int i = 0; i < pointerCount; i++) {
                if (action == MotionEvent.ACTION_POINTER_UP && i == event.getActionIndex()) {
                    continue; // Skip the pointer that was just lifted
                }

                float x = event.getX(i);
                float y = event.getY(i);

                // 1. D-Pad detection
                float dx = x - dpadCenterX;
                float dy = y - dpadCenterY;
                float distSq = dx * dx + dy * dy;
                float dpadTouchRadius = dpadRadius * 1.3f;
                if (distSq <= dpadTouchRadius * dpadTouchRadius) {
                    touchHandled = true;
                    float deadzoneSq = (dpadRadius * 0.2f) * (dpadRadius * 0.2f);
                    if (distSq >= deadzoneSq) {
                        double angle = Math.toDegrees(Math.atan2(dy, dx)); // -180 to 180
                        // Right: -45 to 45
                        // Down: 45 to 135
                        // Left: 135 to 180 or -180 to -135
                        // Up: -135 to -45
                        if (angle >= -67.5 && angle <= 67.5) nextState.put(KEY_RIGHT, true);
                        if (angle >= 22.5 && angle <= 157.5) nextState.put(KEY_DOWN, true);
                        if (angle >= 112.5 || angle <= -112.5) nextState.put(KEY_LEFT, true);
                        if (angle >= -157.5 && angle <= -22.5) nextState.put(KEY_UP, true);
                    }
                }

                // 2. Action buttons detection
                float xA = actionCenterX + btnSpacing;
                float yA = actionCenterY;
                if (distSq(x, y, xA, yA) <= btnRadius * btnRadius * 1.5f) {
                    nextState.put(KEY_A, true);
                    touchHandled = true;
                }

                float xB = actionCenterX;
                float yB = actionCenterY + btnSpacing;
                if (distSq(x, y, xB, yB) <= btnRadius * btnRadius * 1.5f) {
                    nextState.put(KEY_B, true);
                    touchHandled = true;
                }

                float xX = actionCenterX;
                float yX = actionCenterY - btnSpacing;
                if (distSq(x, y, xX, yX) <= btnRadius * btnRadius * 1.5f) {
                    nextState.put(KEY_X, true);
                    touchHandled = true;
                }

                float xY = actionCenterX - btnSpacing;
                float yY = actionCenterY;
                if (distSq(x, y, xY, yY) <= btnRadius * btnRadius * 1.5f) {
                    nextState.put(KEY_Y, true);
                    touchHandled = true;
                }

                // 3. Shoulder buttons (with 6dp touch margin)
                float density = getResources().getDisplayMetrics().density;
                float pad = 6 * density;
                if (rectHit(btnL, x, y, pad)) {
                    nextState.put(KEY_L, true);
                    touchHandled = true;
                }
                if (rectHit(btnR, x, y, pad)) {
                    nextState.put(KEY_R, true);
                    touchHandled = true;
                }

                // 4. Select and Start
                if (rectHit(btnSelect, x, y, pad)) {
                    nextState.put(KEY_SELECT, true);
                    touchHandled = true;
                }
                if (rectHit(btnStart, x, y, pad)) {
                    nextState.put(KEY_START, true);
                    touchHandled = true;
                }
            }
        }

        // Apply state differences to native SDL
        for (Map.Entry<Integer, Boolean> entry : nextState.entrySet()) {
            int key = entry.getKey();
            boolean isDown = entry.getValue();
            boolean wasDown = isPressed(key);

            if (isDown != wasDown) {
                keyState.put(key, isDown);
                if (isDown) {
                    SDLActivity.onNativeKeyDown(key);
                } else {
                    SDLActivity.onNativeKeyUp(key);
                }
            }
        }

        invalidate();
        return touchHandled || super.onTouchEvent(event);
    }

    private void releaseAll() {
        for (Map.Entry<Integer, Boolean> entry : keyState.entrySet()) {
            if (entry.getValue()) {
                SDLActivity.onNativeKeyUp(entry.getKey());
            }
        }
        for (Integer k : keyState.keySet()) keyState.put(k, false);
    }

    private static boolean rectHit(RectF r, float x, float y, float pad) {
        return x >= r.left - pad && x <= r.right + pad && y >= r.top - pad && y <= r.bottom + pad;
    }

    private static float distSq(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return dx * dx + dy * dy;
    }
}
