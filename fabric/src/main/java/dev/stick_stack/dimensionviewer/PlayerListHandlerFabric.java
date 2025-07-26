package dev.stick_stack.dimensionviewer;

public class PlayerListHandlerFabric extends PlayerListHandler {

    @Override
    public String checkForAliases(String dimension) {
        if (ConfigFabric.get().ENABLE_ALIASES) {
            for (String alias : ConfigFabric.get().DIM_ALIASES) {
                if (alias.split(" ")[0].equals(dimension)) {
                    return alias.split(" ", 2)[1];
                }
            }
        }

        return CommonUtils.dimensionToString(dimension);
    }
    
}
