package dev.stick_stack.dimensionviewer;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.include.com.google.gson.Gson;
import org.spongepowered.include.com.google.gson.GsonBuilder;
import org.spongepowered.include.com.google.gson.annotations.Expose;
import org.spongepowered.include.com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigFabric {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static ConfigFabric INSTANCE;
    private static boolean isFileWatcherRunning = false;

    @Expose
    @SerializedName("listFormat")
    public String LIST_FORMAT = ConfigCommon.DEFAULT_LIST_FORMAT;

    @Expose
    @SerializedName("dimensionPosition")
    public CommonUtils.DimensionPosition DIM_POSITION = CommonUtils.DimensionPosition.APPEND;

    @Expose
    @SerializedName("defaultColor")
    public String DEFAULT_COLOR = ConfigCommon.DEFAULT_COLOR;

    @Expose
    @SerializedName("overworldColor")
    public String OVERWORLD_COLOR = ConfigCommon.OVERWORLD_COLOR;

    @Expose
    @SerializedName("netherColor")
    public String NETHER_COLOR = ConfigCommon.NETHER_COLOR;

    @Expose
    @SerializedName("endColor")
    public String END_COLOR = ConfigCommon.END_COLOR;

    @Expose
    @SerializedName("perDimColor")
    public boolean PER_DIM_COLOR = ConfigCommon.PER_DIM_COLOR;

    @Expose
    @SerializedName("dimInChatName")
    public boolean DIM_IN_CHAT_NAME = ConfigCommon.DIM_IN_CHAT_NAME;

    @Expose
    @SerializedName("chatDimHover")
    public boolean CHAT_DIM_HOVER = ConfigCommon.CHAT_DIM_HOVER;

    @Expose
    @SerializedName("enableAliases")
    public boolean ENABLE_ALIASES = ConfigCommon.ENABLE_ALIASES;

    @Expose
    @SerializedName("moddedDimensions")
    public List<String> MODDED_DIMS = new ArrayList<>();

    @Expose
    @SerializedName("dimensionAliases")
    public List<String> DIM_ALIASES = new ArrayList<>();

    @Expose
    @SerializedName("customColors")
    public List<String> CUSTOM_COLORS = new ArrayList<>();

    @NotNull
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
                String checksum = "";
                Path configDir = getConfigFile().getParentFile().toPath();
                configDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    WatchKey key = watchService.take();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }

                        if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            /*try {*/
                                Path changed = (Path) event.context();
                                if (changed.toString().endsWith(Constants.MOD_ID + ".json")) {
                                    Path config = Path.of("config", changed.toString());

                                    assert checksum != null;
                                    if (checksum.equals(FabricUtils.generateMd5Hash(config))) {
//                                        Constants.LOG.info("Tried to reload config but checksum was the same!");
                                        break;
                                    }

                                    // Set Checksum
                                    checksum = FabricUtils.generateMd5Hash(config);

                                    Constants.LOG.info("Dimension Viewer config file changed! Updating player info...");
                                    loadConfig();

                                    if (!PlayerListHandler.playerList.isEmpty()) {
                                        FabricUtils.refreshDisplayNames(
                                                PlayerListHandler.playerList.getFirst().getServer().getPlayerList()
                                        );
                                    } else {
                                        Constants.LOG.info("Skipping display name refresh as no players are detected on the server");
                                    }

                                    break;
                                }
                            /*} catch (NullPointerException exception) {
                                Constants.LOG.error(exception.getMessage());
                                break;
                            }*/
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            } catch (IOException | InterruptedException | NullPointerException exception) {
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
