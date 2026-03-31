package com.helium.crafting;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

import java.lang.reflect.Method;
import java.util.Optional;

public final class CraftingInventoryUtils {

    private static Method onmouseclickmethod = null;
    private static boolean onmouseclickresolved = false;

    private CraftingInventoryUtils() {}

    public static void clickslot(HandledScreen<? extends ScreenHandler> gui, int slotNum, int mouseButton, SlotActionType type) {
        if (slotNum >= 0 && slotNum < gui.getScreenHandler().slots.size()) {
            Slot slot = gui.getScreenHandler().getSlot(slotNum);
            clickslot(gui, slot, slotNum, mouseButton, type);
        } else {
            MinecraftClient mc = MinecraftClient.getInstance();
            ClientPlayerInteractionManager interactionManager = mc.interactionManager;
            if (interactionManager != null) {
                interactionManager.clickSlot(gui.getScreenHandler().syncId, slotNum, mouseButton, type, mc.player);
            }
        }
    }

    public static void clickslot(HandledScreen<? extends ScreenHandler> gui, Slot slot, int slotNum, int mouseButton, SlotActionType type) {
        try {
            if (!onmouseclickresolved) {
                onmouseclickresolved = true;
                for (Method m : HandledScreen.class.getDeclaredMethods()) {
                    Class<?>[] params = m.getParameterTypes();
                    if (params.length == 4 && params[0] == Slot.class && params[1] == int.class
                            && params[2] == int.class && params[3] == SlotActionType.class) {
                        m.setAccessible(true);
                        onmouseclickmethod = m;
                        break;
                    }
                }
            }
            if (onmouseclickmethod != null) {
                onmouseclickmethod.invoke(gui, slot, slotNum, mouseButton, type);
            }
        } catch (Throwable t) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.interactionManager != null) {
                mc.interactionManager.clickSlot(gui.getScreenHandler().syncId, slotNum, mouseButton, type, mc.player);
            }
        }
    }

    public static void leftclickslot(HandledScreen<? extends ScreenHandler> gui, Slot slot) {
        clickslot(gui, slot, slot.getIndex(), 0, SlotActionType.PICKUP);
    }

    public static void leftclickslot(HandledScreen<? extends ScreenHandler> gui, int slotNum) {
        clickslot(gui, slotNum, 0, SlotActionType.PICKUP);
    }

    public static void movematchingintoslot(HandledScreen<? extends ScreenHandler> gui, int slotNum) {
        clickslot(gui, slotNum, 0, SlotActionType.PICKUP);
        clickslot(gui, slotNum, 0, SlotActionType.PICKUP_ALL);
        clickslot(gui, slotNum, 0, SlotActionType.PICKUP);
    }

    public static void rightclickslot(HandledScreen<? extends ScreenHandler> gui, Slot slot) {
        clickslot(gui, slot, slot.getIndex(), 1, SlotActionType.PICKUP);
    }

    public static void rightclickslot(HandledScreen<? extends ScreenHandler> gui, int slotNum) {
        clickslot(gui, slotNum, 1, SlotActionType.PICKUP);
    }

    public static void shiftclickslot(HandledScreen<? extends ScreenHandler> gui, Slot slot) {
        clickslot(gui, slot, slot.getIndex(), 0, SlotActionType.QUICK_MOVE);
    }

    public static void shiftclickslot(HandledScreen<? extends ScreenHandler> gui, int slotNum) {
        clickslot(gui, slotNum, 0, SlotActionType.QUICK_MOVE);
    }

    public static void dropitemsfromcursor(HandledScreen<? extends ScreenHandler> gui) {
        clickslot(gui, -999, 0, SlotActionType.PICKUP);
    }

    public static void dropitem(HandledScreen<? extends ScreenHandler> gui, int slotNum) {
        clickslot(gui, slotNum, 0, SlotActionType.THROW);
    }

    public static void dropstack(HandledScreen<? extends ScreenHandler> gui, int slotNum) {
        clickslot(gui, slotNum, 1, SlotActionType.THROW);
    }

    public static Slot getslot(HandledScreen<? extends ScreenHandler> gui, int slotNum) {
        return gui.getScreenHandler().getSlot(slotNum);
    }

    @SuppressWarnings("unchecked")
    public static Optional<Slot> findmatchingslot(HandledScreen<? extends ScreenHandler> gui, Object ingredient) {
        DefaultedList<Slot> slots = gui.getScreenHandler().slots;
        for (Slot slot : slots) {
            if (!(slot.inventory instanceof PlayerInventory)) continue;
            try {
                Class<?> ingredientClass = Class.forName("net.minecraft.recipe.Ingredient");
                Method matchesMethod = ingredientClass.getMethod("matches", Optional.class, net.minecraft.item.ItemStack.class);
                Boolean matches = (Boolean) matchesMethod.invoke(null, Optional.of(ingredient), slot.getStack());
                if (matches) return Optional.of(slot);
            } catch (Throwable t) {
                continue;
            }
        }
        return Optional.empty();
    }

    public static Optional<Slot> findemptyslot(HandledScreen<? extends ScreenHandler> gui) {
        DefaultedList<Slot> slots = gui.getScreenHandler().slots;
        for (Slot slot : slots) {
            if (!(slot.inventory instanceof PlayerInventory)) continue;
            if (!slot.getStack().isOf(Items.AIR)) continue;
            return Optional.of(slot);
        }
        return Optional.empty();
    }
}
