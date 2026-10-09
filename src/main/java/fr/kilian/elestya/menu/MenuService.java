package fr.kilian.elestya.menu;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.ShopSource;
import org.bukkit.entity.Player;

import java.util.Objects;

public class MenuService {

    private final FormService formService;
    private final GuildSource guildSource;
    private final ShopSource shopSource;

    public MenuService(
            FormService formService,
            GuildSource guildSource,
            ShopSource shopSource
    ) {
        this.formService = Objects.requireNonNull(
                formService,
                "formService cannot be null"
        );

        this.guildSource = Objects.requireNonNull(guildSource, "guildSource cannot be null");
        this.shopSource = Objects.requireNonNull(shopSource, "shopSource cannot be null");
    }

    public void ouvrirMenuPrincipal(Player player){

        Objects.requireNonNull(player, "player cannot be null");

        formService.runSync(player, () -> openMenu(player));

    }

    public void openMenu(Player player){

        Objects.requireNonNull(player, "player cannot be null");

        new MenuForm(shopSource, guildSource, formService).open(player);

    }

}
