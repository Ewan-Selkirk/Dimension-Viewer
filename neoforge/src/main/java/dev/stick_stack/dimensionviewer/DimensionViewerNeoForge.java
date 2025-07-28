package dev.stick_stack.dimensionviewer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

@Mod(Constants.MOD_ID)
public class DimensionViewerNeoForge {

    public DimensionViewerNeoForge(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, ConfigNeoForge.CONFIG);
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.DEDICATED_SERVER)
    private static class PlayerEventHandler {

        private static void refreshPlayerDetails(PlayerEvent event) {
            List<ServerPlayer> players = event.getEntity().getServer().getPlayerList().getPlayers();

            players.forEach(ServerPlayer::refreshDisplayName);
            players.forEach(ServerPlayer::refreshTabListName);
        }

        private static Style tryGetColor(String color) {
            try {
                ChatFormatting format = ChatFormatting.valueOf(color);
                return Style.EMPTY.withColor(format);
            } catch (IllegalArgumentException exception) {
                for (String entry : ConfigNeoForge.CUSTOM_COLORS.get()) {
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

            return Style.EMPTY;
        }

        private static Component createDimensionComponent(PlayerEvent event, MutableComponent originalName) {
            ResourceLocation dimension = event.getEntity().level().dimension().location();
            String dimSource = CommonUtils.toTitleCase(CommonUtils.splitResourceLocation(dimension, 0));
            final PlayerListHandlerNeoForge handler = new PlayerListHandlerNeoForge();

            Style style = Style.EMPTY;
            boolean foundModdedDim = false;
            if (ConfigNeoForge.PER_DIM_COLOR.get()) {
                for (String modDim : ConfigNeoForge.MODDED_DIMS.get()) {
                    if (modDim.split(" ")[0].equals(dimension.toString())) {
                        style = tryGetColor(modDim.split(" ")[1]);
                        foundModdedDim = true;
                        break;
                    }
                }

                if (!foundModdedDim) {
                    style = switch (dimension.toString()) {
                        case "minecraft:overworld" -> tryGetColor(ConfigNeoForge.OVERWORLD_COLOR.get());
                        case "minecraft:the_nether" -> tryGetColor(ConfigNeoForge.NETHER_COLOR.get());
                        case "minecraft:the_end" -> tryGetColor(ConfigNeoForge.END_COLOR.get());
                        default -> tryGetColor(ConfigNeoForge.DEFAULT_COLOR.get());
                    };
                }
            } else {
                style = tryGetColor(ConfigNeoForge.DEFAULT_COLOR.get());
            }

            MutableComponent dimComponent = handler.makeDimensionComponent(event.getEntity(), ConfigNeoForge.LIST_FORMAT.get())
                    .withStyle(style);

            if (ConfigNeoForge.CHAT_DIM_HOVER.get()) {
                dimComponent.withStyle(dimComponent.getStyle().withHoverEvent(
                        new HoverEvent.ShowText(Component.literal(dimSource))
                ));
            }

            MutableComponent spacer = MutableComponent.create(new PlainTextContents.LiteralContents(" "));
            if (ConfigNeoForge.DIM_POSITION.get() == CommonUtils.DimensionPosition.PREPEND) {
                spacer.setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)).append(originalName);
                return dimComponent.append(spacer);
            } else {
                spacer.append(dimComponent);
                return originalName.append(spacer);
            }
        }

        @SubscribeEvent
        public static void onPlayerConnect(PlayerEvent.PlayerLoggedInEvent event) {
            PlayerListHandler.playerList.add(event.getEntity());
            refreshPlayerDetails(event);
        }

        @SubscribeEvent
        public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
            PlayerListHandler.playerList.remove(event.getEntity());
            refreshPlayerDetails(event);
        }

        @SubscribeEvent
        public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
            refreshPlayerDetails(event);
        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            refreshPlayerDetails(event);
        }

        @SubscribeEvent
        public static void changeDisplayName(PlayerEvent.NameFormat event) {
            if (!ConfigNeoForge.DIM_IN_CHAT_NAME.get()) return;

            event.setDisplayname(createDimensionComponent(event, event.getDisplayname().copy()));
        }

        @SubscribeEvent
        public static void changeTabListName(PlayerEvent.TabListNameFormat event) {
            if (ConfigNeoForge.DIM_IN_CHAT_NAME.get()) {
                event.setDisplayName(event.getEntity().getDisplayName());
            } else {
                MutableComponent originalName = event.getEntity().getDisplayName().copy();
                event.setDisplayName(createDimensionComponent(event, originalName));
            }
        }
    }

    @EventBusSubscriber(modid = Constants.MOD_ID)
    private static class ModEventBus {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            CustomCommands.RegisterCommands(event.getDispatcher());
        }
    }

    @EventBusSubscriber(modid = Constants.MOD_ID)
    private static class ModEventBusEvents {
        @SubscribeEvent
        public static void onConfigReloaded(ModConfigEvent.Reloading event) {
            if (event.getConfig().getModId().contains(Constants.MOD_ID)) {
                Constants.LOG.info("Config file reloaded!");

                if (!PlayerListHandler.playerList.isEmpty()) {
                    MinecraftServer server = PlayerListHandler.playerList.get(0).getServer();

                    // Refresh display name first as tab list name uses that if DIM_IN_CHAT_NAME is true
                    server.getPlayerList().getPlayers().forEach(ServerPlayer::refreshDisplayName);
                    server.getPlayerList().getPlayers().forEach(ServerPlayer::refreshTabListName);
                } else {
                    Constants.LOG.info("Skipping player refresh as there are no players...");
                }
            }
        }
    }
}
