package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.BoundingBoxEvent;
import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.PhaseUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

import java.util.Arrays;

@Module.Declaration(name = "PhaseWalk", category = Category.Movement)
public class PhaseWalk extends Module {

    ModeSetting mode = registerMode("Mode", Arrays.asList("NCP", "Vanilla"), "NCP");
    BooleanSetting h = registerBoolean("Keep Floor", false, () -> mode.getValue().equalsIgnoreCase("Vanilla"));
    ModeSetting bound = registerMode("Bounds", PhaseUtil.bound, "Min", () -> mode.getValue().equalsIgnoreCase("NCP"));
    BooleanSetting clipCheck = registerBoolean("Clipped Check", false, () -> mode.getValue().equalsIgnoreCase("NCP"));
    BooleanSetting update = registerBoolean("Update Pos", false, () -> mode.getValue().equalsIgnoreCase("NCP"));
    BooleanSetting sprint = registerBoolean("Sprint Force Enable", true, () -> mode.getValue().equalsIgnoreCase("NCP"));

    int tpid = 0;

    @EventHandler
    private final Listener<BoundingBoxEvent> boundingBoxEventListener = new Listener<>(event -> {

        if (mode.getValue().equalsIgnoreCase("Vanilla"))
            if (event.getPos().y >= mc.player.getPositionVector().y || !h.getValue())
                event.setbb(Block.NULL_AABB);

    });

    @Override
    public void onUpdate() {
        if (mc.player.collidedHorizontally && !ModuleManager.getModule(Flight.class).isEnabled() && mode.getValue().equalsIgnoreCase("NCP"))
            packetFly();
    }

    void packetFly() {

        double[] clip = MotionUtil.forward(0.0624);

        if ((PlayerUtil.isPlayerClipped(false) || !clipCheck.getValue()) || (mc.gameSettings.keyBindSprint.isKeyDown() && sprint.getValue()) || mc.gameSettings.keyBindSneak.isKeyDown()) {

            if (mc.gameSettings.keyBindSneak.isKeyDown() && mc.player.onGround)
                tp(mc.player.posX + clip[0], mc.player.posY - 0.0624, mc.player.posZ + clip[1], false);
            else
                tp(mc.player.posX + clip[0], mc.player.posY, mc.player.posZ + clip[1], true);

        }

    }

    void tp(double x, double y, double z, boolean onGround) {

        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, onGround));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid -1));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid + 1));
        PhaseUtil.doBounds(bound.getValue(), true);

        if (update.getValue())
            mc.player.setPosition(x, y, z);

    }

    @SuppressWarnings("Unused")
    @EventHandler
    private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {

        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            tpid = ((SPacketPlayerPosLook) event.getPacket()).teleportId;
        }

    });

    @SuppressWarnings("Unused")
    @EventHandler
    private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {

        if (event.getPacket() instanceof CPacketPlayer.PositionRotation || event.getPacket() instanceof CPacketPlayer.Position) {
            tpid++;
        }

    });

}
