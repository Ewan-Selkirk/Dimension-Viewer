package com.github.ewanselkirk.dimensionviewer;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.*;

@Mod.EventBusSubscriber(modid = DimensionViewer.MODID, value = Dist.DEDICATED_SERVER)
public class PlayerListHandler {
    private final static Map<String, ResourceLocation> players = new HashMap<>();
    private static List<ServerPlayer> playerList = new ArrayList<>();
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static synchronized void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getPlayer();

        players.put(player.getStringUUID(), event.getTo().location());

        updatePlayerList();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static synchronized void changeUserDisplayName(PlayerEvent.TabListNameFormat event) {
        if (Config.DIM_IN_CHAT_NAME.get()) {
            event.setDisplayName(event.getPlayer().getDisplayName());
            return;
        }

        try {
            event.setDisplayName(new TextComponent(event.getPlayer().getScoreboardName())
                    .append(replaceTokens(event)));

        } catch (NullPointerException exception) {
//            DimensionViewer.LOGGER.log(Level.WARNING, exception.getMessage());
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static synchronized void changeUserChatName(PlayerEvent.NameFormat event) {
        if (!Config.DIM_IN_CHAT_NAME.get()) return;

        try {
            event.setDisplayname(event.getDisplayname().copy().append(
                new TextComponent(replaceTokens(event))).setStyle(Style.EMPTY.withHoverEvent(
                    !Config.CHAT_DIM_HOVER.get() ? null : new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new TextComponent(
                            makeTitleCase(
                                splitResourceLocation(players.get(event.getPlayer().getStringUUID()), 0)
                            )
                        )
                    )
                ))
            );
        } catch (NullPointerException exception) {
//            DimensionViewer.LOGGER.log(Level.WARNING, exception.getMessage());
        }

    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static synchronized void onPlayerConnect(PlayerEvent.PlayerLoggedInEvent event) {
        playerList = Objects.requireNonNull(event.getPlayer().getServer()).getPlayerList().getPlayers();
        playerList.forEach(p -> players.put(p.getStringUUID(), p.level.dimension().location()));

        updatePlayerList();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static synchronized void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        players.remove(event.getPlayer().getStringUUID());
        playerList = Objects.requireNonNull(event.getPlayer().getServer()).getPlayerList().getPlayers();

        updatePlayerList();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static synchronized void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        players.put(event.getPlayer().getStringUUID(),
                event.getPlayer().level.dimension().location());

        updatePlayerList();
    }

    /**
     * Method for updating the player list. Should be called anytime a change
     * is made to the player list (E.G. player connects/disconnects)
     */
    private static void updatePlayerList() {
        if (Config.DIM_IN_CHAT_NAME.get()) playerList.forEach(ServerPlayer::refreshDisplayName);
        playerList.forEach(ServerPlayer::refreshTabListName);
    }

    private static String splitResourceLocation(ResourceLocation key, int pos) {
        String txt = key.toString();

        return txt.split(":")[pos];
    }

    /**
     * A small function for changing an input string to title case (E.G. hello world -> Hello World)
     *
     * @param text The string to make title case.
     * @return String in title case
     */
    private static String makeTitleCase(String text) {
        text = text.replace("_", " ");

        if (text.length() <= 2 || (text.startsWith(" ") || text.endsWith(" "))) {
            text = text.toUpperCase(Locale.ROOT);
        } else {
            String[] split = text.split(" ");
            StringBuilder builder = new StringBuilder();
            int count = 0;

            for (var s: split) {
                split[count] = (Character.toUpperCase(s.charAt(0)) + s.substring(1));
                builder.append(split[count]);

                if (count != split.length - 1) builder.append(' ');

                count++;
            }

            text = builder.toString();
        }

        return text;
    }

    private static String replaceTokens(PlayerEvent event) {
        String format = Config.LIST_FORMAT.get();
        Config.FontColor color = Config.FONT_COLOR.get();
        boolean per_dim_colors = Config.PER_DIM_COLOR.get();
        Config.FontColor[] dim_colors = {
                Config.OVERWORLD_COLOR.get(),
                Config.NETHER_COLOR.get(),
                Config.END_COLOR.get()
        };

        // Get player dimension from the 'players' map
        String dimension = makeTitleCase(splitResourceLocation(
                players.get(event.getPlayer().getStringUUID()), 1)
        );

        // Map tokens to their respective codes for ease of use
        final Map<String, String> tokens = new HashMap<>() {
            {
                put("%d", dimension);
                put("%i", "\u00A7o");
                put("%b", "\u00A7l");
                put("%u", "\u00A7n");
                put("%o", "\u00A7k");
                put("%s", "\u00A7m");
                put("%r", "\u00A7r");
            }
        };

        for (var s: tokens.keySet()) {
            format = format.replace(s, tokens.get(s));
        }

        if (!per_dim_colors) {
            format = format.replace("%c", color.value);
        } else {
            format = format.replace("%c", switch (players.get(event.getPlayer().getStringUUID()).toString()) {
                case "minecraft:overworld" -> dim_colors[0].value;
                case "minecraft:the_nether" -> dim_colors[1].value;
                case "minecraft:the_end" -> dim_colors[2].value;
                default -> color.value;
            });
        }

        return format;
    }

    @Mod.EventBusSubscriber(modid = DimensionViewer.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {
        @SubscribeEvent
        public static void onConfigChanged(ModConfigEvent.Reloading event) {
            if (event.getConfig().getModId().contains(DimensionViewer.MODID)) {
                updatePlayerList();
            }
        }
    }

    @Mod.EventBusSubscriber(modid = DimensionViewer.MODID)
    public static class RegisterCommands {
        @SubscribeEvent
        public static void refreshPlayerDimensionsCommand(RegisterCommandsEvent event) {
            event.getDispatcher().register(
                    Commands.literal("refreshPlayerList").executes(ctx -> {
                        ctx.getSource().sendSuccess(Component.nullToEmpty("Manually refreshing player list..."),
                                true);

                        PlayerListHandler.updatePlayerList();
                        return 0;
                    })
            );
        }
    }

}
