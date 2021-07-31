package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

import java.util.Arrays;

@Module.Declaration(name = "AirJump", category = Category.Movement)
public class AirJump extends Module {

    ModeSetting mode = registerMode("Mode", Arrays.asList("Single", "Repeat"), "Single");
    IntegerSetting repeat = registerInteger("Repeat", 19,1,20, () -> mode.getValue().equalsIgnoreCase("Repeat"));

    @Override
    public void onUpdate() {
        if (mode.getValue().equalsIgnoreCase("Single")){
            if (mc.gameSettings.keyBindJump.isPressed()) {
                mc.player.jump();
            }
        } else if (mode.getValue().equalsIgnoreCase("Repeat") && mc.player.ticksExisted % repeat.getValue() == 0 && mc.gameSettings.keyBindJump.isKeyDown()) {

            mc.player.jump();

        }
    }
}
