package com.helium.crafting;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class CraftingInputHelper {

    private static Field boundkeyfield = null;
    private static boolean boundkeyfieldresolved = false;
    private static Method keypressedmethod = null;
    private static boolean keypressedresolved = false;

    private CraftingInputHelper() {}

    private static boolean iskeypressed(int keycode) {
        Window window = MinecraftClient.getInstance().getWindow();
        try {
            return InputUtil.isKeyPressed(window, keycode);
        } catch (NoSuchMethodError e) {
            try {
                if (!keypressedresolved) {
                    keypressedresolved = true;
                    keypressedmethod = InputUtil.class.getMethod("isKeyPressed", long.class, int.class);
                }
                if (keypressedmethod != null) {
                    return (boolean) keypressedmethod.invoke(null, window.getHandle(), keycode);
                }
            } catch (Throwable ignored) {}
            return GLFW.glfwGetKey(window.getHandle(), keycode) == GLFW.GLFW_PRESS;
        }
    }

    public static boolean isshiftdown() {
        return iskeypressed(GLFW.GLFW_KEY_LEFT_SHIFT) || iskeypressed(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public static boolean iscontroldown() {
        return iskeypressed(GLFW.GLFW_KEY_LEFT_CONTROL) || iskeypressed(GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    public static boolean isaltdown() {
        return iskeypressed(GLFW.GLFW_KEY_LEFT_ALT) || iskeypressed(GLFW.GLFW_KEY_RIGHT_ALT);
    }

    public static boolean iskeybindingpressed(KeyBinding keyBinding) {
        int code = getboundkeycode(keyBinding);
        if (code == InputUtil.UNKNOWN_KEY.getCode()) return false;
        return iskeypressed(code);
    }

    public static boolean isdropkeypressed() {
        return iskeybindingpressed(MinecraftClient.getInstance().options.dropKey);
    }

    public static boolean istogglekey(int keycode) {
        return keycode == GLFW.GLFW_KEY_CAPS_LOCK ||
                keycode == GLFW.GLFW_KEY_NUM_LOCK ||
                keycode == GLFW.GLFW_KEY_SCROLL_LOCK;
    }

    private static int getboundkeycode(KeyBinding keyBinding) {
        try {
            if (!boundkeyfieldresolved) {
                boundkeyfieldresolved = true;
                try {
                    boundkeyfield = KeyBinding.class.getDeclaredField("boundKey");
                    boundkeyfield.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    for (Field f : KeyBinding.class.getDeclaredFields()) {
                        if (InputUtil.Key.class.isAssignableFrom(f.getType())) {
                            f.setAccessible(true);
                            boundkeyfield = f;
                            break;
                        }
                    }
                }
            }
            if (boundkeyfield != null) {
                InputUtil.Key key = (InputUtil.Key) boundkeyfield.get(keyBinding);
                return key.getCode();
            }
        } catch (Throwable ignored) {}
        return InputUtil.UNKNOWN_KEY.getCode();
    }
}
