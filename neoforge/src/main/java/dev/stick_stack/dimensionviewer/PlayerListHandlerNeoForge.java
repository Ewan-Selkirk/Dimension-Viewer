package dev.stick_stack.dimensionviewer;

public class PlayerListHandlerNeoForge extends PlayerListHandler {

    @Override
    public String checkForAliases(String dimensionResourceLocation) {
        if (ConfigNeoForge.ENABLE_ALIASES.get()) {
            for (String alias : ConfigNeoForge.DIM_ALIASES.get()) {
                if (alias.split(" ")[0].equals(dimensionResourceLocation)) {
                    return alias.split(" ", 2)[1];
                }
            }
        }

        return CommonUtils.dimensionToString(dimensionResourceLocation);
    }

}
