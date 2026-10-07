package fr.kilian.elestya;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.command.GuildMenuCommand;
import fr.kilian.elestya.menu.FormService;
import fr.kilian.elestya.menu.guild.GuildMenuService;
import fr.kilian.elestya.source.memory.MemoryGuildSource;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Objects;

public final class Main extends JavaPlugin {

    private GuildSource guildSource;
    private FormService formService;
    private GuildMenuService guildMenuService;

    private FloodgateApi api;

    @Override
    public void onEnable() {

        api = FloodgateApi.getInstance();

        guildSource = new MemoryGuildSource();
        formService = new FormService(api);

        guildMenuService = new GuildMenuService(
                guildSource,
                formService
        );

        Objects.requireNonNull(
                getCommand("form"),
                "form command is not defined in plugin.yml"
        ).setExecutor(
                new GuildMenuCommand(this)
        );

        getLogger().info("Elestya Bedrock Menus activé.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Elestya Bedrock Menus désactivé.");
    }

    public void ouvrirMenuGuilde(Player player) {

        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        guildMenuService.ouvrirMenuGuilde(player);
    }
}