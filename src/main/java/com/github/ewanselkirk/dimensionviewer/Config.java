package com.github.ewanselkirk.dimensionviewer;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Config {

    public enum FontColor {
        DARK_RED("\u00A74"),
        RED("\u00A7c"),
        GOLD("\u00A76"),
        YELLOW("\u00A7e"),
        DARK_GREEN("\u00A72"),
        GREEN("\u00A7a"),
        AQUA("\u00A7b"),
        DARK_AQUA("\u00A73"),
        DARK_BLUE(" \u00A71"),
        BLUE(" \u00A79"),
        LIGHT_PURPLE("\u00A7d"),
        DARK_PURPLE("\u00A75"),
        WHITE("\u00A7f"),
        GRAY("\u00A77"),
        DARK_GRAY("\u00A78"),
        BLACK("\u00A70");

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
                .define("listFormat", "%c <%d>");
        FONT_COLOR = BUILDER.comment("The color to use for the dimension font if perDimColorPath is false.",
                        "(In the event of a modded dimension being entered, this color will be used as a fallback)")
                .defineEnum("fontColor", FontColor.DARK_AQUA);
        PER_DIM_COLOR = BUILDER.comment("Should each dimension have its own color?")
                .define("perDimColorPath", true);

        PerDimensionCustomization();
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

}
