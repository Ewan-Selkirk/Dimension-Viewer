package com.github.ewan_selkirk.dimensionviewer;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class Config {

    public enum FontColor {
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
    }

    public static ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec CONFIG;

    public static ForgeConfigSpec.ConfigValue<String> LIST_FORMAT;

    public static ForgeConfigSpec.EnumValue<FontColor> FONT_COLOR;

    public static ForgeConfigSpec.EnumValue<FontColor> OVERWORLD_COLOR;
    public static ForgeConfigSpec.EnumValue<FontColor> NETHER_COLOR;
    public static ForgeConfigSpec.EnumValue<FontColor> END_COLOR;

    public static ForgeConfigSpec.BooleanValue PER_DIM_COLOR;

    public static ForgeConfigSpec.BooleanValue DIM_IN_CHAT_NAME;

    public static ForgeConfigSpec.BooleanValue CHAT_DIM_HOVER;

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> MODDED_DIMS;
    private static List<String> moddedDimensionList = new ArrayList<>();

    static {
        BUILDER.comment("Customization Settings").push("customization");

        CategoryCustomization();

        BUILDER.pop();

        CONFIG = BUILDER.build();
    }

    private static void CategoryCustomization() {
        LIST_FORMAT = BUILDER.comment("Format that will be used to display the dimension in the tab list with the use of tokens:",
                        "    %d - Dimension Name", "    %c - Color value (default or dimension-based)", "    %i - Italic font",
                        "    %b - Bold font", "    %u - Underline font", "    %o - Obfuscated font",
                        "    %s - Strikethrough font", "    %r - Font reset")
                .define("listFormat", " %c%i<%d>");
        FONT_COLOR = BUILDER.comment("The color to use for the dimension font if perDimColorPath is false.",
                        "(In the event of a modded dimension being entered, this color will be used as a fallback)")
                .defineEnum("fontColor", FontColor.DARK_AQUA);
        PER_DIM_COLOR = BUILDER.comment("Should each dimension have its own color?")
                .define("perDimColorPath", true);

        PerDimensionCustomization();

        ChatCustomization();

        ModdedDimensionCustomization();
    }

    private static void PerDimensionCustomization() {
        BUILDER.comment("Per-Dimension Customization").push("dimension");

        OVERWORLD_COLOR = BUILDER.comment("Color to use for the Overworld")
                .defineEnum("overworldColor", FontColor.GREEN);
        NETHER_COLOR = BUILDER.comment("Color to use for the Nether")
                .defineEnum("netherColor", FontColor.DARK_RED);
        END_COLOR = BUILDER.comment("Color to use for the End")
                .defineEnum("endColor", FontColor.DARK_PURPLE);

        BUILDER.pop();
    }

    private static void ChatCustomization() {
        BUILDER.comment("Chat-related Customization").push("chat");

        DIM_IN_CHAT_NAME = BUILDER.comment("Should a users' current dimension be added to chat messages?")
                .define("dimInChatName", true);

        CHAT_DIM_HOVER = BUILDER.comment("Add a hover effect in chat that will display which mod added the dimension",
                        "Requires 'dimInChatName' to be set to true")
                .define("chatDimHover", true);

        BUILDER.pop();
    }

    private static void ModdedDimensionCustomization() {
        BUILDER.comment("Modded Dimension Customization").push("modded");

        MODDED_DIMS = BUILDER.comment("A list of modded dimension resource IDs and a color in the format of \"modid:dim_id color\"" +
                        "\nFor example, Twilight Forest in Gold would be \"twilightforest:twilight_forest GOLD\"" +
                        "\nWill throw an exception if the color is not valid" +
                        "\nAllowed Values: DARK_RED, RED, GOLD, YELLOW, DARK_GREEN, GREEN, AQUA, DARK_AQUA, DARK_BLUE, BLUE, LIGHT_PURPLE, DARK_PURPLE, WHITE, GRAY, DARK_GRAY, BLACK")
                .defineListAllowEmpty(
                        List.of("modded_dimension_ids"),
                        () -> moddedDimensionList,
                        (item) -> (item instanceof String i && i.matches("([a-z_]+:[a-z_]+ [A-Z_]+)")
                                && !i.isEmpty() && FontColor.valueOf(i.split(" ")[1]) instanceof FontColor)
                );

        BUILDER.pop();
    }

}
