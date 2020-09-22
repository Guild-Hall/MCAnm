package com.github.worldsender.mcanm.client.config;

import com.github.worldsender.mcanm.Reference;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

public class MCAnmConfiguration {
    public static Pair<MCAnmConfiguration, ForgeConfigSpec> createConfigSpec() {
        Pair<MCAnmConfiguration, ForgeConfigSpec> builder = new ForgeConfigSpec.Builder().configure(MCAnmConfiguration::new);
        return builder;
    }

    private BooleanValue enableReload;

    public MCAnmConfiguration(ForgeConfigSpec.Builder builder) {
        enableReload = builder.comment("Enable reloading of models when the resource manager is reloaded")
               .translation(Reference.gui_config_reload_enabled)
               .define(Reference.config_reload_enabled, true);
    }

    public boolean isReloadEnabled() {
        return enableReload.get().booleanValue();
    }
}
