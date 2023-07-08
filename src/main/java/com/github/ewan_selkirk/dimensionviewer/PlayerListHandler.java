package com.github.ewan_selkirk.dimensionviewer;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
    private static final Map<String, ResourceLocation> players = new HashMap<>();
    private static List<ServerPlayer> playerList = new ArrayList<>();

    // Map tokens to their respective codes for ease of use
    private static final Map<String, String> tokens = new HashMap<>() {
        {
            put("%i", "§o");
            put("%b", "§l");
            put("%u", "§n");
            put("%o", "§k");
            put("%s", "§m");
            put("%r", "§r");
        }
    };

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static synchronized void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        players.put(event.getEntity().getStringUUID(), event.getTo().location());

        updatePlayerList();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static synchronized void changeUserDisplayName(PlayerEvent.TabListNameFormat event) {
        // If the display name has been changed with the PlayerEvent#NameFormat event we can simply use that
        // for the tab list name format.
        if (Config.DIM_IN_CHAT_NAME.get()) {
            event.setDisplayName(event.getEntity().getDisplayName());
            return;
        }

        try {
            event.setDisplayName(Component.empty().append(replaceTokens(event)));
        } catch (NullPointerException e) {

        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static synchronized void changeUserChatName(PlayerEvent.NameFormat event) {
        if (!Config.DIM_IN_CHAT_NAME.get()) return;

        try {
            MutableComponent name = Component.empty();
            name.append(replaceTokens(event));

            if (!Config.CHAT_DIM_HOVER.get()) {
                event.setDisplayname(name);
                return;
            }

            name.setStyle(Style.EMPTY.withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(getSource(event)))
            ));

            event.setDisplayname(name);

        } catch (NullPointerException e) {
        }

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static synchronized void onPlayerConnect(PlayerEvent.PlayerLoggedInEvent event) {
        playerList = Objects.requireNonNull(event.getEntity().getServer()).getPlayerList().getPlayers();
        playerList.forEach(p -> players.put(p.getStringUUID(), p.level.dimension().location()));

        updatePlayerList();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static synchronized void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        players.remove(event.getEntity().getStringUUID());
        playerList = Objects.requireNonNull(event.getEntity().getServer()).getPlayerList().getPlayers();

        updatePlayerList();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static synchronized void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        try {
            players.put(event.getEntity().getStringUUID(), event.getEntity().level.dimension().location());
            updatePlayerList();
        } catch (NullPointerException e) {

        }
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
        String dimension = getDimension(event);
        tokens.put("%d", dimension);

        // Get player name and add it to the list of tokens
        String name = event.getEntity().getName().getString();
        tokens.put("%p", name);

        for (var s: tokens.keySet()) {
            format = format.replace(s, tokens.get(s));
        }

        if (!per_dim_colors) {
            format = format.replace("%c", color.value);
        } else {
            // Check for modded dimension settings first
            for (String dim : Config.MODDED_DIMS.get()) {
                if ((!players.get(event.getEntity().getStringUUID()).toString().equals(dim.split(" ")[0]))) {
                    continue;
                }
                format = format.replace("%c", Config.FontColor.valueOf(dim.split(" ")[1]).value);
                break;
            }

            format = format.replace("%c", switch (players.get(event.getEntity().getStringUUID()).toString()) {
                case "minecraft:overworld" -> dim_colors[0].value;
                case "minecraft:the_nether" -> dim_colors[1].value;
                case "minecraft:the_end" -> dim_colors[2].value;
                default -> color.value;
            });
        }

        return format;
    }

    /**
     * Method for updating the player list. Should be called anytime a change
     * is made to the player list (E.G. player connects/disconnects)
     * @return Whether the player list was updated successfully.
     */
    private static boolean updatePlayerList() {
        if (playerList.size() > 0) {
            playerList = Objects.requireNonNull(playerList.get(0).getServer()).getPlayerList().getPlayers();

            if (Config.DIM_IN_CHAT_NAME.get()) playerList.forEach(ServerPlayer::refreshDisplayName);
            playerList.forEach(ServerPlayer::refreshTabListName);

            return true;
        }

        return false;
    }

    /**
     * Returns the source of a resource location (E.G. minecraft:the_end -> Minecraft)
     * @param event A player event to get the player UUID from.
     * @return The source as a Title-case string.
     */
    private static String getSource(PlayerEvent event) {
        return makeTitleCase(splitResourceLocation(players.get(event.getEntity().getStringUUID()), 0));
    }

    /**
     * Returns the dimension of a resource location (E.G. minecraft:the_end -> The End)
     * @param event A player event to get the player UUID from.
     * @return The dimension as a Title-case string.
     */
    private static String getDimension(PlayerEvent event) {

        // Check if there is a custom alias for the dimension
        if (Config.ENABLE_ALIASES.get()) {
            for (int i = 0; i < Config.DIMENSION_ALIASES.get().size(); i++) {
                if (Config.DIMENSION_ALIASES.get().get(i).contains(players.get(event.getEntity().getStringUUID()).toString())) {
                    return Config.DIMENSION_ALIASES.get().get(i).split(" ", 2)[1];
                }
            }
        }

        return makeTitleCase(splitResourceLocation(players.get(event.getEntity().getStringUUID()), 1));
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
        public static void CommandRegistration(RegisterCommandsEvent event) {
            event.getDispatcher().register(
                    Commands.literal("refreshPlayerList").executes(ctx -> {
                        if (PlayerListHandler.updatePlayerList()) {
                            ctx.getSource().sendSuccess(Component.nullToEmpty("[Dimension Viewer] Manually refreshing player list..."),
                                    true);
                        } else {
                            ctx.getSource().sendFailure(Component.nullToEmpty("[Dimension Viewer] Could not manually refresh. No players detected..."));
                        }
                        return 0;
                    })
            );

            event.getDispatcher().register(
                    Commands.literal("getdimensionid").executes(ctx -> {
                        try {
                            ctx.getSource().sendSuccess(
                                    Component.nullToEmpty(
                                            ctx.getSource().getPlayerOrException().level.dimension().location().toString()),
                                    false);
                        } catch (CommandSyntaxException exception) {
                            ctx.getSource().sendFailure(
                                    Component.nullToEmpty("[Dimension Viewer] The command could not detect a player.")
                            );
                        }

                        return 0;
                    })
            );
        }
    }

}
