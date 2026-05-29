package com.swill.kaura;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.util.List;
import java.util.Random;

public class KillAuraClient implements ClientModInitializer {
    
    private static boolean enabled = true;
    private static long lastAttackTime = 0;
    private static final long MIN_INTERVAL_MS = 1700;
    private static final long MAX_INTERVAL_MS = 1870;
    private static final double REACH = 3.4;
    private static Random random = new Random();
    
    @Override
    public void onInitializeClient() {
        System.out.println("[SWILL] KillAura загружен");
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled) return;
            if (client.player == null) return;
            if (client.world == null) return;
            
            long now = System.currentTimeMillis();
            if (now - lastAttackTime < MIN_INTERVAL_MS) return;
            
            LivingEntity target = findTarget(client);
            if (target != null) {
                attack(client, target);
                long delay = MIN_INTERVAL_MS + (long)(random.nextDouble() * (MAX_INTERVAL_MS - MIN_INTERVAL_MS));
                lastAttackTime = now;
            }
        });
    }
    
    private LivingEntity findTarget(MinecraftClient client) {
        Vec3d eyePos = client.player.getEyePos();
        Box box = client.player.getBoundingBox().expand(REACH);
        
        List<LivingEntity> entities = client.world.getEntitiesByClass(
            LivingEntity.class,
            box,
            e -> e != client.player && e.isAlive()
        );
        
        for (LivingEntity e : entities) {
            if (eyePos.distanceTo(e.getPos()) <= REACH) {
                return e;
            }
        }
        return null;
    }
    
    private void attack(MinecraftClient client, LivingEntity target) {
        client.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
    }
}
