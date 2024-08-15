package dev.stick_stack.dimensionviewer.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.stick_stack.dimensionviewer.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends MixinPlayer {

    protected MixinServerPlayer(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private static Component createDimensionComponent(ServerPlayer player, MutableComponent originalName) {
        ResourceLocation dimension = player.level().dimension().location();
        String dimSource = CommonUtils.ToTitleCase(CommonUtils.splitResourceLocation(dimension, 0));
        final PlayerListHandlerFabric handler = new PlayerListHandlerFabric();

        Style style = Style.EMPTY;
        boolean foundModdedDim = false;
        if (ConfigFabric.get().PER_DIM_COLOR) {
            for (String modDim : ConfigFabric.get().MODDED_DIMS) {
                if (modDim.split(" ")[0].equals(dimension.toString())) {
                    style = tryGetColor(modDim.split(" ")[1]);
                    foundModdedDim = true;
                    break;
                }
            }

            if (!foundModdedDim) {
                style = switch (dimension.toString()) {
                    case "minecraft:overworld" -> tryGetColor(ConfigFabric.get().OVERWORLD_COLOR);
                    case "minecraft:the_nether" -> tryGetColor(ConfigFabric.get().NETHER_COLOR);
                    case "minecraft:the_end" -> tryGetColor(ConfigFabric.get().END_COLOR);
                    default -> tryGetColor(ConfigFabric.get().DEFAULT_COLOR);
                };
            }
        } else {
            style = tryGetColor(ConfigFabric.get().DEFAULT_COLOR);
        }

        MutableComponent dimComponent = handler.makeDimensionComponent(player, ConfigFabric.get().LIST_FORMAT)
                .withStyle(style);

        if (ConfigFabric.get().CHAT_DIM_HOVER) {
            dimComponent.withStyle(dimComponent.getStyle().withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(dimSource))
            ));
        }

        MutableComponent spacer = MutableComponent.create(new PlainTextContents.LiteralContents(" "));
        if (ConfigFabric.get().DIM_POSITION == CommonUtils.DimensionPosition.PREPEND) {
            spacer.setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)).append(originalName);
            return dimComponent.append(spacer);
        } else {
            spacer.append(dimComponent);
            return originalName.append(spacer);
        }
    }

    @Unique
    private static Style tryGetColor(String color) {
        try {
            ChatFormatting format = ChatFormatting.valueOf(color);
            return Style.EMPTY.withColor(format);
        } catch (IllegalArgumentException exception) {
            for (String entry : ConfigFabric.get().CUSTOM_COLORS) {
                String[] splits = entry.split(" ");

                if (color.equals(splits[0])) {
                    if (splits[1].startsWith("#")) {
                        return Style.EMPTY.withColor(CommonUtils.hexToInt(splits[1]));
                    } else {
                        int r = Integer.parseInt(splits[1].substring(1));
                        int g = Integer.parseInt(splits[2].substring(1));
                        int b = Integer.parseInt(splits[3].substring(1));

                        return Style.EMPTY.withColor(CommonUtils.rgbToInt(r, g, b));
                    }
                }
            }
        }

        Constants.LOG.error("Invalid colour {}! Setting to default...", color);
        return Style.EMPTY;
    }

    @Override
    protected void onRespawn(CallbackInfo ci) {
        FabricUtils.refreshDisplayNames(getServer().getPlayerList());
    }

    @Inject(method = "hasChangedDimension", at = @At("TAIL"))
    private void onChangedDimension(CallbackInfo ci) {
        FabricUtils.refreshDisplayNames(getServer().getPlayerList());
    }

    @Override
    protected void onGetDisplayName(CallbackInfoReturnable<MutableComponent> cir) {
        if (!ConfigFabric.get().DIM_IN_CHAT_NAME) return;

        cir.setReturnValue(createDimensionComponent((ServerPlayer)(Object)this, cir.getReturnValue()).copy());
    }

    @ModifyReturnValue(method = "getTabListDisplayName", at = @At("RETURN"))
    private Component onGetTabListDisplayName(@Nullable Component component) {
        MutableComponent nameComponent = getName().copy();
        return createDimensionComponent((ServerPlayer)(Object)this, nameComponent);
    }
}
