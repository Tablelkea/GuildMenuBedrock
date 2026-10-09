package fr.kilian.elestya.menu.buy;

import fr.kilian.elestya.api.ShopSource;
import fr.kilian.elestya.api.domain.shop.ShopItem;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;

import java.util.Objects;

public class BuyItemForm {

    private final ShopSource shopSource;
    private final FormService formService;
    private final ShopItem item;

    public BuyItemForm(ShopSource shopSource, FormService formService, ShopItem item) {
        this.formService = Objects.requireNonNull(formService, "formService cannot be null");
        this.shopSource = Objects.requireNonNull(shopSource, "shopSource cannot be null");
        this.item = Objects.requireNonNull(item, "item cannot be null");
    }

    public void open(Player player) {

        Objects.requireNonNull(player, "player cannot be null");

        CustomForm form = CustomForm.builder()
                .title(item.name())
                .label("Prix: " + item.buyPrice() + " / unité")
                .slider("Quantité:", 1, 64, 1, 1)

                .closedOrInvalidResultHandler(formService.sync(player, response -> new BuyItemCategoryForm(formService, shopSource, item.category()).open(player)))
                .validResultHandler(formService.sync(player, response -> {

                    int quantity = (int) response.asSlider();

                    shopSource.buy(player, item, quantity);
                    new BuyItemForm(shopSource, formService, item).open(player);
                }))
                .build();

        formService.sendForm(player, form);

    }

}
