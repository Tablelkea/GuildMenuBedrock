package fr.kilian.elestya.menu.buy;

import fr.kilian.elestya.api.ShopSource;
import fr.kilian.elestya.api.domain.shop.ShopCategory;
import fr.kilian.elestya.api.domain.shop.ShopItem;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;

import java.util.Objects;
import java.util.Optional;

public class BuyItemCategoryForm {

    private final FormService formService;
    private final ShopCategory category;
    private final ShopSource shopSource;

    public BuyItemCategoryForm(FormService formService, ShopSource shopSource, ShopCategory category) {
        this.formService = Objects.requireNonNull(formService, "formService cannot be null");
        this.shopSource = Objects.requireNonNull(shopSource, "shopSource cannot be null");
        this.category = Objects.requireNonNull(category, "category cannot be null");
    }

    public void open(Player player) {

        Objects.requireNonNull(player, "player cannot be null");

        SimpleForm.Builder builder = SimpleForm.builder()
                .title("Catégorie: " + convertCategoryName(category));

        for (ShopItem item : shopSource.getShopItemsFromCategory(category)) {
            builder.button(item.name() + "\nPrix: " + item.buyPrice() + " / unité", FormImage.Type.URL, "https://blockrender.dev/render/item/"+ item.itemStack().getType().name() + ".png?size=128");
        }
        SimpleForm form = builder.button("Retour")
                .closedOrInvalidResultHandler(formService.sync(player, response -> new BuyMainForm(shopSource, formService).open(player)))
                .validResultHandler(formService.sync(player, response -> {
                    String button = response.clickedButton().text();

                    if(button.equalsIgnoreCase("Retour")){
                        new BuyMainForm(shopSource, formService).open(player);
                        return;
                    }

                    String itemName = button.split("\n", 2)[0];

                    Optional<ShopItem> optionalItem = shopSource.findShopItemByName(itemName);

                    if (optionalItem.isEmpty()) {
                        return;
                    }

                    new BuyItemForm(shopSource, formService, optionalItem.get()).open(player);
                }))
                .build();

        formService.sendForm(player, form);

    }

    private String convertCategoryName(ShopCategory category) {
        switch (category) {

            case PLANTS -> {
                return "Plantes";
            }
            case BLOCK -> {
                return "Blocs";
            }
            case FOOD -> {
                return "Nourritures";
            }
            case DROPS -> {
                return "Butins";
            }
            case ORE -> {
                return "Minerais";
            }
        }

        return "Catégorie Invalide";
    }

}
