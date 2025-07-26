package dev.stick_stack.dimensionviewer.platform;

import dev.stick_stack.dimensionviewer.CommonUtils;
import dev.stick_stack.dimensionviewer.ConfigCommon;
import dev.stick_stack.dimensionviewer.ConfigNeoForge;
import dev.stick_stack.dimensionviewer.platform.services.IConfigHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NeoForgeConfigHelper implements IConfigHelper {
    
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
        return ConfigCommon.DEFAULT_COLOR;
    }

    @Override
    public String OverworldColor() {
        return ConfigNeoForge.OVERWORLD_COLOR.get();
    }

    @Override
    public String NetherColor() {
        return ConfigNeoForge.NETHER_COLOR.get();
    }

    @Override
    public String EndColor() {
        return ConfigNeoForge.END_COLOR.get();
    }

    @Override
    public String ListFormat() {
        return ConfigNeoForge.LIST_FORMAT.get();
    }

    @Override
    public List<String> GetAllCustomColors() {
        return (List<String>) ConfigNeoForge.CUSTOM_COLORS.get();
    }

    @Override
    public void AddCustomColor(String name, String color) {
        List<String> colors = (List<String>) ConfigNeoForge.CUSTOM_COLORS.get();

        colors.add("%s %s".formatted(name, color));
        ConfigNeoForge.CUSTOM_COLORS.set(colors);
        ConfigNeoForge.CUSTOM_COLORS.save();
    }

    @Override
    public boolean RemoveCustomColor(String name) {
        List<String> colors = (List<String>) ConfigNeoForge.CUSTOM_COLORS.get();

        int i = 0;
        for (String c : colors) {
            if (c.split(" ")[0].equals(name)) {
                colors.remove(i);
                ConfigNeoForge.CUSTOM_COLORS.set(colors);
                ConfigNeoForge.CUSTOM_COLORS.save();
                return true;
            }
            i++;
        }
        return false;
    }

    @Override
    public @Nullable String GetAlias(String dimId) {
        for (var dim : ConfigNeoForge.DIM_ALIASES.get()) {
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
                for (var dim : ConfigNeoForge.MODDED_DIMS.get()) {
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
        List<String> aliases = (List<String>) ConfigNeoForge.DIM_ALIASES.get();

        int i = 0;
        for (String dim : aliases) {
            var values = dim.split(" ", 2);

            if (dimId.equals(values[0])) {
                aliases.set(i, "%s %s".formatted(dimId, alias));
                ConfigNeoForge.DIM_ALIASES.set(aliases);
                ConfigNeoForge.DIM_ALIASES.save();
                return;
            }

            i++;
        }

        aliases.add(i, "%s %s".formatted(dimId, alias));
        ConfigNeoForge.DIM_ALIASES.set(aliases);
        ConfigNeoForge.DIM_ALIASES.save();
    }

    @Override
    public void SetColor(String dimId, String color) {
        List<String> dims = (List<String>) ConfigNeoForge.MODDED_DIMS.get();

        switch (dimId) {
            case "minecraft:overworld" -> {
                ConfigNeoForge.OVERWORLD_COLOR.set(color);
                ConfigNeoForge.OVERWORLD_COLOR.save();
            }
            case "minecraft:the_nether" -> {
                ConfigNeoForge.NETHER_COLOR.set(color);
                ConfigNeoForge.NETHER_COLOR.save();
            }
            case "minecraft:the_end" -> {
                ConfigNeoForge.END_COLOR.set(color);
                ConfigNeoForge.END_COLOR.save();
            }
            default -> {
                int i = 0;
                for (String dim : ConfigNeoForge.MODDED_DIMS.get()) {
                    var values = dim.split(" ", 2);

                    if (values[0].equals(dim)) {
                        dims.add(i, "%s %s".formatted(dimId, color));
                        ConfigNeoForge.MODDED_DIMS.set(dims);
                        ConfigNeoForge.MODDED_DIMS.save();
                        return;
                    }

                    i++;
                }

                dims.add("%s %s".formatted(dimId, color));
                ConfigNeoForge.MODDED_DIMS.set(dims);
                ConfigNeoForge.MODDED_DIMS.save();
            }
        }


    }

    @Override
    public void SetBoolSetting(String setting, boolean value) {
        switch (setting) {
            case "perDimColor" -> {
                ConfigNeoForge.PER_DIM_COLOR.set(value);
                ConfigNeoForge.PER_DIM_COLOR.save();
            }
            case "dimInChatName" -> {
                ConfigNeoForge.DIM_IN_CHAT_NAME.set(value);
                ConfigNeoForge.DIM_IN_CHAT_NAME.save();
            }
            case "chatDimHover" -> {
                ConfigNeoForge.CHAT_DIM_HOVER.set(value);
                ConfigNeoForge.CHAT_DIM_HOVER.save();
            }
            case "enableAliases" -> {
                ConfigNeoForge.ENABLE_ALIASES.set(value);
                ConfigNeoForge.ENABLE_ALIASES.save();
            }
        }
    }

    @Override
    public void ResetAlias(String dimId) {
        List<String> aliases = (List<String>) ConfigNeoForge.DIM_ALIASES.get();

        aliases.removeIf(a -> dimId.equals(a.split(" ", 2)[0]));
        ConfigNeoForge.DIM_ALIASES.set(aliases);
        ConfigNeoForge.DIM_ALIASES.save();
    }

    @Override
    public void ResetColor(String dimId) {
        switch (dimId) {
            case "minecraft:overworld" -> ConfigNeoForge.OVERWORLD_COLOR.set(BaseOverworldColor());
            case "minecraft:the_nether" -> ConfigNeoForge.NETHER_COLOR.set(BaseNetherColor());
            case "minecraft:the_end" -> ConfigNeoForge.END_COLOR.set(BaseEndColor());
            default -> {
                List<String> dims = (List<String>) ConfigNeoForge.MODDED_DIMS.get();
                dims.removeIf(a -> dimId.equals(a.split(" ", 2)[0]));

                ConfigNeoForge.MODDED_DIMS.set(dims);
                ConfigNeoForge.MODDED_DIMS.save();
            }
        }
    }

    @Override
    public void SetFormat(String format) {
        ConfigNeoForge.LIST_FORMAT.set(format);
        ConfigNeoForge.LIST_FORMAT.save();
    }

    @Override
    public void SetPlacement(CommonUtils.DimensionPosition position) {
        ConfigNeoForge.DIM_POSITION.set(position);
        ConfigNeoForge.DIM_POSITION.save();
    }

    @Override
    public void RefreshPlayerData(PlayerList players) {
        players.getPlayers().forEach(ServerPlayer::refreshDisplayName);
        players.getPlayers().forEach(ServerPlayer::refreshTabListName);
    }

    @Override
    public boolean HasAlias(String dimId) {
        for (var dim : ConfigNeoForge.DIM_ALIASES.get()) {
            if (dim.split(" ", 2)[0].equals(dimId)) {
                return true;
            }
        }

        return false;
    }
}
