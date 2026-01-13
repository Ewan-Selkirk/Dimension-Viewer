package dev.stick_stack.dimensionviewer;

import java.util.regex.Pattern;

public class PlayerListHandlerNeoForge extends PlayerListHandler {

    @Override
    public String checkForAliases(String dimensionResourceLocation) {
        if (ConfigNeoForge.ENABLE_ALIASES.get()) {
            for (String alias : ConfigNeoForge.DIM_ALIASES.get()) {
                Pattern AliasAsPattern = Pattern.compile(alias.split(" ")[0]);

                if (AliasAsPattern.matcher(dimensionResourceLocation).find()) {
                    return alias.split(" ", 2)[1];
                }
            }
        }

        Pattern userIdPattern = Pattern.compile("[-_]?\\w{8}([-_]\\w{4}){3}[-_]\\w{12}/?");
        return CommonUtils.dimensionToString(userIdPattern.matcher(dimensionResourceLocation).replaceAll(""));
    }

}
