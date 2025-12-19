package top.qm.industrialplatform.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public class WrenchParticleHandler {
    private WrenchParticleHandler() {
    }

    public static void extendedSpawnAboveBlock(
            ServerLevel level,
            BlockPos pos,
            ParticleOptions particle,
            int count,
            double height
    ) {
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();
        int finX = (int) Math.floor(posX / 16.0) * 16;
        int finZ = (int) Math.floor(posZ / 16.0) * 16;
        for (float i = -16; i <= 32; i = (float) (i + 0.5)) {
            level.sendParticles(
                    particle,
                    finX + i,
                    pos.getY() + height,
                    finZ - 16,
                    count,
                    0.15, 0.05, 0.15,
                    0.0
            );
            level.sendParticles(
                    particle,
                    finX + i,
                    pos.getY() + height,
                    finZ + 32,
                    count,
                    0.15, 0.05, 0.15,
                    0.0
            );
            level.sendParticles(
                    particle,
                    finX-16,
                    pos.getY() + height,
                    finZ + i,
                    count,
                    0.15, 0.05, 0.15,
                    0.0
            );
            level.sendParticles(
                    particle,
                    finX + 32,
                    pos.getY() + height,
                    finZ + i,
                    count,
                    0.15, 0.05, 0.15,
                    0.0
            );
        }
    }


    public static void spawnAboveBlock(
            ServerLevel level,
            BlockPos pos,
            ParticleOptions particle,
            int count,
            double height
    ) {
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();
        int finX = (int) Math.floor(posX / 16.0) * 16;
        int finZ = (int) Math.floor(posZ / 16.0) * 16;
        for (float i = 0; i <= 16; i = (float) (i + 0.5)) {
            level.sendParticles(
                    particle,
                    finX + i,
                    pos.getY() + height,
                    finZ,
                    count,
                    0.15, 0.05, 0.15,
                    0.0
            );
            level.sendParticles(
                    particle,
                    finX + i,
                    pos.getY() + height,
                    finZ + 16,
                    count,
                    0.15, 0.05, 0.15,
                    0.0
            );
            level.sendParticles(
                    particle,
                    finX,
                    pos.getY() + height,
                    finZ + i,
                    count,
                    0.15, 0.05, 0.15,
                    0.0
            );
            level.sendParticles(
                    particle,
                    finX + 16,
                    pos.getY() + height,
                    finZ + i,
                    count,
                    0.15, 0.05, 0.15,
                    0.0
            );
        }
    }

    public static void levitation(ServerLevel level, BlockPos pos) {
        spawnAboveBlock(level, pos, ParticleTypes.FIREWORK, 1, 1.0);
    }

    public static void extendedLevitation(ServerLevel level, BlockPos pos) {
        extendedSpawnAboveBlock(level, pos, ParticleTypes.FIREWORK, 1, 1.0);
    }

    public static void filling(ServerLevel level, BlockPos pos) {
        spawnAboveBlock(level, pos, ParticleTypes.FLAME, 1, 1.0);
    }

    public static void extendedFilling(ServerLevel level, BlockPos pos) {
        extendedSpawnAboveBlock(level, pos, ParticleTypes.FLAME, 1, 1.0);
    }
}

