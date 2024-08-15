package dev.stick_stack.dimensionviewer;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.players.PlayerList;

import java.util.EnumSet;

public class FabricUtils {

    /**
     * Broadcast a packet to all players telling them to update their display names.
     * Will refresh the tab list menu
     * @param players The list of players to broadcast to
     */
    public static void refreshDisplayNames(PlayerList players) {
        // Holy shit I actually figured out how to update the name...
        // I spent DAYS trying to figure this out...
        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME);
        players.broadcastAll(new ClientboundPlayerInfoUpdatePacket(actions, players.getPlayers()));
    }

}
