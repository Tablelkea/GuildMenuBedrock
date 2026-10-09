package fr.kilian.elestya.api;

import fr.kilian.elestya.api.domain.shop.ShopCategory;
import fr.kilian.elestya.api.domain.shop.ShopItem;
import fr.kilian.elestya.api.result.ActionResult;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public interface ShopSource {

    List<ShopItem> getShopItems();

    Optional<Double> getItemBuyPrice(String name);

    Optional<Double> getItemSellPrice(String name);

    Optional<ShopItem> findShopItemByName(String name);

    List<ShopItem> getShopItemsFromCategory(ShopCategory category);

    ActionResult sell(Player player, ShopItem item, int quantity);

    ActionResult buy(Player player, ShopItem item, int quantity);

    ShopCategory getCategoryByName(String name);
}
