package tech.alexnijjar.endermanoverhaul.common.items.pearls;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.alexnijjar.endermanoverhaul.common.entities.projectiles.ThrownSoulPearl;
import tech.alexnijjar.endermanoverhaul.common.tags.ModEntityTypeTags;

import java.util.List;

public class SoulPearlItem extends Item {
    public SoulPearlItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        if (player.isShiftKeyDown()) return InteractionResultHolder.pass(player.getItemInHand(usedHand));
        ItemStack itemStack = player.getItemInHand(usedHand);
        int id = itemStack.getOrCreateTag().getInt("BoundEntity");
        Entity entity = level.getEntity(id);
        if (entity == null) return InteractionResultHolder.fail(itemStack);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_PEARL_THROW, SoundSource.NEUTRAL, 0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));
        player.getCooldowns().addCooldown(this, 20);
        if (!level.isClientSide()) {
            ThrownSoulPearl pearl = new ThrownSoulPearl(level, player, entity);
            pearl.setItem(itemStack);
            pearl.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
            level.addFreshEntity(pearl);
            player.getItemInHand(usedHand).removeTagKey("BoundEntity");
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity interactionTarget, @NotNull InteractionHand usedHand) {
        if (!player.level().isClientSide() && player.isShiftKeyDown() && !interactionTarget.getType().is(ModEntityTypeTags.IGNORE_SOUL_PEARL)) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt("BoundEntity", interactionTarget.getId());
            player.displayClientMessage(Component.translatable("tooltip.endermanoverhaul.bound_to", interactionTarget.getDisplayName().getString()), true);
            player.getItemInHand(usedHand).setTag(tag);
            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("tooltip.endermanoverhaul.soul_pearl_1").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.endermanoverhaul.soul_pearl_2").withStyle(ChatFormatting.GRAY));

        if (level == null) return;
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("BoundEntity")) {
            int id = tag.getInt("BoundEntity");
            Entity entity = level.getEntity(id);
            if (entity == null) {
                tooltipComponents.add(Component.translatable("tooltip.endermanoverhaul.not_bound").withStyle(ChatFormatting.RED));
            } else {
                tooltipComponents.add(Component.translatable("tooltip.endermanoverhaul.bound_to", entity.getDisplayName().getString()).withStyle(ChatFormatting.GREEN));
            }
        } else {
            tooltipComponents.add(Component.translatable("tooltip.endermanoverhaul.not_bound").withStyle(ChatFormatting.RED));
        }
    }
}
