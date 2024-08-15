package dev.stick_stack.dimensionviewer;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class PlayerListHandler {

    public static final List<Player> playerList = new ArrayList<>();

    public MutableComponent makeDimensionComponent(Player player, String format) {
        ResourceLocation dimension = player.level().dimension().location();

        return extractTokensFromFormat(format, dimension);
    }

    private MutableComponent extractTokensFromFormat(String format, ResourceLocation dimension) {
        // Check the list format for tokens, then remove any tokens from the string
        Style style = checkTokens(null, format);
        format = replaceTokens(format);

        // Replace tokens in the aliased dimension name
        String aliasedDim = checkForAliases(dimension.toString());
        style = checkTokens(style, aliasedDim);
        aliasedDim = replaceTokens(aliasedDim);

        // The alias can use the `%d` token, allowing the original name to be used
        aliasedDim = aliasedDim.replace("%d", CommonUtils.ToTitleCase(
                CommonUtils.splitResourceLocation(dimension, 1)
        ));

        // Finally, replace the dimension placeholder with the actual dimension name.
        format = format.replace("%d", aliasedDim);
        return MutableComponent.create(new PlainTextContents.LiteralContents(format)).withStyle(style);
    }


    /**
     * Checks a String for any valid styling tokens.
     * @param inStyle null for a new Style or an existing Style to append to.
     * @param inString The String to check for tokens in
     * @return A Style with the appropriate settings applied
     */
    private Style checkTokens(@Nullable Style inStyle, String inString) {
        inStyle = inStyle == null ? Style.EMPTY : inStyle;

        boolean useItalic = inString.contains("%i");
        boolean useBold = inString.contains("%b");
        boolean useUnderline = inString.contains("%u");
        boolean useStrikethrough = inString.contains("%s");
        boolean useObfuscate = inString.contains("%o");

        inStyle = inStyle
                .withItalic(inStyle.isItalic() || useItalic)
                .withBold(inStyle.isBold() || useBold)
                .withUnderlined(inStyle.isUnderlined() || useUnderline)
                .withStrikethrough(inStyle.isStrikethrough() || useStrikethrough)
                .withObfuscated(inStyle.isObfuscated() || useObfuscate);

        return inStyle;
    }

    /**
     * Replace all valid styling tokens in a string
     * @param inString A String with tokens to be removed
     * @return inString with all tokens removed
     */
    private String replaceTokens(String inString) {
        // Remove all tokens (anything proceeded with a %, but ignore the `%d` token)
        for (String token : new String[] {"%i", "%b", "%u", "%s", "%o"}) {
            inString = inString.replace(token, "");
        }

        // Remove any remaining invalid tokens
        inString = inString.replaceAll("%[^%d].*?", "");

        // Replace the escaped token
        inString = inString.replace("%%", "%");

        return inString;
    }

    public abstract String checkForAliases(String dimension);

}
