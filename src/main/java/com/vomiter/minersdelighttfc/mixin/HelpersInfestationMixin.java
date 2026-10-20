package com.vomiter.minersdelighttfc.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.vomiter.minersdelighttfc.entity.SilverfishInfestation;
import net.dries007.tfc.common.entities.TFCEntities;
import net.dries007.tfc.common.entities.prey.Pest;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.region.Units;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(value = Helpers.class, remap = false)
public abstract class HelpersInfestationMixin {
    @Unique private static final int mdtfc$MIN_DEPTH = 10;
    @Unique private static final int mdtfc$DEPTH_STEP = 10;
    @Unique private static final int mdtfc$PEST_LIMIT = 8;
    @Unique private static final double mdtfc$PEST_RANGE = 40;

    // Keep TFC's existing probability reduction, but include the replacement pests.
    @ModifyExpressionValue(
            method = "tickInfestation",
            at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"),
            require = 1
    )
    private static int mdtfc$countSilverfish(int pests, Level level, BlockPos pos,
                                                        int infestation, @Nullable Player player) {
        return mdtfc$depth(level, pos) >= mdtfc$MIN_DEPTH
                ? pests + level.getEntitiesOfClass(Silverfish.class,
                new AABB(pos).inflate(mdtfc$PEST_RANGE)).size()
                : pests;
    }

    // Run only after TFC's food/chance/crowding checks and pest selection succeed.
    @WrapOperation(
            method = "tickInfestation",
            at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V"),
            require = 1
    )
    private static void mdtfc$replaceRat(Optional<EntityType<?>> selected,
                                                    Consumer<? super EntityType<?>> spawn,
                                                    Operation<Void> original, Level level, BlockPos pos,
                                                    int infestation, @Nullable Player player) {
        int depth = mdtfc$depth(level, pos);
        if (depth < mdtfc$MIN_DEPTH || selected.isEmpty()
                || selected.get() != TFCEntities.RAT.get()) {
            original.call(selected, spawn);
            return;
        }
        if (!(level instanceof ServerLevel server) || level.getDifficulty() == Difficulty.PEACEFUL) return;

        AABB bounds = new AABB(pos).inflate(mdtfc$PEST_RANGE);
        int nearby = level.getEntitiesOfClass(Pest.class, bounds).size()
                + level.getEntitiesOfClass(Silverfish.class, bounds).size();
        int count = Math.min(1 + (depth - mdtfc$MIN_DEPTH) / mdtfc$DEPTH_STEP,
                mdtfc$PEST_LIMIT - nearby);
        boolean spawned = false;
        for (int i = 0; i < count; i++) {
            Silverfish silverfish = EntityType.SILVERFISH.create(server);
            if (silverfish == null) continue;
            silverfish.moveTo(Vec3.atBottomCenterOf(pos));
            Vec3 target = LandRandomPos.getPos(silverfish, 15, 5);
            if (target == null || !server.hasChunkAt(BlockPos.containing(target))) continue;
            silverfish.moveTo(target);
            if (!server.getWorldBorder().isWithinBounds(silverfish.getBoundingBox())
                    || !server.noCollision(silverfish)) continue;
            ForgeEventFactory.onFinalizeSpawn(silverfish, server,
                    server.getCurrentDifficultyAt(BlockPos.containing(target)), MobSpawnType.EVENT, null, null);
            SilverfishInfestation.setOrigin(silverfish, pos);
            spawned |= server.addFreshEntity(silverfish);
        }
        if (spawned) {
            SilverfishInfestation.addEggs(server, pos);
            if (player != null) {
                player.displayClientMessage(Component.translatable("tfc.tooltip.infestation"), true);
            }
        }
    }

    @Unique
    private static int mdtfc$depth(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel) || !(level.getBlockEntity(pos) instanceof ChestBlockEntity)) return 0;
        ChunkData data = ChunkData.get(level, pos);
        // TFC's terrain cache is unaffected by trees, roofs or later excavation.
        int surfaceY;
        if (data.status() == ChunkData.Status.FULL) {
            surfaceY = data.getRockData().getSurfaceHeight()[Units.index(pos.getX(), pos.getZ())];
        } else {
            surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX(), pos.getZ()) - 1;
        }
        return surfaceY - pos.getY();
    }
}