package fr.kilian.elestya;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.ShopSource;
import fr.kilian.elestya.command.GuildMenuCommand;
import fr.kilian.elestya.command.MainMenuCommand;
import fr.kilian.elestya.command.ShopMenuCommand;
import fr.kilian.elestya.menu.FormService;
import fr.kilian.elestya.menu.MenuService;
import fr.kilian.elestya.menu.guild.GuildMenuService;
import fr.kilian.elestya.menu.buy.BuyMenuService;
import fr.kilian.elestya.source.memory.MemoryGuildSource;
import fr.kilian.elestya.source.memory.MemoryShopSource;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Objects;

public final class ElestyaBedrock extends JavaPlugin {

    private GuildSource guildSource;
    private ShopSource shopSource;
    private FormService formService;
    private GuildMenuService guildMenuService;
    private BuyMenuService buyMenuService;
    private MenuService menuService;

    private FloodgateApi api;

    @Override
    public void onEnable() {

        api = FloodgateApi.getInstance();

        guildSource = new MemoryGuildSource();
        shopSource = new MemoryShopSource();

        formService = new FormService(this, api);

        guildMenuService = new GuildMenuService(
                guildSource,
                formService
        );

        buyMenuService = new BuyMenuService(
                shopSource,
                formService
        );

        menuService = new MenuService(
                formService,
                guildSource,
                shopSource
        );

        Objects.requireNonNull(
                getCommand("guilde"),
                "guildmenu command is not defined in plugin.yml"
        ).setExecutor(
                new GuildMenuCommand(this)
        );

        Objects.requireNonNull(
                getCommand("boutique"),
                "boutique command is not defined in plugin.yml"
        ).setExecutor(
                new ShopMenuCommand(this)
        );

        Objects.requireNonNull(
                getCommand("menu"),
                "menu command is not defined in plugin.yml"
        ).setExecutor(
                new MainMenuCommand(this)
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

    public void ouvrirMenuBoutique(Player player){
        Objects.requireNonNull(player, "player cannot be null");

        buyMenuService.ouvrirMenuBoutique(player);


    }

    public void ouvrirMenuPrincipal(Player player){
        Objects.requireNonNull(player, "player cannot be null");

        menuService.ouvrirMenuPrincipal(player);
    }


}