package com.m1cheatmine.util;

public class MathUtils {
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * clamp(t, 0f, 1f);
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * clamp(t, 0.0, 1.0);
    }

    public static float smoothLerp(float current, float target, float speed, float deltaSeconds) {
        float t = 1f - (float) Math.exp(-speed * clamp(deltaSeconds, 0f, 0.1f));
        return lerp(current, target, t);
    }

    public static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    public static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    public static boolean isHovered(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public static float easeOutCubic(float t) {
        t = clamp(t, 0f, 1f);
        return 1f - (float) Math.pow(1f - t, 3);
    }

    public static float easeInOutCubic(float t) {
        t = clamp(t, 0f, 1f);
        return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
    }
}