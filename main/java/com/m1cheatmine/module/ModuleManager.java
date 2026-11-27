package com.m1cheatmine.module;

import com.m1cheatmine.module.impl.combat.*;
import com.m1cheatmine.module.impl.hud.*;
import com.m1cheatmine.module.impl.misc.*;
import com.m1cheatmine.module.impl.movement.*;
import com.m1cheatmine.module.impl.player.*;
import com.m1cheatmine.module.impl.render.*;
import com.m1cheatmine.module.impl.render.StorageEspModule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    public static final List<Module> modules = new ArrayList<>();

    public static void init() {
        modules.add(new KillAuraModule());
        modules.add(new CrystalAuraModule());
        modules.add(new AnchorAuraModule());
        modules.add(new SurroundModule());
        modules.add(new AutoTotemModule());
        modules.add(new CriticalsModule());
        modules.add(new VelocityModule());
        modules.add(new AimAssistModule());
        modules.add(new TriggerBotModule());
        modules.add(new AutoArmorModule());
        modules.add(new AutoLogModule());
        modules.add(new HitboxesModule());
        modules.add(new ReachModule());
        modules.add(new SpearAuraModule());
        modules.add(new MaceAuraModule());

        modules.add(new FlyModule());
        modules.add(new SpeedModule());
        modules.add(new StepModule());
        modules.add(new ReverseStepModule());
        modules.add(new NoFallModule());
        modules.add(new JesusModule());
        modules.add(new PhaseModule());
        modules.add(new ElytraFlyModule());
        modules.add(new ScaffoldModule());
        modules.add(new BoatFlyModule());
        modules.add(new ToggleSprintModule());
        modules.add(new NoSlowModule());

        modules.add(new EspModule());
        modules.add(new TracersModule());
        modules.add(new XrayModule());
        modules.add(new FullbrightModule());
        modules.add(new FreeCamModule());
        modules.add(new NametagsModule());
        modules.add(new NoRenderModule());
        modules.add(new HoleEspModule());
        modules.add(new StorageEspModule());

        modules.add(new NukerModule());
        modules.add(new AutoMineModule());
        modules.add(new FastPlaceModule());
        modules.add(new ChestStealerModule());
        modules.add(new InventoryManagerModule());
        modules.add(new AutoEatModule());
        modules.add(new FakePlayerModule());
        modules.add(new TimerModule());

        modules.add(new BlinkModule());
        modules.add(new PacketFlyModule());
        modules.add(new AntiAfkModule());
        modules.add(new AutoReconnectModule());

        modules.add(new FpsModule());
        modules.add(new CpsModule());
        modules.add(new KeystrokesModule());
        modules.add(new WatermarkModule());
        modules.add(new ArrayListModule());
    }

    public static List<Module> getByCategory(Category c) {
        return modules.stream().filter(m -> m.category == c).collect(Collectors.toList());
    }

    public static <T extends Module> T getModule(Class<T> clazz) {
        for (Module m : modules) {
            if (clazz.isInstance(m)) {
                return clazz.cast(m);
            }
        }
        return null;
    }
}