package de.lewtuutimer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class LewtuuTimerClient implements ClientModInitializer {
    private final Timer timer = new Timer();
    private int ticksSinceSave;

    @Override
    public void onInitializeClient() {
        TimerStorage storage = new TimerStorage();
        TimerCommands commands = new TimerCommands(timer, storage);
        commands.register();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            commands.resetConfirmation();
            ticksSinceSave = 0;
            Long savedMillis = storage.open(client);
            if (savedMillis == null) {
                timer.stop();
                return;
            }

            // Offline-Zeit wird nicht mitgezählt. Ein geladener Timer wartet auf resume.
            timer.restorePaused(savedMillis);
            client.execute(() -> {
                TimerDisplay.showTimer(client, timer);
                if (client.player != null) {
                    client.player.sendSystemMessage(TimerDisplay.status(
                        "Gespeicherter Timer pausiert geladen. Nutze /timer resume."
                    ));
                }
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            storage.save(timer);
            storage.close();
            timer.stop();
            commands.resetConfirmation();
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> storage.save(timer));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!timer.exists()) return;
            TimerDisplay.showTimer(client, timer);
            if (++ticksSinceSave >= 20) {
                storage.save(timer);
                ticksSinceSave = 0;
            }
        });
    }
}
