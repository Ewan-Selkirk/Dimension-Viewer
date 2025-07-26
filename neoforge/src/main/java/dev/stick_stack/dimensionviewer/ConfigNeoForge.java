package dev.stick_stack.dimensionviewer;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

import static dev.stick_stack.dimensionviewer.ConfigCommon.allowedColorsComment;
import static dev.stick_stack.dimensionviewer.ConfigCommon.modidRegex;

public class ConfigNeoForge {

    private static final List<String> moddedDimensionList = new ArrayList<>();
    private static final List<String> dimensionAliases = new ArrayList<>();
    private static final List<String> customColourList = new ArrayList<>();

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec CONFIG;

    public static ModConfigSpec.ConfigValue<String> LIST_FORMAT;
    public static ModConfigSpec.EnumValue<CommonUtils.DimensionPosition> DIM_POSITION;

    public static ModConfigSpec.ConfigValue<String> DEFAULT_COLOR;
    public static ModConfigSpec.ConfigValue<String> OVERWORLD_COLOR;
    public static ModConfigSpec.ConfigValue<String> NETHER_COLOR;
    public static ModConfigSpec.ConfigValue<String> END_COLOR;

    public static ModConfigSpec.BooleanValue PER_DIM_COLOR;
    public static ModConfigSpec.BooleanValue DIM_IN_CHAT_NAME;
    public static ModConfigSpec.BooleanValue CHAT_DIM_HOVER;
    public static ModConfigSpec.BooleanValue ENABLE_ALIASES;

    public static ModConfigSpec.ConfigValue<List<? extends String>> MODDED_DIMS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> DIM_ALIASES;
    public static ModConfigSpec.ConfigValue<List<? extends String>> CUSTOM_COLORS;

    static {
        BUILDER.comment("Customization Settings").push("customization");

        CategoryCustomization();

        BUILDER.pop();

        CONFIG = BUILDER.build();
    }

    private static void CategoryCustomization() {
        LIST_FORMAT = BUILDER.comment("Format that will be used to display the dimension in the tab list with the use of tokens:",
                        "    %d - Dimension Name*", "    %i - Italic font", "    %b - Bold font",
                        "    %u - Underline font", "    %o - Obfuscated font", "    %s - Strikethrough font" +
                        "\n*Required (well, not technically, but it defeats the purpose without it!)")
                .define("listFormat", ConfigCommon.DEFAULT_LIST_FORMAT);
        DIM_POSITION = BUILDER.comment("Whether the dimension should be placed before or after the player name")
                .defineEnum("dimensionPosition", CommonUtils.DimensionPosition.APPEND);
        DEFAULT_COLOR = BUILDER.comment("The color to use for the dimension font if perDimColorPath is false.",
                        "(In the event of a modded dimension being entered, this color will be used as a fallback)")
                .define("fontColor", ConfigCommon.DEFAULT_COLOR);
        PER_DIM_COLOR = BUILDER.comment("Should each dimension have its own color?")
                .define("perDimColor", ConfigCommon.PER_DIM_COLOR);
        ENABLE_ALIASES = BUILDER.comment("Global toggle for dimension aliases. Requires aliases to be set below.")
                .define("enableAliases", ConfigCommon.ENABLE_ALIASES);

        PerDimensionCustomization();

        ChatCustomization();

        ModdedDimensionCustomization();

        ExtraCustomization();
    }

    private static void PerDimensionCustomization() {
        BUILDER.comment("Per-Dimension Customization").push("dimension");

        OVERWORLD_COLOR = BUILDER.comment("Color to use for the Overworld" +
                        allowedColorsComment)
                .define("overworldColor", ConfigCommon.OVERWORLD_COLOR);
        NETHER_COLOR = BUILDER.comment("Color to use for the Nether" +
                        allowedColorsComment)
                .define("netherColor", ConfigCommon.NETHER_COLOR);
        END_COLOR = BUILDER.comment("Color to use for the End" +
                        allowedColorsComment)
                .define("endColor", ConfigCommon.END_COLOR);

        BUILDER.pop();
    }

    private static void ChatCustomization() {
        BUILDER.comment("Chat-related Customization").push("chat");

        DIM_IN_CHAT_NAME = BUILDER.comment("Should a users' current dimension be added to chat messages?")
                .define("dimInChatName", ConfigCommon.DIM_IN_CHAT_NAME);

        CHAT_DIM_HOVER = BUILDER.comment("Add a hover effect in chat that will display the source of a dimension",
                        "Requires `dimInChatName` to be set to true")
                .define("chatDimHover", ConfigCommon.CHAT_DIM_HOVER);

        BUILDER.pop();
    }

    private static void ModdedDimensionCustomization() {
        BUILDER.comment("Modded Dimension Customization").push("modded");

        MODDED_DIMS = BUILDER.comment("A list of modded dimension resource IDs and a color in the format of \"modid:dim_id color\"" +
                "\nFor example, Twilight Forest in Gold would be \"twilightforest:twilight_forest GOLD\"" +
                "\nWill throw an exception if the color is not valid" +
                allowedColorsComment
        ).defineListAllowEmpty(
                List.of("moddedDimensions"),
                () -> moddedDimensionList,
                (item) -> (item instanceof String i && i.matches(modidRegex + " ([A-Z_]+)")
                )
        );

        DIM_ALIASES = BUILDER.comment("A list of aliases to use instead of the original dimension ID." +
                "\nUses the format 'modid:dim_id New Name'." +
                "\nFor example, to replace 'Overworld' with 'Grasslands' you would use 'minecraft:overworld Grasslands'" +
                "\nAliases support the same tokens as `listFormat`, allowing you to make a specific dimension bold or italic or both!"
        ).defineListAllowEmpty(
                List.of("dimensionAliases"),
                () -> dimensionAliases,
                (item) -> (item instanceof String i && i.matches(modidRegex + " (.*)"))
        );

        BUILDER.pop();
    }

    private static void ExtraCustomization() {
        BUILDER.comment("Extra Customization").push("extra");

        CUSTOM_COLORS = BUILDER.comment("Custom colors can be defined here." +
                "\nUses the format 'COLOR_NAME #HEX' or 'COLOR_NAME r000 g000 b000'" +
                "\nIf a custom color of the same name already exists the server will reject the newest one." +
                "\nThe name must be uppercase and can only contain letters and underscores."
        ).defineListAllowEmpty(
                List.of("customColors"),
                () -> customColourList,
                (item) -> (item instanceof String i
                        && i.matches("[A-Z_]+ (#(?:[0-9a-fA-F]{3}){1,2}|[rh][0-9]{1,3} [gs][0-9]{1,3} [bv][0-9]{1,3}[ ]?)")
//                                && !customColourList.contains(i.split(" ")[0])
                        && customColourList.stream().noneMatch((p) -> p.split(" ")[0].equals(i.split(" ")[0]))
                )
        );
    }
}
