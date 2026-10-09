package fr.kilian.elestya.command;

import fr.kilian.elestya.ElestyaBedrock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ShopMenuCommand implements CommandExecutor {

    private final ElestyaBedrock plugin;

    public ShopMenuCommand(ElestyaBedrock plugin){
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    "Cette commande doit être exécutée par un joueur."
            );
            return false;
        }

        plugin.ouvrirMenuBoutique(player);

        return true;
    }
}
