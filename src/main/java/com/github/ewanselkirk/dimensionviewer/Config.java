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

    public static ForgeConfigSpec.BooleanValue PER_DIM_COLOR;

    static {
        BUILDER.comment("Customization Settings").push("customization");

        CategoryCustomization();

        BUILDER.pop();

        CONFIG = BUILDER.build();
    }

    private static void CategoryCustomization() {
        LIST_FORMAT = BUILDER.comment("Format that will be used to display the dimension in the tab list. %d will insert the dimension and %c will insert a colour")
                .define("listFormat", "%c <%d>");
        FONT_COLOR = BUILDER.comment("The color to use for the dimension font (only valid if perDimColorPath set to false)")
                .defineEnum("fontColor", FontColor.GREEN);
        PER_DIM_COLOR = BUILDER.comment("Should each dimension have its own color?")
                .define("perDimColorPath", false);

        PerDimensionCustomization();
    }

    private static void PerDimensionCustomization() {
        BUILDER.comment("Per-Dimension Customization").push("dimension");

        BUILDER.pop();
    }

}
