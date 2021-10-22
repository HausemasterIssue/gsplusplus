package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import com.gamesense.client.module.modules.movement.Blink;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;

/**
 * @author Doogie13
 * @since 15/07/2021
 */

@Module.Declaration(name = "FootConcrete", category = Category.Combat)
public class FootConcrete extends Module {

    ModeSetting jumpMode = registerMode("jumpMode", Arrays.asList("real", "fake"), "real");

    BooleanSetting general = registerBoolean("General Settings", false);

    ModeSetting mode = registerMode("rubberbandMode", Arrays.asList("jump", "clip"), "jump", () -> jumpMode.getValue().equals("real") && general.getValue());
    BooleanSetting useBlink = registerBoolean("useBlink", true, () -> jumpMode.getValue().equals("real") && general.getValue());
    BooleanSetting conserve = registerBoolean("Conserve", false);
    IntegerSetting range = registerInteger("clipRange", 50, 1, 32, () -> general.getValue());
    BooleanSetting rotate = registerBoolean("rotate", true, () -> general.getValue());
    BooleanSetting debugpos = registerBoolean("Debug Position", false);


    BooleanSetting blocks = registerBoolean("Blocks Menu", false);

    BooleanSetting obby = registerBoolean("Obsidian", true, () -> blocks.getValue());
    BooleanSetting echest = registerBoolean("Ender Chest", true, () -> blocks.getValue());
    BooleanSetting rod = registerBoolean("End Rod", false, () -> blocks.getValue());
    BooleanSetting anvil = registerBoolean("Anvil", false, () -> blocks.getValue());
    BooleanSetting any = registerBoolean("Any", false, () -> blocks.getValue());


    final Timer concreteTimer = new Timer();
    boolean doGlitch;
    boolean invalidHotbar;
    boolean rotation;
    int oldSlot;
    int targetBlockSlot;
    BlockPos burrowBlockPos;
    int oldslot;
    BlockPos pos;

    public void onEnable() {

        if (rotate.getValue()) {

            rotation = true;

        }

        invalidHotbar = false;

        //BLINK AND TIMER

        if (useBlink.getValue()) {
            ModuleManager.getModule(Blink.class).enable();
        }

        // FIND SLOT

        targetBlockSlot = getBlocks();

        if (targetBlockSlot == -1) {

            MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "No burrow blocks in hotbar, disabling");

            invalidHotbar = true;

            disable();

            if (useBlink.getValue()) {
                ModuleManager.getModule(Blink.class).disable();
            }

            disable();

        }

        // JUMP

        if (!invalidHotbar) {

            if (mc.player.onGround) {


                burrowBlockPos = new BlockPos(Math.ceil(mc.player.posX) - 1, Math.ceil(mc.player.posY - 1) + 1.5, Math.ceil(mc.player.posZ) - 1);


                if (mc.world.isOutsideBuildHeight(burrowBlockPos)) {
                    disable();
                    MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "You are trying to burrow above build limit, disabling.");
                }

                if (jumpMode.getValue().equals("real")) {
                    mc.player.jump();
                    pos = new BlockPos(mc.player.getPositionVector());
                } else {

                    // CIRUU BURROW (not ashamed to admit it)

                    targetBlockSlot = getBlocks();

                    oldSlot = mc.player.inventory.currentItem;

                    if (targetBlockSlot == -1) {
                        MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "You are trying to burrow above build limit, disabling.");
                        disable();
                    }

                    mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));

                    PlayerUtil.fakeJump(!conserve.getValue());

                    PlacementUtil.place(burrowBlockPos, EnumHand.MAIN_HAND, (rotation));

                    getPacket();

                    mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));

                    disable();

                }
                concreteTimer.reset();

                doGlitch = false;

            } else {

                disable();

            }
        }
    }

    public void onUpdate() {

        if (mode.getValue().equalsIgnoreCase("Real")) {

            if (mc.player.posY > Math.floor(pos.y) + 1) {

                targetBlockSlot = getBlocks();

                oldSlot = mc.player.inventory.currentItem;

                if (targetBlockSlot == -1)
                    disable();

                mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));

                PlacementUtil.place(burrowBlockPos, EnumHand.MAIN_HAND, (rotation));

                getPacket();

                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));
            }

        }

    }

    private BlockPos findHoles() {
        NonNullList<BlockPos> holes = NonNullList.create();

        List<BlockPos> blockPosList = EntityUtil.getSquare(new BlockPos(mc.player.posX, range.getValue() / 2f - mc.player.posY, mc.player.posZ), new BlockPos(mc.player.posX, range.getValue() / 2f + mc.player.posY, mc.player.posZ));
        for (BlockPos pos : blockPosList) {

            if (mc.world.isAirBlock(pos.add(0, 1, 0)) && mc.world.isAirBlock(pos) && pos.getDistance(((int) mc.player.posX), ((int) mc.player.posY), ((int) mc.player.posZ)) >= 2) {
                holes.add(pos);
                break;
            }

        }

        if (holes.isEmpty()) {

            blockPosList = EntityUtil.getHollowSphere(PlayerUtil.getPlayerPos(), range.getValue(), range.getValue(), true, 1, 2, 2);

            for (BlockPos pos : blockPosList) {

                if (mc.world.isAirBlock(pos.add(0, 1, 0)) && mc.world.isAirBlock(pos) && pos.getDistance(((int) mc.player.posX), ((int) mc.player.posY), ((int) mc.player.posZ)) >= 2) {
                    holes.add(pos);
                    break;
                }

            }

        }


        return holes.get(0);
    }

    void getPacket() {

        BlockPos pos = findHoles();

        try {
            if (debugpos.getValue())
                MessageBus.sendClientPrefixMessage("Pos: " + (Math.floor(pos.x) + 0.5) + " " + Math.floor(pos.y) + " " + (Math.floor(pos.z) + 0.5) + " " + mc.world.isAirBlock(pos.down()));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(Math.floor(pos.x) + 0.5, Math.floor(pos.y), Math.floor(pos.z) + 0.5, mc.world.isAirBlock(pos.down())));
        } catch (Exception e) {

            MessageBus.sendClientPrefixMessage(String.valueOf(e));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1, mc.player.posZ, false));

        }
    }

    int getBlocks() {

        int current = -1;

        if (any.getValue() && InventoryUtil.findAnyBlockSlot(0, 8) != -1)
            current = InventoryUtil.findAnyBlockSlot(0, 8);
        if (anvil.getValue() && InventoryUtil.findFirstBlockSlot(Blocks.ANVIL.getClass(), 0, 8) != -1)
            current = InventoryUtil.findFirstBlockSlot(Blocks.ANVIL.getClass(), 0, 8);
        if (rod.getValue() && InventoryUtil.findFirstBlockSlot(Blocks.END_ROD.getClass(), 0, 8) != -1)
            current = InventoryUtil.findFirstBlockSlot(Blocks.END_ROD.getClass(), 0, 8);
        if (echest.getValue() && InventoryUtil.findFirstBlockSlot(Blocks.ENDER_CHEST.getClass(), 0, 8) != -1)
            current = InventoryUtil.findFirstBlockSlot(Blocks.ENDER_CHEST.getClass(), 0, 8);
        if (obby.getValue() && InventoryUtil.findFirstBlockSlot(Blocks.OBSIDIAN.getClass(), 0, 8) != -1)
            current = InventoryUtil.findFirstBlockSlot(Blocks.OBSIDIAN.getClass(), 0, 8);

        return current;

    }
}