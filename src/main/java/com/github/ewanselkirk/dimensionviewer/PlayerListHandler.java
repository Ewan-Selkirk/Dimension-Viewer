package com.github.ewanselkirk.dimensionviewer;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

@Mod.EventBusSubscriber(modid = DimensionViewer.MODID, value = Dist.DEDICATED_SERVER)
public class PlayerListHandler {

    private static Map<String, String> players = new HashMap<>();
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static synchronized void onPlayerDimensionChange(final PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getPlayer();

        players.put(player.getScoreboardName(), event.getTo().location().toString());

        event.getEntityLiving().getServer().getPlayerList().getPlayer(event.getPlayer().getUUID())
                .refreshTabListName();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static synchronized void changeUserDisplayName(PlayerEvent.TabListNameFormat event){
        try {
            String dimension = makeTitleCase(players.get(event.getPlayer().getScoreboardName()));
            event.setDisplayName(new TextComponent(event.getPlayer().getScoreboardName() + "\u00A72" +
                    " <" + dimension + ">"));
        } catch (NullPointerException exception) {
            DimensionViewer.LOGGER.log(Level.SEVERE, exception.getMessage());
        }

    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static synchronized void onPlayerConnect(PlayerEvent.PlayerLoggedInEvent event){
        players.put(event.getPlayer().getScoreboardName(),
                event.getPlayer().level.dimension().location().toString());

        event.getEntityLiving().getServer().getPlayerList().getPlayer(event.getPlayer().getUUID()).refreshTabListName();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static synchronized void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event){
        players.remove(event.getPlayer().getScoreboardName());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static synchronized void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        players.put(event.getPlayer().getScoreboardName(),
                event.getPlayer().level.dimension().location().toString());

        event.getEntityLiving().getServer().getPlayerList().getPlayer(event.getPlayer().getUUID())
                .refreshTabListName();
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

}
