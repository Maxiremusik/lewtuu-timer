package de.lewtuutimer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class TimerStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger("lewtuutimer");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file = FabricLoader.getInstance().getConfigDir().resolve("lewtuutimer.json");
    private SavedTimers savedTimers = new SavedTimers();
    private String serverKey;

    TimerStorage() {
        load();
    }

    Long open(Minecraft client) {
        ServerData server = client.getCurrentServer();
        serverKey = server != null && server.ip != null && !server.ip.isBlank()
            ? server.ip.toLowerCase(Locale.ROOT)
            : "singleplayer";
        return savedTimers.servers.get(serverKey);
    }

    void close() {
        serverKey = null;
    }

    void save(Timer timer) {
        if (serverKey == null || !timer.exists()) return;
        savedTimers.servers.put(serverKey, timer.currentElapsedMillis());
        write();
    }

    void remove() {
        if (serverKey == null) return;
        savedTimers.servers.remove(serverKey);
        write();
    }

    private void load() {
        if (!Files.exists(file)) return;
        try {
            SavedTimers loaded = GSON.fromJson(Files.readString(file), SavedTimers.class);
            if (loaded != null && loaded.servers != null) savedTimers = loaded;
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Could not load timers from {}", file, exception);
        }
    }

    private void write() {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(savedTimers));
        } catch (IOException exception) {
            LOGGER.error("Could not save timers to {}", file, exception);
        }
    }

    private static final class SavedTimers {
        private Map<String, Long> servers = new HashMap<>();
    }
}
