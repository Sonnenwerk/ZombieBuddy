package me.zed_0xff.zombie_buddy;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class ModSignatureCache {
    private static final Path CACHE_FILE = Path.of(System.getProperty("user.home"), ".zombie_buddy", "sig_cache.properties");
    private static final Properties CACHE = new Properties();

    static {
        if (Files.exists(CACHE_FILE)) {
            try (InputStream in = Files.newInputStream(CACHE_FILE)) {
                CACHE.load(in);
            } catch (IOException ignored) {}
        }
    }

    public static boolean isCachedValid(Path jarFile) {
        try {
            long lastModified = Files.getLastModifiedTime(jarFile).toMillis();
            long size = Files.size(jarFile);
            String key = jarFile.toAbsolutePath().toString();
            String cachedMeta = CACHE.getProperty(key);

            return cachedMeta != null && cachedMeta.equals(lastModified + ":" + size);
        } catch (IOException e) {
            return false;
        }
    }

    public static void cacheVerified(Path jarFile) {
        try {
            long lastModified = Files.getLastModifiedTime(jarFile).toMillis();
            long size = Files.size(jarFile);
            String key = jarFile.toAbsolutePath().toString();
            CACHE.setProperty(key, lastModified + ":" + size);

            Files.createDirectories(CACHE_FILE.getParent());
            try (OutputStream out = Files.newOutputStream(CACHE_FILE)) {
                CACHE.store(out, "ZombieBuddy Signature Validation Cache");
            }
        } catch (IOException ignored) {}
    }
}
