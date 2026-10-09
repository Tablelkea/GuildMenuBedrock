package fr.kilian.elestya.menu.buy;

import fr.kilian.elestya.api.ShopSource;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;

import java.util.Objects;

public class BuyMenuService {

    private final ShopSource shopSource;
    private final FormService formService;

    public BuyMenuService(
            ShopSource shopSource,
            FormService formService
    ) {
        this.shopSource = Objects.requireNonNull(
                shopSource,
                "shopSource cannot be null"
        );

        this.formService = Objects.requireNonNull(
                formService,
                "formService cannot be null"
        );
    }

    public void ouvrirMenuBoutique(Player player){

        Objects.requireNonNull(player, "player cannot be null");

        formService.runSync(player, () -> openMenu(player));

    }

    public void openMenu(Player player){

        Objects.requireNonNull(player, "player cannot be null");

        new BuyMainForm(shopSource, formService).open(player);

    }

}
