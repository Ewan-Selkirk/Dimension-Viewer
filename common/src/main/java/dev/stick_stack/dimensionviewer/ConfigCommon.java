package dev.stick_stack.dimensionviewer;

import net.minecraft.ChatFormatting;

import java.util.Locale;

public class ConfigCommon {

    public static final String modidRegex = "([a-z_]+:[a-z0-9_/-]+)";
    public static final String allowedColorsComment = "\nAllowed Values: DARK_RED, RED, GOLD, YELLOW, DARK_GREEN, GREEN, " +
            "AQUA, DARK_AQUA, DARK_BLUE, BLUE, LIGHT_PURPLE, DARK_PURPLE, WHITE, GRAY, DARK_GRAY, BLACK" +
            "\nOr any custom colours defined in `customColors`";

    public static String DEFAULT_LIST_FORMAT = "%i<%d>";

    public static String DEFAULT_COLOR = ChatFormatting.GOLD.getName().toUpperCase(Locale.ROOT);
    public static String OVERWORLD_COLOR = ChatFormatting.DARK_GREEN.getName().toUpperCase(Locale.ROOT);
    public static String NETHER_COLOR = ChatFormatting.DARK_RED.getName().toUpperCase(Locale.ROOT);
    public static String END_COLOR = ChatFormatting.DARK_PURPLE.getName().toUpperCase(Locale.ROOT);

    public static boolean PER_DIM_COLOR = true;
    public static boolean DIM_IN_CHAT_NAME = true;
    public static boolean CHAT_DIM_HOVER = true;
    public static boolean ENABLE_ALIASES = true;

}
