package ladysnake.pandemonium;

import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import ladysnake.pandemonium.common.entity.ability.*;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.requiem.api.v1.event.requiem.PossessionStartCallback;
import ladysnake.requiem.common.entity.ability.MeleeAbility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.Identifier;

public class PandemoniumRequiemPlugin implements RequiemPlugin {

    @Override
    public void onRequiemInitialize() {
        PossessionStartCallback.EVENT.register(Pandemonium.id("shell_interaction"), (target, possessor) -> {
            if (target instanceof PlayerShellEntity) {
                ((PlayerShellEntity) target).onSoulInteract(possessor);
                return PossessionStartCallback.Result.HANDLED;
            }
            return PossessionStartCallback.Result.PASS;
        });
        // Shulkers are a specific kind of boring, so we let players leave them regardless of their level
        PossessionStartCallback.EVENT.register(Requiem.id("shulker"), (target, possessor) -> {
            if (target instanceof ShulkerEntity && target.world.isClient) {
                MinecraftClient client = MinecraftClient.getInstance();
                client.inGameHud.setOverlayMessage(I18n.translate("requiem:shulker.onboard", client.options.keySneak.getLocalizedName()), false);
            }
            return PossessionStartCallback.Result.PASS;
        });
        // Enderman specific behaviour is unneeded now that players can possess them
        PossessionStartCallback.EVENT.unregister(new Identifier(Requiem.MOD_ID, "enderman"));
        PossessionStartCallback.EVENT.register(Pandemonium.id("allow_everything"), (target, possessor) -> PossessionStartCallback.Result.ALLOW);
    }

    @Override
    public void registerMobAbilities(MobAbilityRegistry abilityRegistry) {
        abilityRegistry.register(EntityType.BLAZE, MobAbilityConfig.builder().indirectAttack(BlazeFireballAbility::new).build());
        abilityRegistry.register(EntityType.CAT, MobAbilityConfig.builder().directAttack(e -> new MeleeAbility(e, true)).build());
        abilityRegistry.register(EntityType.CREEPER, MobAbilityConfig.<CreeperEntity>builder().indirectAttack(CreeperPrimingAbility::new).build());
        abilityRegistry.register(EntityType.ENDERMAN, MobAbilityConfig.builder().indirectInteract(BlinkAbility::new).build());
        abilityRegistry.register(EntityType.EVOKER, MobAbilityConfig.<EvokerEntity>builder()
                .directAttack(EvokerFangAbility::new)
                .directInteract(EvokerWololoAbility::new)
                .indirectInteract(EvokerVexAbility::new)
                .build());
        abilityRegistry.register(EntityType.GHAST, MobAbilityConfig.builder().indirectAttack(GhastFireballAbility::new).build());
        abilityRegistry.register(EntityType.GUARDIAN, MobAbilityConfig.<GuardianEntity>builder().directAttack(GuardianBeamAbility::new).build());
        abilityRegistry.register(EntityType.IRON_GOLEM, MobAbilityConfig.builder().directAttack(e -> new MeleeAbility(e, true)).build());
        abilityRegistry.register(EntityType.OCELOT, MobAbilityConfig.builder().directAttack(e -> new MeleeAbility(e, true)).build());
        abilityRegistry.register(EntityType.SHULKER, MobAbilityConfig.<ShulkerEntity>builder()
                .directAttack(ShulkerShootAbility::new)
                .indirectAttack(ShulkerShootAbility::new)
                .indirectInteract(ShulkerPeekAbility::new).build());
    }
}
