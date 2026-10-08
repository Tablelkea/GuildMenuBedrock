package fr.kilian.elestya.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.cumulus.form.Form;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.Objects;
import java.util.function.Consumer;

public class FormService {

    private final JavaPlugin plugin;
    private final FloodgateApi api;

    public FormService(
            JavaPlugin plugin,
            FloodgateApi api
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );

        this.api = Objects.requireNonNull(
                api,
                "api cannot be null"
        );
    }

    public boolean isBedrockPlayer(Player player) {

        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        return api.isFloodgatePlayer(
                player.getUniqueId()
        );
    }

    public boolean sendForm(
            Player player,
            Form form
    ) {

        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        Objects.requireNonNull(
                form,
                "form cannot be null"
        );

        if (!api.isFloodgatePlayer(
                player.getUniqueId()
        )) {
            return false;
        }

        FloodgatePlayer floodgatePlayer =
                api.getPlayer(
                        player.getUniqueId()
                );

        if (floodgatePlayer == null) {
            return false;
        }

        return floodgatePlayer.sendForm(form);
    }

    public void runSync(
            Player player,
            Runnable action
    ) {

        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        Objects.requireNonNull(
                action,
                "action cannot be null"
        );

        Runnable guardedAction = () -> {

            if (!player.isOnline()) {
                return;
            }

            action.run();
        };

        if (Bukkit.isPrimaryThread()) {
            guardedAction.run();
            return;
        }

        Bukkit.getScheduler().runTask(
                plugin,
                guardedAction
        );
    }

    public Runnable sync(
            Player player,
            Runnable action
    ) {

        return () -> runSync(
                player,
                action
        );
    }

    public <T> Consumer<T> sync(
            Player player,
            Consumer<T> action
    ) {

        Objects.requireNonNull(
                action,
                "action cannot be null"
        );

        return value -> runSync(
                player,
                () -> action.accept(value)
        );
    }
}