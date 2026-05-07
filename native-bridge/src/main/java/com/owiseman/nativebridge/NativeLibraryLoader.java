package com.owiseman.nativebridge;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class NativeLibraryLoader {

    private static final Logger LOG = Logger.getLogger(NativeLibraryLoader.class.getName());
    private static final AtomicBoolean loaded = new AtomicBoolean(false);

    public static synchronized void loadLibrary(String libraryName) {
        if (loaded.getAndSet(true)) {
            LOG.fine("Native library already loaded: " + libraryName);
            return;
        }

        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();

        String libraryPath = resolveLibraryPath(libraryName, osName, osArch);
        LOG.info("Loading native library: " + libraryPath);

        try {
            System.loadLibrary(libraryName);
            LOG.info("Native library loaded: " + libraryName);
        } catch (UnsatisfiedLinkError e) {
            LOG.info("System library not found, attempting to load from classpath...");
            loadFromClasspath(libraryName, osName, osArch);
        }
    }

    public static synchronized void loadFromPath(Path libraryPath) {
        if (loaded.get()) {
            LOG.fine("Native library already loaded");
            return;
        }

        LOG.info("Loading native library from path: " + libraryPath);
        System.load(libraryPath.toAbsolutePath().toString());
        loaded.set(true);
        LOG.info("Native library loaded from path successfully");
    }

    private static void loadFromClasspath(String libraryName, String osName, String osArch) {
        String resourcePath = "native/" + osArch + "/" + System.mapLibraryName(libraryName);

        try (InputStream is = NativeLibraryLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Native library not found in classpath: " + resourcePath);
            }

            Path tempDir = Files.createTempDirectory("office-agent-native");
            Path tempFile = tempDir.resolve(System.mapLibraryName(libraryName));
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);

            System.load(tempFile.toAbsolutePath().toString());
            loaded.set(true);
            LOG.info("Native library loaded from classpath: " + resourcePath);

            tempFile.toFile().deleteOnExit();
            tempDir.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load native library from classpath: " + resourcePath, e);
        }
    }

    private static String resolveLibraryPath(String libraryName, String osName, String osArch) {
        return "native/" + osArch + "/" + libraryName;
    }

    public static boolean isLoaded() {
        return loaded.get();
    }
}
