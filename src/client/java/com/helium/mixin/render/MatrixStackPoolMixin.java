package com.helium.mixin.render;

import com.helium.HeliumClient;
import com.helium.config.HeliumConfig;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Mixin(MatrixStack.class)
public abstract class MatrixStackPoolMixin {

    @Unique
    private static Field helium$stackField = null;

    @Unique
    private static boolean helium$resolved = false;

    @Unique
    private static boolean helium$failed = false;

    @Unique
    private static boolean helium$isList = false;

    @Unique
    private static Method helium$copyIntoMethod = null;

    @Unique
    private static Constructor<MatrixStack.Entry> helium$noArgCtor = null;

    @Unique
    private final Deque<MatrixStack.Entry> helium$pool = new ArrayDeque<>();

    @Unique
    private static boolean helium$hasStackDepth = false;

    @Unique
    private static void helium$resolve() {
        if (helium$resolved) return;
        helium$resolved = true;

        try {
            MatrixStack.class.getDeclaredField("stackDepth");
            helium$hasStackDepth = true;
            return;
        } catch (NoSuchFieldException ignored) {}

        try {
            Field f2 = MatrixStack.class.getDeclaredField("field_55850");
            if (f2.getType() == int.class) {
                helium$hasStackDepth = true;
                return;
            }
        } catch (NoSuchFieldException ignored) {}

        String[] fieldNames = {"stack", "field_55849", "field_22924"};
        for (String name : fieldNames) {
            try {
                Field f = MatrixStack.class.getDeclaredField(name);
                f.setAccessible(true);
                helium$stackField = f;
                Class<?> type = f.getType();
                helium$isList = List.class.isAssignableFrom(type) && !Deque.class.isAssignableFrom(type);
                break;
            } catch (NoSuchFieldException ignored) {}
        }

        try {
            helium$copyIntoMethod = MatrixStack.Entry.class.getDeclaredMethod("copy", MatrixStack.Entry.class);
            helium$copyIntoMethod.setAccessible(true);
        } catch (NoSuchMethodException ignored) {}

        try {
            helium$noArgCtor = MatrixStack.Entry.class.getDeclaredConstructor();
            helium$noArgCtor.setAccessible(true);
        } catch (NoSuchMethodException ignored) {}
    }

    @Unique
    @SuppressWarnings("unchecked")
    private Object helium$getStack() {
        if (helium$stackField == null) return null;
        try {
            return helium$stackField.get(this);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Unique
    private MatrixStack.Entry helium$createCopy(MatrixStack.Entry source) {
        if (helium$noArgCtor != null && helium$copyIntoMethod != null) {
            try {
                MatrixStack.Entry entry = helium$noArgCtor.newInstance();
                helium$copyIntoMethod.invoke(entry, source);
                return entry;
            } catch (Throwable ignored) {}
        }

        try {
            Constructor<MatrixStack.Entry> ctor =
                    MatrixStack.Entry.class.getDeclaredConstructor(
                            org.joml.Matrix4f.class, org.joml.Matrix3f.class);
            ctor.setAccessible(true);
            return ctor.newInstance(
                    new org.joml.Matrix4f(source.getPositionMatrix()),
                    new org.joml.Matrix3f(source.getNormalMatrix()));
        } catch (Throwable ignored) {}

        return null;
    }

    @Unique
    private void helium$copyData(MatrixStack.Entry dest, MatrixStack.Entry source) {
        if (helium$copyIntoMethod != null) {
            try {
                helium$copyIntoMethod.invoke(dest, source);
                return;
            } catch (Throwable ignored) {}
        }
        dest.getPositionMatrix().set(source.getPositionMatrix());
        dest.getNormalMatrix().set(source.getNormalMatrix());
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "push", at = @At("HEAD"), cancellable = true, require = 0)
    private void helium$pooledPush(CallbackInfo ci) {
        if (helium$failed) return;

        try {
            HeliumConfig config = HeliumClient.getConfig();
            if (config == null || !config.poseStackPooling) return;

            helium$resolve();
            if (helium$hasStackDepth) return;

            Object stackObj = helium$getStack();
            if (stackObj == null) return;

            MatrixStack.Entry top;
            if (helium$isList) {
                List<MatrixStack.Entry> list = (List<MatrixStack.Entry>) stackObj;
                top = list.get(list.size() - 1);
            } else {
                Deque<MatrixStack.Entry> deque = (Deque<MatrixStack.Entry>) stackObj;
                top = deque.getLast();
            }

            MatrixStack.Entry reused = helium$pool.pollLast();
            if (reused == null) {
                reused = helium$createCopy(top);
                if (reused == null) return;
            } else {
                helium$copyData(reused, top);
            }

            if (helium$isList) {
                ((List<MatrixStack.Entry>) stackObj).add(reused);
            } else {
                ((Deque<MatrixStack.Entry>) stackObj).addLast(reused);
            }
            ci.cancel();
        } catch (Throwable t) {
            if (!helium$failed) {
                helium$failed = true;
                HeliumClient.LOGGER.warn("pose stack pooling disabled ({})", t.getClass().getSimpleName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "pop", at = @At("HEAD"), cancellable = true, require = 0)
    private void helium$pooledPop(CallbackInfo ci) {
        if (helium$failed) return;

        try {
            HeliumConfig config = HeliumClient.getConfig();
            if (config == null || !config.poseStackPooling) return;

            helium$resolve();
            if (helium$hasStackDepth) return;

            Object stackObj = helium$getStack();
            if (stackObj == null) return;

            int size;
            MatrixStack.Entry removed;

            if (helium$isList) {
                List<MatrixStack.Entry> list = (List<MatrixStack.Entry>) stackObj;
                size = list.size();
                if (size <= 1) return;
                removed = list.remove(size - 1);
            } else {
                Deque<MatrixStack.Entry> deque = (Deque<MatrixStack.Entry>) stackObj;
                size = deque.size();
                if (size <= 1) return;
                removed = deque.removeLast();
            }

            if (helium$pool.size() < 256) {
                helium$pool.addLast(removed);
            }
            ci.cancel();
        } catch (Throwable t) {
            if (!helium$failed) {
                helium$failed = true;
                HeliumClient.LOGGER.warn("pose stack pooling disabled ({})", t.getClass().getSimpleName());
            }
        }
    }
}
