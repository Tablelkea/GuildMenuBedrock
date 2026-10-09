package fr.kilian.elestya.source.memory;

import fr.kilian.elestya.api.ShopSource;
import fr.kilian.elestya.api.domain.shop.ShopCategory;
import fr.kilian.elestya.api.domain.shop.ShopItem;
import fr.kilian.elestya.api.result.ActionResult;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class MemoryShopSource implements ShopSource {

    private final Map<String, ShopItem> shopitems = new HashMap<>();
    private final Map<ShopCategory, List<ShopItem>> shopCategory = new HashMap<>();

    public MemoryShopSource(){
        new MemoryShopSeeder(this).seed();
    }

    public void addShopItem(ShopItem item){
        Objects.requireNonNull(item, "item cannot be null");

        shopitems.put(item.name().toLowerCase(Locale.ROOT), item);
        shopCategory.putIfAbsent(item.category(), new ArrayList<>());
        shopCategory.get(item.category()).add(item);
    }

    @Override
    public List<ShopItem> getShopItems() {
        return new ArrayList<>(shopitems.values());
    }

    @Override
    public Optional<Double> getItemBuyPrice(String name) {

        Optional<ShopItem> optionalItem = findShopItemByName(name);

        if(optionalItem.isEmpty()){
            return Optional.empty();
        }

        ShopItem item = optionalItem.get();

        return Optional.of(item.buyPrice());

    }


    @Override
    public Optional<Double> getItemSellPrice(String name) {

        Objects.requireNonNull(name, "name cannot be null");

        Optional<ShopItem> optionalItem = findShopItemByName(name);

        if(optionalItem.isEmpty()){
            return Optional.empty();
        }

        ShopItem item = optionalItem.get();

        return Optional.of(item.sellPrice());
    }

    @Override
    public Optional<ShopItem> findShopItemByName(String name) {

        Objects.requireNonNull(name, "name cannot be null");

        return Optional.ofNullable(shopitems.get(name.toLowerCase(Locale.ROOT)));
    }

    @Override
    public List<ShopItem> getShopItemsFromCategory(ShopCategory category) {

        Objects.requireNonNull(category, "category cannot be null");

        return shopCategory.get(category);

    }

    @Override
    public ActionResult sell(Player player, ShopItem item, int quantity) {

        Objects.requireNonNull(player, "playerId cannot be null");
        Objects.requireNonNull(item, "item cannot be null");

        if (quantity <= 0) {
            return ActionResult.failure("Le montant doit être supérieur à 0.");
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack itemstack = item.itemStack();

        if(inventory.contains(itemstack, quantity)){

            itemstack.setAmount(quantity);
           inventory.remove(itemstack);
        }

        ActionResult result = ActionResult.success("Vous avez vendu " + quantity + " " + item.name());
        player.sendMessage(result.message());
        return result;
    }

    @Override
    public ActionResult buy(Player player, ShopItem item, int quantity) {

        Objects.requireNonNull(player, "playerId cannot be null");
        Objects.requireNonNull(item, "item cannot be null");

        if (quantity <= 0) {
            return ActionResult.failure("Le montant doit être supérieur à 0.");
        }

        item.itemStack().setAmount(quantity);

        player.give(item.itemStack());

        ActionResult result = ActionResult.success("Vous avez acheter " + quantity + " " + item.name());
        player.sendMessage(result.message());
        return result;
    }

    @Override
    public ShopCategory getCategoryByName(String name) {

        switch (name) {

            case "Plantes" -> {
                return ShopCategory.PLANTS;
            }
            case "Blocs" -> {
                return ShopCategory.BLOCK;
            }
            case "Nourritures" -> {
                return ShopCategory.FOOD;
            }
            case "Butins" -> {
                return ShopCategory.DROPS;
            }
            case "Minerais" -> {
                return ShopCategory.ORE;
            }
        }

        return null;
    }
}
