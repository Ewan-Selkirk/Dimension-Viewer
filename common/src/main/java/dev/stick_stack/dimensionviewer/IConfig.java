package dev.stick_stack.dimensionviewer;

interface IConfig {

    public IConfig getInstance();

    static void saveConfig() {

    }

    IConfig loadConfig();

}
