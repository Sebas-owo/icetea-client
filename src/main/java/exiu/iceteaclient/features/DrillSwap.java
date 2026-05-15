package exiu.iceteaclient.features;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import exiu.iceteaclient.mixin.RightClickHelper;
import exiu.iceteaclient.util.Delay;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class DrillSwap {

    public static boolean enabled = false;

    private int fishingRodSlot = -1;
    private int otherDrillSlot = -1;
    private int selectedSlot;

    private boolean useMainDrillAbility = false;

    private boolean active = false;
    
    public void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> { return enabled ? rightClick(player, hand, false) : ActionResult.PASS; });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> { return enabled ? rightClick(player, hand, true) : ActionResult.PASS; });
        UseItemCallback.EVENT.register((player, world, hand) -> { return enabled ? rightClick(player, hand, false) : ActionResult.PASS; });
    }

    private ActionResult rightClick(PlayerEntity player, Hand hand, boolean entity) {
        ItemStack stack = player.getStackInHand(hand);

        if (isDrill(stack)) {
            if (entity) {
                return ActionResult.FAIL;
            }
            if (!active) {
                active = true;
                return prepareSwap(player);
            }
        }

        return ActionResult.PASS;
    }

    private ActionResult prepareSwap(PlayerEntity player) {

        boolean hasFishingRod = false;
        boolean hasOtherDrill = false;

        PlayerInventory inventory = player.getInventory();
        selectedSlot = inventory.getSelectedSlot();

        if (fishingRodSlot >= 0 && isFishingRod(inventory.getStack(fishingRodSlot))) {
            hasFishingRod = true;
        }

        if (!hasFishingRod) {
            for (int i = 0; i < 9; i++) {
                if (isFishingRod(inventory.getStack(i))) {
                    hasFishingRod = true;
                    fishingRodSlot = i;
                    break;
                }
            }
        }

        if (otherDrillSlot >= 0 && otherDrillSlot != selectedSlot && isDrill(inventory.getStack(otherDrillSlot))) {
            hasOtherDrill = true;
        }

        if (!hasOtherDrill) {
            for (int i = 0; i < 9; i++) {
                if (i != selectedSlot && isDrill(inventory.getStack(i))) {
                    hasOtherDrill = true;
                    otherDrillSlot = i;
                    break;
                }
            }
        }

        if (!hasFishingRod && !hasOtherDrill) {
            active = false;
            return ActionResult.PASS;
        }

        if (hasFishingRod) {
            inventory.setSelectedSlot(fishingRodSlot);
        } else {
            inventory.setSelectedSlot(otherDrillSlot);
        }

        useMainDrillAbility = hasOtherDrill ? false : true;

        macro(hasFishingRod, hasOtherDrill, true);

        return ActionResult.FAIL;
    }

    private void macro(boolean hasFishingRod, boolean hasOtherDrill, boolean swapped) {
        
        long delay = Delay.getDelay(200, 100, 300);

        CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS).execute(() -> MinecraftClient.getInstance().execute(() -> {
            MinecraftClient instance = MinecraftClient.getInstance();
            PlayerEntity player = instance.player;

            if (hasFishingRod) {
                ((RightClickHelper) instance).invokeDoItemUse();
                macro(false, hasOtherDrill, false);
            } else if (hasOtherDrill) {
                if (!swapped) {
                    player.getInventory().setSelectedSlot(otherDrillSlot);
                    macro(false, hasOtherDrill, true);
                } else {
                    ((RightClickHelper) instance).invokeDoItemUse();
                    macro(false, false, false);
                }
            } else {
                if (!swapped) {
                    player.getInventory().setSelectedSlot(selectedSlot);
                    if (useMainDrillAbility) {
                        macro(false, false, true);
                    } else {
                        active = false;
                    }
                } else {
                    ((RightClickHelper) instance).invokeDoItemUse();
                    active = false;
                }
            }
        }));
    }

    private boolean isDrill(ItemStack stack) {
        LoreComponent lore = stack.get(DataComponentTypes.LORE);

        if (lore == null || lore.lines().isEmpty()) {
            return false;
        }

        Text firstLine = lore.lines().get(0);

        if (firstLine.getSiblings().isEmpty()) {
            return false;
        }

        String text = firstLine.getSiblings().get(0).getString();

        if (!text.contains("Breaking Power")) {
            return false;
        }

        return true;
    }

    private boolean isFishingRod(ItemStack stack) {
        return stack.isOf(Items.FISHING_ROD);
    }
}
