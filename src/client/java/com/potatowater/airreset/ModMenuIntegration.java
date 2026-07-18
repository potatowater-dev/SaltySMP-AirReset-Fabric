package com.potatowater.airreset;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            AirResetConfig cfg = AirResetConfig.INSTANCE;
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("AirReset"))
                    .setSavingRunnable(AirResetConfig::save);

            ConfigEntryBuilder entries = builder.entryBuilder();
            ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

            general.addEntry(entries.startBooleanToggle(Component.literal("Enabled"), cfg.enabled)
                    .setDefaultValue(true)
                    .setTooltip(Component.literal("Master toggle. Still auto-disables off whitelist."))
                    .setSaveConsumer(value -> cfg.enabled = value)
                    .build());

            general.addEntry(entries.startFloatField(Component.literal("Min pitch"), cfg.minPitch)
                    .setDefaultValue(80.0F)
                    .setMin(0.0F)
                    .setMax(90.0F)
                    .setTooltip(Component.literal("How far down you must look (90 = straight down)."))
                    .setSaveConsumer(value -> cfg.minPitch = value)
                    .build());

            general.addEntry(entries.startDoubleField(Component.literal("Required downward velocity"), cfg.requiredDownwardVelocity)
                    .setDefaultValue(0.8D)
                    .setMin(0.01D)
                    .setMax(10.0D)
                    .setTooltip(Component.literal("Minimum fall speed to trigger (threshold)."))
                    .setSaveConsumer(value -> cfg.requiredDownwardVelocity = value)
                    .build());

            general.addEntry(entries.startIntField(Component.literal("Settle ticks"), cfg.settleTicks)
                    .setDefaultValue(5)
                    .setMin(1)
                    .setMax(40)
                    .setTooltip(Component.literal("How long to keep cancelling upward/downward knock after the reset."))
                    .setSaveConsumer(value -> cfg.settleTicks = value)
                    .build());

            ConfigCategory servers = builder.getOrCreateCategory(Component.literal("Servers"));
            List<String> whitelistCopy = new ArrayList<>(cfg.serverWhitelist);

            servers.addEntry(entries.startStrList(Component.literal("Server whitelist"), whitelistCopy)
                    .setDefaultValue(new ArrayList<>())
                    .setTooltip(Component.literal(
                            "Only enable AirReset on these servers. Empty = all servers. "
                                    + "Use host or host:port. For singleplayer add \"singleplayer\"."
                    ))
                    .setSaveConsumer(value -> {
                        cfg.serverWhitelist = value == null ? new ArrayList<>() : new ArrayList<>(value);
                        var client = net.minecraft.client.Minecraft.getInstance();
                        if (client.player != null) {
                            AirResetClient.evaluateServerGate(client, true);
                        }
                    })
                    .build());

            return builder.build();
        };
    }
}
