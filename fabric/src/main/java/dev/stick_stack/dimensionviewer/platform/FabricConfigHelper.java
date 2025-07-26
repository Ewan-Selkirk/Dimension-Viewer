package dev.stick_stack.dimensionviewer.platform;

import dev.stick_stack.dimensionviewer.CommonUtils;
import dev.stick_stack.dimensionviewer.ConfigCommon;
import dev.stick_stack.dimensionviewer.ConfigFabric;
import dev.stick_stack.dimensionviewer.FabricUtils;
import dev.stick_stack.dimensionviewer.platform.services.IConfigHelper;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FabricConfigHelper implements IConfigHelper {

    @Override
    public String BaseDefaultColor() {
        return ConfigCommon.DEFAULT_COLOR;
    }

    @Override
    public String BaseOverworldColor() {
        return ConfigCommon.OVERWORLD_COLOR;
    }

    @Override
    public String BaseNetherColor() {
        return ConfigCommon.NETHER_COLOR;
    }

    @Override
    public String BaseEndColor() {
        return ConfigCommon.END_COLOR;
    }

    @Override
    public String BaseListFormat() {
        return ConfigCommon.DEFAULT_LIST_FORMAT;
    }

    @Override
    public String DefaultColor() {
        return ConfigFabric.get().DEFAULT_COLOR;
    }

    @Override
    public String OverworldColor() {
        return ConfigFabric.get().OVERWORLD_COLOR;
    }

    @Override
    public String NetherColor() {
        return ConfigFabric.get().NETHER_COLOR;
    }

    @Override
    public String EndColor() {
        return ConfigFabric.get().END_COLOR;
    }

    @Override
    public String ListFormat() {
        return ConfigFabric.get().LIST_FORMAT;
    }

    @Override
    public List<String> GetAllCustomColors() {
        return ConfigFabric.get().CUSTOM_COLORS;
    }

    @Override
    public void AddCustomColor(String name, String color) {
        ConfigFabric.get().CUSTOM_COLORS.add("%s %s".formatted(name, color));
        ConfigFabric.saveConfig();
    }

    @Override
    public boolean RemoveCustomColor(String name) {
        int i = 0;
        for (var color : ConfigFabric.get().CUSTOM_COLORS) {
            if (color.split(" ")[0].equals(name)) {
                ConfigFabric.get().CUSTOM_COLORS.remove(i);
                ConfigFabric.saveConfig();
                return true;
            }
            i++;
        }

        return false;
    }

    @Override
    @Nullable
    public String GetAlias(String dimId) {
        for (var dim : ConfigFabric.get().DIM_ALIASES) {
            var values = dim.split(" ", 2);

            if (dimId.equals(values[0])) {
                return values[1];
            }
        }

        return null;
    }

    @Override
    @Nullable
    public String GetCustomColor(String dimId) {
         return switch (dimId) {
            case "minecraft:overworld" -> OverworldColor();
            case "minecraft:the_nether" -> NetherColor();
            case "minecraft:the_end" -> EndColor();
            default -> {
                for (var dim : ConfigFabric.get().MODDED_DIMS) {
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
        int i = 0;
        for (String dim : ConfigFabric.get().DIM_ALIASES) {
            var values = dim.split(" ", 2);

            if (dimId.equals(values[0])) {
                ConfigFabric.get().DIM_ALIASES.set(i, "%s %s".formatted(dimId, alias));
                ConfigFabric.saveConfig();
                return;
            }

            i++;
        }

        ConfigFabric.get().DIM_ALIASES.add("%s %s".formatted(dimId, alias));
        ConfigFabric.saveConfig();
    }

    @Override
    public void SetColor(String dimId, String color) {
        switch (dimId) {
            case "minecraft:overworld" -> {
                ConfigFabric.get().OVERWORLD_COLOR = color;
                ConfigFabric.saveConfig();
            }
            case "minecraft:the_nether" -> {
                ConfigFabric.get().NETHER_COLOR = color;
                ConfigFabric.saveConfig();
            }
            case "minecraft:the_end" -> {
                ConfigFabric.get().END_COLOR = color;
                ConfigFabric.saveConfig();
            }
            default -> {
                int i = 0;
                for (String dim : ConfigFabric.get().MODDED_DIMS) {
                    var values = dim.split(" ", 2);

                    if (values[0].equals(dim)) {
                        ConfigFabric.get().MODDED_DIMS.set(i, "%s %s".formatted(dimId, color));
                        ConfigFabric.saveConfig();
                        return;
                    }

                    i++;
                }

                ConfigFabric.get().MODDED_DIMS.add("%s %s".formatted(dimId, color));
                ConfigFabric.saveConfig();
            }
        }
    }

    @Override
    public void SetBoolSetting(String setting, boolean value) {
        switch (setting) {
            case "perDimColor" -> ConfigFabric.get().PER_DIM_COLOR = value;
            case "dimInChatName" -> ConfigFabric.get().DIM_IN_CHAT_NAME = value;
            case "chatDimHover" -> ConfigFabric.get().CHAT_DIM_HOVER = value;
            case "enableAliases" -> ConfigFabric.get().ENABLE_ALIASES = value;
        }

        ConfigFabric.saveConfig();
    }

    @Override
    public void ResetAlias(String dimId) {
        ConfigFabric.get().DIM_ALIASES.removeIf(a -> dimId.equals(a.split(" ", 2)[0]));
        ConfigFabric.saveConfig();
    }

    @Override
    public void ResetColor(String dimId) {
        switch (dimId) {
            case "minecraft:overworld" -> ConfigFabric.get().OVERWORLD_COLOR = ConfigCommon.OVERWORLD_COLOR;
            case "minecraft:the_nether" -> ConfigFabric.get().NETHER_COLOR = ConfigCommon.NETHER_COLOR;
            case "minecraft:the_end" -> ConfigFabric.get().END_COLOR = ConfigCommon.END_COLOR;
            default -> {
                ConfigFabric.get().MODDED_DIMS.removeIf(a -> dimId.equals(a.split(" ", 2)[0]));
            }
        }

        ConfigFabric.saveConfig();
    }

    @Override
    public void SetFormat(String format) {
        ConfigFabric.get().LIST_FORMAT = format;
        ConfigFabric.saveConfig();
    }

    @Override
    public void SetPlacement(CommonUtils.DimensionPosition position) {
        ConfigFabric.get().DIM_POSITION = position;
        ConfigFabric.saveConfig();
    }

    @Override
    public void RefreshPlayerData(PlayerList players) {
        FabricUtils.refreshDisplayNames(players);
    }

    @Override
    public boolean HasAlias(String dimId) {
        for (var d : ConfigFabric.get().DIM_ALIASES) {
            if (d.split(" ", 2)[0].equals(dimId)) {
                return true;
            }
        }

        return false;
    }
}