package com.vomiter.minersdelighttfc.loot;

import com.vomiter.minersdelighttfc.MinersDelightTFC;
import net.dries007.tfc.util.loot.TFCLoot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public enum NoSkyLightCondition implements LootItemCondition {
    INSTANCE;

    private static final LootItemConditionType TYPE = new LootItemConditionType(
            new TFCLoot.InstanceSerializer<>(INSTANCE));

    public static void register(RegisterEvent event) {
        event.register(Registries.LOOT_CONDITION_TYPE,
                helper -> helper.register(MinersDelightTFC.id("no_sky_light"), TYPE));
    }

    @Override
    public @NotNull LootItemConditionType getType() {
        return TYPE;
    }

    @Override
    public @NotNull Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    @Override
    public boolean test(LootContext context) {
        Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
        return origin != null && context.getLevel().getBrightness(LightLayer.SKY, BlockPos.containing(origin)) == 0;
    }
}
