package fr.kilian.elestya.command;

import fr.kilian.elestya.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class GuildMenuCommand implements CommandExecutor {

    private final Main plugin;

    public GuildMenuCommand(Main plugin) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    "Cette commande doit être exécutée par un joueur."
            );
            return true;
        }

        plugin.ouvrirMenuGuilde(player);

        return true;
    }
}