package dev.stick_stack.dimensionviewer.platform;

import dev.stick_stack.dimensionviewer.CommonUtils;
import dev.stick_stack.dimensionviewer.ConfigCommon;
import dev.stick_stack.dimensionviewer.ConfigForge;
import dev.stick_stack.dimensionviewer.platform.services.IConfigHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ForgeConfigHelper implements IConfigHelper {

    @Override
    public String BaseDefaultColor() {
        return ConfigForge.BASE_DEFAULT_COLOR;
    }

    @Override
    public String BaseOverworldColor() {
        return ConfigForge.BASE_OVERWORLD_COLOR;
    }

    @Override
    public String BaseNetherColor() {
        return ConfigForge.BASE_NETHER_COLOR;
    }

    @Override
    public String BaseEndColor() {
        return ConfigForge.BASE_END_COLOR;
    }

    @Override
    public String BaseListFormat() {
        return ConfigForge.BASE_LIST_FORMAT;
    }

    @Override
    public String DefaultColor() {
        return ConfigForge.DEFAULT_COLOR.get();
    }

    @Override
    public String OverworldColor() {
        return ConfigForge.OVERWORLD_COLOR.get();
    }

    @Override
    public String NetherColor() {
        return ConfigForge.NETHER_COLOR.get();
    }

    @Override
    public String EndColor() {
        return ConfigForge.END_COLOR.get();
    }

    @Override
    public String ListFormat() {
        return ConfigForge.LIST_FORMAT.get();
    }

    @Override
    public List<String> GetAllCustomColors() {
        return (List<String>) ConfigForge.CUSTOM_COLORS.get();
    }

    @Override
    public void AddCustomColor(String name, String color) {
        List<String> colors = (List<String>) ConfigForge.CUSTOM_COLORS.get();

        colors.add("%s %s".formatted(name, color));
        ConfigForge.CUSTOM_COLORS.set(colors);
    }

    @Override
    public boolean RemoveCustomColor(String name) {
        List<String> colors = (List<String>) ConfigForge.CUSTOM_COLORS.get();

        int i = 0;
        for (String c : colors) {
            if (c.split(" ")[0].equals(name)) {
                colors.remove(i);
                ConfigForge.CUSTOM_COLORS.set(colors);
                return true;
            }
            i++;
        }
        return false;
    }

    @Override
    public @Nullable String GetAlias(String dimId) {
        for (var dim : ConfigForge.DIM_ALIASES.get()) {
            var values = dim.split(" ", 2);

            if (dimId.equals(values[0])) {
                return values[1];
            }
        }

        return null;
    }

    @Override
    public @Nullable String GetCustomColor(String dimId) {
        return switch (dimId) {
            case "minecraft:overworld" -> OverworldColor();
            case "minecraft:the_nether" -> NetherColor();
            case "minecraft:the_end" -> EndColor();
            default -> {
                for (var dim : ConfigForge.MODDED_DIMS.get()) {
                    var values = dim.split(" ", 2);

                    if (dimId.equals(values[0])) {
                        yield values[1];
                    }
                }

                yield null;
            }
        };
    }

    @Override
    public void SetAlias(String dimId, String alias) {
        List<String> aliases = (List<String>) ConfigForge.DIM_ALIASES.get();

        int i = 0;
        for (String dim : aliases) {
            var values = dim.split(" ", 2);

            if (dimId.equals(values[0])) {
                aliases.set(i, "%s %s".formatted(dimId, alias));
                ConfigForge.DIM_ALIASES.set(aliases);
                return;
            }

            i++;
        }

        aliases.add(i, "%s %s".formatted(dimId, alias));
        ConfigForge.DIM_ALIASES.set(aliases);
    }

    @Override
    public void SetColor(String dimId, String color) {
        List<String> dims = (List<String>) ConfigForge.MODDED_DIMS.get();

        switch (dimId) {
            case "minecraft:overworld" -> {
                ConfigForge.OVERWORLD_COLOR.set(color);
            }
            case "minecraft:the_nether" -> {
                ConfigForge.NETHER_COLOR.set(color);
            }
            case "minecraft:the_end" -> {
                ConfigForge.END_COLOR.set(color);
            }
            default -> {
                int i = 0;
                for (String dim : ConfigForge.MODDED_DIMS.get()) {
                    var values = dim.split(" ", 2);

                    if (dimId.equals(values[0])) {
                        dims.set(i, "%s %s".formatted(dimId, color));
                        ConfigForge.MODDED_DIMS.set(dims);
                        return;
                    }

                    i++;
                }

                dims.add("%s %s".formatted(dimId, color));
                ConfigForge.MODDED_DIMS.set(dims);
            }
        }
    }

    @Override
    public void SetBoolSetting(String setting, boolean value) {
        switch (setting) {
            case "perDimColor" -> ConfigForge.PER_DIM_COLOR.set(value);
            case "dimInChatName" -> ConfigForge.DIM_IN_CHAT_NAME.set(value);
            case "chatDimHover" -> ConfigForge.CHAT_DIM_HOVER.set(value);
            case "enableAliases" -> ConfigForge.ENABLE_ALIASES.set(value);
        }
    }

    @Override
    public void ResetAlias(String dimId) {
        List<String> aliases = (List<String>) ConfigForge.DIM_ALIASES.get();

        aliases.removeIf(a -> dimId.equals(a.split(" ", 2)[0]));
        ConfigForge.DIM_ALIASES.set(aliases);
    }

    @Override
    public void ResetColor(String dimId) {
        switch (dimId) {
            case "minecraft:overworld" -> ConfigForge.OVERWORLD_COLOR.set(ConfigCommon.OVERWORLD_COLOR);
            case "minecraft:the_nether" -> ConfigForge.NETHER_COLOR.set(ConfigCommon.NETHER_COLOR);
            case "minecraft:the_end" -> ConfigForge.END_COLOR.set(ConfigCommon.END_COLOR);
            default -> {
                List<String> dims = (List<String>) ConfigForge.MODDED_DIMS.get();
                dims.removeIf(a -> dimId.equals(a.split(" ", 2)[0]));

                ConfigForge.MODDED_DIMS.set(dims);
            }
        }
    }

    @Override
    public void SetFormat(String format) {
        ConfigForge.LIST_FORMAT.set(format);
    }

    @Override
    public void SetPlacement(CommonUtils.DimensionPosition position) {
        ConfigForge.DIM_POSITION.set(position);
    }

    @Override
    public void RefreshPlayerData(PlayerList players) {
        players.getPlayers().forEach(ServerPlayer::refreshDisplayName);
        players.getPlayers().forEach(ServerPlayer::refreshTabListName);
    }

    @Override
    public boolean HasAlias(String dimId) {
        for (var dim : ConfigForge.DIM_ALIASES.get()) {
            if (dim.split(" ", 2)[0].equals(dimId)) {
                return true;
            }
        }

        return false;
    }
}
