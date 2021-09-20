/*
package com.gamesense.client.module.modules.misc;


import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import scala.util.Random;

import java.util.Arrays;

@Module.Declaration(name = "AntiAim", category = Category.Misc)
public class AntiAim extends Module {

    float lastTickPitch;
    float lastTickYaw;

    float thisTickPitch;
    float thisTickYaw;

    int nextVal;

    ModeSetting pitchMode = registerMode("pitchMode", Arrays.asList("Random", "Speen", "Custom", "None"), "Random");
    ModeSetting yawMode = registerMode("yawMode", Arrays.asList("Random", "Speen", "Custom", "None"), "Random");

    DoubleSetting customPitch = registerDouble("customPitch", 0, -90, 90);
    DoubleSetting yawYaw = registerDouble("customYaw", 0, -90, 90);
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Send> packetSendListener = new Listener<>(event -> {

        if (PlayerUtil.nullCheck()){
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
        }

        Packet packet = event.getPacket();
        Random r = new Random();

        if (packet instanceof CPacketPlayer) {
            if (pitchMode.getValue().equals("Random")) {
                ((CPacketPlayer) packet).pitch = r.nextInt((90 - -90) + 1) + -90;
            }
            if (yawMode.getValue().equals("Random")) {
                ((CPacketPlayer) packet).yaw = r.nextInt((90 - -90) + 1) + -90;
            }

            if (pitchMode.getValue().equals("Custom")) {
                ((CPacketPlayer) packet).pitch = customPitch.getValue().floatValue();
            }
            if (yawMode.getValue().equals("Custom")) {
                ((CPacketPlayer) packet).yaw = yawYaw.getValue().floatValue();
            }

            if (pitchMode.getValue().equals("Speen")) {
                ((CPacketPlayer) packet).pitch = nextVal;
            }
            if (yawMode.getValue().equals("Speen")) {
                ((CPacketPlayer) packet).yaw = nextVal;
            }

        }

    });
    IntegerSetting speenSpeed = registerInteger("speedSpeed", 10, 0, 50);

    @Override
    public void onUpdate() {
        lastTickPitch = thisTickPitch;
        thisTickPitch = mc.player.rotationPitch;

        lastTickYaw = thisTickYaw;
        thisTickYaw = mc.player.rotationYaw;

        if (lastTickYaw == thisTickYaw || lastTickPitch == thisTickPitch) {
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
        }

        nextVal += speenSpeed.getValue();

    }
}*/
