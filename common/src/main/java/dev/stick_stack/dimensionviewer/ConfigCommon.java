package dev.stick_stack.dimensionviewer;

import com.google.common.base.Supplier;
import net.minecraft.ChatFormatting;

import java.util.List;

public class ConfigCommon implements IConfig {

    protected static IConfig INSTANCE;

    public ConfigCommon() {
//        loadConfig();
        Constants.LOG.info("Config initialized! {}", INSTANCE);

        INSTANCE = this;
    }

    /*@Override
    public IConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = loadConfig();
        }

        return INSTANCE;
    }*/

    @Override
    public void saveConfig() {

    }

    @Override
    public IConfig loadConfig() {
        Constants.LOG.error("PLEASE OVERRIDE THE LOAD CONFIG IMPLEMENTATION THANKS!");
        return null;
    }

    /*public enum FontColor {
        DARK_RED("§4"),
        RED("§c"),
        GOLD("§6"),
        YELLOW("§e"),
        DARK_GREEN("§2"),
        GREEN("§a"),
        AQUA("§b"),
        DARK_AQUA("§3"),
        DARK_BLUE("§1"),
        BLUE("§9"),
        LIGHT_PURPLE("§d"),
        DARK_PURPLE("§5"),
        WHITE("§f"),
        GRAY("§7"),
        DARK_GRAY("§8"),
        BLACK("§0");

        public final String value;

        FontColor(String value) {
            this.value = value;
        }

        public static boolean contains(String value) {
            for (FontColor color : FontColor.values()) {
                if (color.name().equals(value)) {
                    return true;
                }
            }

            return false;
        }
    }*/

    public static Supplier<String> LIST_FORMAT = () -> "%p %c<%d>%r";

    public static Supplier<String> DEFAULT_COLOR = ChatFormatting.GOLD::toString;
    public static Supplier<String> OVERWORLD_COLOR = ChatFormatting.DARK_GREEN::toString;
    public static Supplier<String> NETHER_COLOR = ChatFormatting.DARK_RED::toString;
    public static Supplier<String> END_COLOR = ChatFormatting.DARK_PURPLE::toString;

    public static Supplier<Boolean> PER_DIM_COLOR = () -> true;
    public static Supplier<Boolean> DIM_IN_CHAT_NAME = () -> true;
    public static Supplier<Boolean> CHAT_DIM_HOVER = () -> true;
    public static Supplier<Boolean> ENABLE_ALIASES = () -> true;

    public static Supplier<List<? extends String>> MODDED_DIMS;
    public static Supplier<List<? extends String>> DIM_ALIASES;

}
