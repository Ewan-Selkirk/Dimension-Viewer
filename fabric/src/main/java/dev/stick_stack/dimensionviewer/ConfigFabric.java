package dev.stick_stack.dimensionviewer;

import org.spongepowered.include.com.google.gson.Gson;
import org.spongepowered.include.com.google.gson.GsonBuilder;
import org.spongepowered.include.com.google.gson.annotations.Expose;
import org.spongepowered.include.com.google.gson.annotations.SerializedName;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigFabric {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static ConfigFabric INSTANCE;
    private static boolean isFileWatcherRunning = false;

    @Expose
    @SerializedName("listFormat")
    public String LIST_FORMAT = "%i<%d>";

    @Expose
    @SerializedName("dimensionPosition")
    public CommonUtils.DimensionPosition DIM_POSITION = CommonUtils.DimensionPosition.APPEND;

    @Expose
    @SerializedName("defaultColor")
    public String DEFAULT_COLOR = "DARK_AQUA";

    @Expose
    @SerializedName("overworldColor")
    public String OVERWORLD_COLOR = "DARK_GREEN";

    @Expose
    @SerializedName("netherColor")
    public String NETHER_COLOR = "DARK_RED";

    @Expose
    @SerializedName("endColor")
    public String END_COLOR = "DARK_PURPLE";

    @Expose
    @SerializedName("perDimColor")
    public boolean PER_DIM_COLOR = true;

    @Expose
    @SerializedName("dimInChatName")
    public boolean DIM_IN_CHAT_NAME = true;

    @Expose
    @SerializedName("chatDimHover")
    public boolean CHAT_DIM_HOVER = true;

    @Expose
    @SerializedName("enableAliases")
    public boolean ENABLE_ALIASES = true;

    @Expose
    @SerializedName("moddedDimensions")
    public List<String> MODDED_DIMS = new ArrayList<>();

    @Expose
    @SerializedName("dimensionAliases")
    public List<String> DIM_ALIASES = new ArrayList<>();

    @Expose
    @SerializedName("customColors")
    public List<String> CUSTOM_COLORS = new ArrayList<>();

    public static ConfigFabric get() {
        if (INSTANCE == null) {
            loadConfig();
        }

        return INSTANCE;
    }

    @Override
    public String toString() {
        return "List Format: %s\n".formatted(LIST_FORMAT) +
                "Dimension Position: %s\n".formatted(DIM_POSITION.toString()) +
                "Default Color: %s\n".formatted(DEFAULT_COLOR) +
                "Overworld Color: %s\n".formatted(OVERWORLD_COLOR) +
                "Nether Color: %s\n".formatted(NETHER_COLOR) +
                "End Color: %s\n".formatted(END_COLOR) +
                "Per Dimension Colors: %s\n".formatted(PER_DIM_COLOR) +
                "Dimension in chat name: %s\n".formatted(DIM_IN_CHAT_NAME) +
                "Chat Hover: %s\n".formatted(CHAT_DIM_HOVER) +
                "Enable Aliases: %s\n".formatted(ENABLE_ALIASES) +
                "Modded Dimensions: %s\n".formatted(MODDED_DIMS.toString()) +
                "Dimension Aliases: %s\n".formatted(DIM_ALIASES.toString()) +
                "Custom Colors: %s".formatted(CUSTOM_COLORS.toString());
    }

    /**
     * Load in config options from disk, or create and save a new config file if it doesn't exist.
     * Starts a thread to check for config changes after a valid instance is made
     */
    public static void loadConfig() {
        File file = getConfigFile();
        try (FileReader reader = new FileReader(file)) {
            INSTANCE = GSON.fromJson(reader, ConfigFabric.class);
        } catch (IOException exception) {
            // Create a config from the default options
            Constants.LOG.warn("Failed to load Dimension Viewer config file. Regenerating...");
            INSTANCE = new ConfigFabric();
            saveConfig();
        } finally {

            if (!isFileWatcherRunning) {
                // Start file watcher
                startFileWatcher();
                isFileWatcherRunning = true;
            }
        }
    }

    /**
     * Write the current configuration values to the disk.
     * Creates the config folder if it does not exist
     */
    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(getConfigFile())) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException exception) {
            Constants.LOG.error(exception.getMessage());
        }
    }

    private static File getConfigFile() {
        File file = new File("config", Constants.MOD_ID + ".json");

        // Create the config folder if it doesn't already exist
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }

        return file;
    }

    private static void startFileWatcher() {
        if (isFileWatcherRunning) return;

        Thread thread = new Thread(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                Path configDir = getConfigFile().getParentFile().toPath();
                configDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    WatchKey key = watchService.take();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Path changed = (Path) event.context();
                            if (changed.toString().endsWith(Constants.MOD_ID + ".json")) {
                                Constants.LOG.info("Dimension Viewer config file changed! Updating player info...");
                                loadConfig();

                                // Uncomment the line below to print the values of the new config after reloading
                                //Constants.LOG.info(INSTANCE.toString());

                                if (!PlayerListHandler.playerList.isEmpty()) {
                                    FabricUtils.refreshDisplayNames(
                                            PlayerListHandler.playerList.get(0).getServer().getPlayerList()
                                    );
                                } else {
                                    Constants.LOG.info("Skipping display name refresh as no players are detected on the server");
                                }

                                break;
                            }
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            } catch (IOException | InterruptedException exception) {
                Constants.LOG.error(exception.getMessage());
            }
        });

        thread.setDaemon(true); // Ensure the thread doesn't block JVM shutdown
        thread.start();
    }


    // GSON seems to just accept anything without causing issues which is both a positive and a negative.
    // Will leave commented for now as it works without it
    /*private static void validateConfiguration(ConfigFabric config) throws InvalidConfigException {
        if (!config.LIST_FORMAT.contains("%d")) throw new InvalidConfigException();

        for (String dim : config.MODDED_DIMS) {
            if (!dim.matches(modidRegex + " ([A-Z_]+)")) throw new InvalidConfigException();
        }

        for (String alias : config.DIM_ALIASES) {
            if (!alias.matches(modidRegex + " (.*)")) throw new InvalidConfigException();
        }

        for (String color : config.CUSTOM_COLORS) {
            if (!color.matches("[A-Z_]+ (#(?:[0-9a-fA-F]{3}){1,2}|(?:[rh][0-9]{1,3} [gs][0-9]{1,3} [bv][0-9]{1,3}[ ]?))"))
                throw new InvalidConfigException();
        }

    }*/

}
