package de.lewtuutimer;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

final class TimerCommands {
    private static final long CONFIRMATION_WINDOW_MILLIS = 10_000;

    private final Timer timer;
    private final TimerStorage storage;
    private long overwriteConfirmationUntil;

    TimerCommands(Timer timer, TimerStorage storage) {
        this.timer = timer;
        this.storage = storage;
    }

    void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerCommand(dispatcher, "lewtuutimer");
            registerCommand(dispatcher, "timer");
        });
    }

    void resetConfirmation() {
        overwriteConfirmationUntil = 0;
    }

    private void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, String commandName) {
        dispatcher.register(ClientCommands.literal(commandName)
            .then(ClientCommands.literal("start").executes(context -> start(context.getSource(), commandName)))
            .then(ClientCommands.literal("stop").executes(context -> stop(context.getSource())))
            .then(ClientCommands.literal("pause").executes(context -> pause(context.getSource())))
            .then(ClientCommands.literal("resume").executes(context -> resume(context.getSource())))
        );
    }

    private int start(FabricClientCommandSource source, String commandName) {
        long now = System.currentTimeMillis();
        if (timer.exists() && now > overwriteConfirmationUntil) {
            overwriteConfirmationUntil = now + CONFIRMATION_WINDOW_MILLIS;
            source.sendError(Component.literal(
                "Es gibt bereits einen Timer. Führe /" + commandName
                    + " start innerhalb von 10 Sekunden erneut aus, um ihn zu überschreiben."
            ));
            return 0;
        }

        overwriteConfirmationUntil = 0;
        timer.start();
        storage.save(timer);
        source.sendFeedback(TimerDisplay.status("Timer gestartet."));
        return 1;
    }

    private int stop(FabricClientCommandSource source) {
        timer.stop();
        overwriteConfirmationUntil = 0;
        storage.remove();
        TimerDisplay.clearActionBar();
        source.sendFeedback(TimerDisplay.status("Timer gestoppt und gelöscht."));
        return 1;
    }

    private int pause(FabricClientCommandSource source) {
        if (!timer.pause()) {
            source.sendError(Component.literal("Der Timer läuft nicht."));
            return 0;
        }

        storage.save(timer);
        TimerDisplay.showTimer(Minecraft.getInstance(), timer);
        source.sendFeedback(TimerDisplay.status("Timer pausiert."));
        return 1;
    }

    private int resume(FabricClientCommandSource source) {
        if (!timer.resume()) {
            source.sendError(Component.literal("Der Timer ist nicht pausiert."));
            return 0;
        }

        storage.save(timer);
        source.sendFeedback(TimerDisplay.status("Timer fortgesetzt."));
        return 1;
    }
}
