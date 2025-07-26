package dev.stick_stack.dimensionviewer;

public class PlayerListHandlerForge extends PlayerListHandler {

    @Override
    public String checkForAliases(String dimensionResourceLocation) {
        if (ConfigForge.ENABLE_ALIASES.get()) {
            for (String alias : ConfigForge.DIM_ALIASES.get()) {
                if (alias.split(" ")[0].equals(dimensionResourceLocation)) {
                    return alias.split(" ", 2)[1];
                }
            }
        }

        return CommonUtils.dimensionToString(dimensionResourceLocation);
    }

}
