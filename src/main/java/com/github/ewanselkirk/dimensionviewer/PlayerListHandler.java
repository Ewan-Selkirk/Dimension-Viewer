package com.github.ewanselkirk.dimensionviewer;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.*;
import java.util.logging.Level;

@Mod.EventBusSubscriber(modid = DimensionViewer.MODID, value = Dist.DEDICATED_SERVER)
public class PlayerListHandler {

    private static Map<String, String> players = new HashMap<>();
    private static List<ServerPlayer> playerList = new ArrayList<>();
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static synchronized void onPlayerDimensionChange(final PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getPlayer();

        players.put(player.getScoreboardName(), event.getTo().location().toString());

        updatePlayerList();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static synchronized void changeUserDisplayName(PlayerEvent.TabListNameFormat event){
        try {
            event.setDisplayName(event.getPlayer().getDisplayName().copy().append(replaceTokens(event)));
        } catch (NullPointerException exception) {
            DimensionViewer.LOGGER.log(Level.WARNING, exception.getMessage());
        }

    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static synchronized void onPlayerConnect(PlayerEvent.PlayerLoggedInEvent event){
        players.put(event.getPlayer().getScoreboardName(),
                event.getPlayer().level.dimension().location().toString());

        playerList = event.getEntityLiving().getServer().getPlayerList().getPlayers();
        updatePlayerList();

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static synchronized void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event){
        players.remove(event.getPlayer().getScoreboardName());
        playerList = event.getEntityLiving().getServer().getPlayerList().getPlayers();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static synchronized void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        players.put(event.getPlayer().getScoreboardName(),
                event.getPlayer().level.dimension().location().toString());

        updatePlayerList();
    }

    private static void updatePlayerList() {
        playerList.forEach((p) -> p.refreshTabListName());
    }

    /**
     * A small function for changing an input string to title case (E.G. hello world -> Hello World)
     * @param text The string to make title case.
     * @return String in title case
     */
    private static String makeTitleCase(String text) {
        text = text.split(":")[1];
        text = text.replace("_", " ");

        if (text.length() <= 2 || (text.startsWith(" ") || text.endsWith(" "))){
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


        // Get player dimension from the 'players' map
        String dimension = makeTitleCase(players.get(event.getPlayer().getScoreboardName()));

        format = format.replace("%d", dimension);
        if (!per_dim_colors) {
            format = format.replace("%c", color.value);
        } else {
            format = format.replace("%c", switch(players.get(event.getPlayer().getScoreboardName())){
                case "minecraft:overworld" -> Config.FontColor.GREEN.value;
                case "minecraft:the_nether" -> Config.FontColor.RED.value;
                case "minecraft:the_end" -> Config.FontColor.DARK_PURPLE.value;
                default -> Config.FontColor.DARK_BLUE.value;
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

}
