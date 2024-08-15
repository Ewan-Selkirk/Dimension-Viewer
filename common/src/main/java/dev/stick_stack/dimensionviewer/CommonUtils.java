package dev.stick_stack.dimensionviewer;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class CommonUtils {

    public static enum DimensionPosition {
        PREPEND,
        APPEND
    }

    /**
     * A small function for changing an input string to title case (E.G. hello world -> Hello World)
     *
     * @param inputString The string to make title case.
     * @return String in title case
     */
    public static String ToTitleCase(String inputString) {
        inputString = inputString.replace("_", " ");

        if (inputString.length() <= 2 || (inputString.startsWith(" ") || inputString.endsWith(" "))) {
            inputString = inputString.toUpperCase(Locale.ROOT);
        } else {
            String[] splitString = inputString.split(" ");
            StringBuilder builder = new StringBuilder();
            int count = 0;

            for (var sub : splitString) {
                splitString[count] = (Character.toUpperCase(sub.charAt(0)) + sub.substring(1));
                builder.append(splitString[count]);

                if (count != splitString.length - 1) builder.append(" ");
                count++;
            }

            inputString = builder.toString();
        }

        return inputString;
    }

    /**
     *
     */
    public static String splitResourceLocation(ResourceLocation key, int pos) {
        String txt = key.toString();

//        return txt.split(":")[pos];
        return splitResourceLocation(txt, pos);
    }

    public static String splitResourceLocation(String key, int pos) {
        return key.split(":")[pos];
    }

    // Stolen from StackOverflow
    // https://stackoverflow.com/a/4801433
    public static int rgbToInt(int r, int g, int b) {
        return ((r & 0x0ff) << 16) | ((g & 0x0ff) << 8) | (b & 0x0ff);
    }

    // Based on this method:
    // https://www.baeldung.com/java-convert-hex-to-rgb
    public static int hexToInt(String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);

        return rgbToInt(r, g, b);
    }
}
