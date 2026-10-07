package fr.kilian.elestya;

import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.event.EventRegistrar;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;

public final class Main extends JavaPlugin implements EventRegistrar {

    @Override
    public void onEnable() {
        // Plugin startup logic

        FloodgateApi api = FloodgateApi.getInstance();

        getLogger().info("Registering Geyser event bus!");
        GeyserApi.api().eventBus().register(this, this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Subscribe
    public void onGeyserPostInitializeEvent(GeyserPostInitializeEvent event) {
        getLogger().info("Geyser started!");
    }
}
