package fr.kilian.elestya.menu.buy;

import fr.kilian.elestya.api.ShopSource;
import fr.kilian.elestya.api.domain.shop.ShopCategory;
import fr.kilian.elestya.menu.FormService;
import fr.kilian.elestya.menu.MenuForm;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.Objects;

public class BuyMainForm {

    private final ShopSource shopSource;
    private final FormService formService;

    public BuyMainForm(ShopSource shopSource, FormService formService) {
        this.shopSource = Objects.requireNonNull(shopSource, "guildSource cannot be null");

        this.formService = Objects.requireNonNull(formService, "formService cannot be null");
    }

    public void open(Player player) {

        Objects.requireNonNull(player, "player cannot be null");

        SimpleForm.Builder builder = SimpleForm.builder()
                .title("Boutique")
                .content("Ton Solde:")
                // pas acces au solde du joueur
                .content("0");

        for (ShopCategory category : ShopCategory.values()) {
            builder.button(convertCategoryName(category));
        }

        builder.validResultHandler(formService.sync(player, response -> {
                    String button = response.clickedButton().text();

                    System.out.println(button);
                    System.out.println(shopSource.getCategoryByName(button));

                    ShopCategory category = shopSource.getCategoryByName(button);
                    new BuyItemCategoryForm(formService, shopSource, category).open(player);

                }))
                .closedOrInvalidResultHandler(() -> {
                });

        SimpleForm form = builder.build();

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
