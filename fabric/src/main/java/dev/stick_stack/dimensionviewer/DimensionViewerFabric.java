package dev.stick_stack.dimensionviewer;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.players.PlayerList;

import java.util.EnumSet;

public class DimensionViewerFabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register((server -> ConfigFabric.get()));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                PlayerListHandler.playerList.add(handler.getPlayer())
        );

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                PlayerListHandler.playerList.remove(handler.getPlayer())
        );

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, isAlive) -> {
            PlayerList playerList = newPlayer.getServer().getPlayerList();

            // Refresh every players name on respawn
            EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME);
            playerList.broadcastAll(new ClientboundPlayerInfoUpdatePacket(actions, playerList.getPlayers()));
        });

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> CustomCommands.RegisterCommands(dispatcher)));
    }
}
