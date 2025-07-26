package dev.stick_stack.dimensionviewer.platform;

import dev.stick_stack.dimensionviewer.Constants;
import dev.stick_stack.dimensionviewer.platform.services.IConfigHelper;

import java.util.ServiceLoader;

public class Services {

    public static final IConfigHelper CONFIG = load(IConfigHelper.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));

        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }

}
