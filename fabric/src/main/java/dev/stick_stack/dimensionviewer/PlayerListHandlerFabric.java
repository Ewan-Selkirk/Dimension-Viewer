package dev.stick_stack.dimensionviewer;

import java.util.regex.Pattern;

public class PlayerListHandlerFabric extends PlayerListHandler {

    @Override
    public String checkForAliases(String dimension) {
        if (ConfigFabric.get().ENABLE_ALIASES) {
            for (String alias : ConfigFabric.get().DIM_ALIASES) {
                Pattern AliasAsPattern = Pattern.compile(alias.split(" ")[0]);

                if (AliasAsPattern.matcher(dimension).find()) {
                    return alias.split(" ", 2)[1];
                }
            }
        }

        Pattern userIdPattern = Pattern.compile("[-_]?\\w{8}([-_]\\w{4}){3}[-_]\\w{12}/?");
        return CommonUtils.dimensionToString(userIdPattern.matcher(dimension).replaceAll(""));
    }
    
}
