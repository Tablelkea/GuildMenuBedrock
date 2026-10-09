package fr.kilian.elestya.source.memory;

import fr.kilian.elestya.api.domain.shop.ShopCategory;
import fr.kilian.elestya.api.domain.shop.ShopItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class MemoryShopSeeder {

    private final MemoryShopSource source;

    public MemoryShopSeeder(MemoryShopSource source){
        this.source = Objects.requireNonNull(source, "source cannot be null");
    }

    public void seed(){

        // ==================== BLOCK ====================

        source.addShopItem(new ShopItem("Bûche de Chêne", ItemStack.of(Material.OAK_LOG), 10, 1, ShopCategory.BLOCK));
        source.addShopItem(new ShopItem("Bûche de Sapin", ItemStack.of(Material.SPRUCE_LOG), 10, 1, ShopCategory.BLOCK));
        source.addShopItem(new ShopItem("Pierre", ItemStack.of(Material.STONE), 8, 1, ShopCategory.BLOCK));
        source.addShopItem(new ShopItem("Pierre Taillée", ItemStack.of(Material.COBBLESTONE), 5, 1, ShopCategory.BLOCK));
        source.addShopItem(new ShopItem("Terre", ItemStack.of(Material.DIRT), 3, 1, ShopCategory.BLOCK));
        source.addShopItem(new ShopItem("Sable", ItemStack.of(Material.SAND), 8, 2, ShopCategory.BLOCK));
        source.addShopItem(new ShopItem("Gravier", ItemStack.of(Material.GRAVEL), 7, 2, ShopCategory.BLOCK));
        source.addShopItem(new ShopItem("Verre", ItemStack.of(Material.GLASS), 15, 3, ShopCategory.BLOCK));
        source.addShopItem(new ShopItem("Obsidienne", ItemStack.of(Material.OBSIDIAN), 100, 15, ShopCategory.BLOCK));
        source.addShopItem(new ShopItem("Briques", ItemStack.of(Material.BRICKS), 25, 5, ShopCategory.BLOCK));


// ==================== FOOD ====================

        source.addShopItem(new ShopItem("Pomme", ItemStack.of(Material.APPLE), 15, 3, ShopCategory.FOOD));
        source.addShopItem(new ShopItem("Pain", ItemStack.of(Material.BREAD), 20, 4, ShopCategory.FOOD));
        source.addShopItem(new ShopItem("Carotte", ItemStack.of(Material.CARROT), 10, 2, ShopCategory.FOOD));
        source.addShopItem(new ShopItem("Pomme de Terre", ItemStack.of(Material.POTATO), 10, 2, ShopCategory.FOOD));
        source.addShopItem(new ShopItem("Steak Cuit", ItemStack.of(Material.COOKED_BEEF), 35, 8, ShopCategory.FOOD));
        source.addShopItem(new ShopItem("Poulet Cuit", ItemStack.of(Material.COOKED_CHICKEN), 30, 7, ShopCategory.FOOD));
        source.addShopItem(new ShopItem("Porc Cuit", ItemStack.of(Material.COOKED_PORKCHOP), 35, 8, ShopCategory.FOOD));
        source.addShopItem(new ShopItem("Saumon Cuit", ItemStack.of(Material.COOKED_SALMON), 30, 6, ShopCategory.FOOD));
        source.addShopItem(new ShopItem("Cookie", ItemStack.of(Material.COOKIE), 12, 2, ShopCategory.FOOD));
        source.addShopItem(new ShopItem("Pomme Dorée", ItemStack.of(Material.GOLDEN_APPLE), 250, 50, ShopCategory.FOOD));


// ==================== DROPS ====================

        source.addShopItem(new ShopItem("Chair Putréfiée", ItemStack.of(Material.ROTTEN_FLESH), 8, 2, ShopCategory.DROPS));
        source.addShopItem(new ShopItem("Os", ItemStack.of(Material.BONE), 15, 4, ShopCategory.DROPS));
        source.addShopItem(new ShopItem("Fil", ItemStack.of(Material.STRING), 15, 4, ShopCategory.DROPS));
        source.addShopItem(new ShopItem("Poudre à Canon", ItemStack.of(Material.GUNPOWDER), 35, 10, ShopCategory.DROPS));
        source.addShopItem(new ShopItem("Œil d'Araignée", ItemStack.of(Material.SPIDER_EYE), 20, 5, ShopCategory.DROPS));
        source.addShopItem(new ShopItem("Perle de l'Ender", ItemStack.of(Material.ENDER_PEARL), 100, 25, ShopCategory.DROPS));
        source.addShopItem(new ShopItem("Bâton de Blaze", ItemStack.of(Material.BLAZE_ROD), 120, 30, ShopCategory.DROPS));
        source.addShopItem(new ShopItem("Larme de Ghast", ItemStack.of(Material.GHAST_TEAR), 200, 50, ShopCategory.DROPS));
        source.addShopItem(new ShopItem("Crème de Magma", ItemStack.of(Material.MAGMA_CREAM), 75, 15, ShopCategory.DROPS));
        source.addShopItem(new ShopItem("Membrane de Phantom", ItemStack.of(Material.PHANTOM_MEMBRANE), 150, 35, ShopCategory.DROPS));


// ==================== PLANTS ====================

        source.addShopItem(new ShopItem("Graines de Blé", ItemStack.of(Material.WHEAT_SEEDS), 5, 1, ShopCategory.PLANTS));
        source.addShopItem(new ShopItem("Blé", ItemStack.of(Material.WHEAT), 12, 3, ShopCategory.PLANTS));
        source.addShopItem(new ShopItem("Canne à Sucre", ItemStack.of(Material.SUGAR_CANE), 15, 3, ShopCategory.PLANTS));
        source.addShopItem(new ShopItem("Bambou", ItemStack.of(Material.BAMBOO), 10, 2, ShopCategory.PLANTS));
        source.addShopItem(new ShopItem("Cactus", ItemStack.of(Material.CACTUS), 12, 3, ShopCategory.PLANTS));
        source.addShopItem(new ShopItem("Citrouille", ItemStack.of(Material.PUMPKIN), 20, 5, ShopCategory.PLANTS));
        source.addShopItem(new ShopItem("Pastèque", ItemStack.of(Material.MELON), 25, 6, ShopCategory.PLANTS));
        source.addShopItem(new ShopItem("Fleur de Pissenlit", ItemStack.of(Material.DANDELION), 8, 2, ShopCategory.PLANTS));
        source.addShopItem(new ShopItem("Rose du Wither", ItemStack.of(Material.WITHER_ROSE), 300, 75, ShopCategory.PLANTS));
        source.addShopItem(new ShopItem("Nénuphar", ItemStack.of(Material.LILY_PAD), 30, 8, ShopCategory.PLANTS));


// ==================== ORE ====================

        source.addShopItem(new ShopItem("Charbon", ItemStack.of(Material.COAL), 20, 5, ShopCategory.ORE));
        source.addShopItem(new ShopItem("Fer Brut", ItemStack.of(Material.RAW_IRON), 40, 10, ShopCategory.ORE));
        source.addShopItem(new ShopItem("Cuivre Brut", ItemStack.of(Material.RAW_COPPER), 25, 6, ShopCategory.ORE));
        source.addShopItem(new ShopItem("Or Brut", ItemStack.of(Material.RAW_GOLD), 60, 15, ShopCategory.ORE));
        source.addShopItem(new ShopItem("Lingot de Fer", ItemStack.of(Material.IRON_INGOT), 50, 12, ShopCategory.ORE));
        source.addShopItem(new ShopItem("Lingot d'Or", ItemStack.of(Material.GOLD_INGOT), 75, 18, ShopCategory.ORE));
        source.addShopItem(new ShopItem("Lapis-Lazuli", ItemStack.of(Material.LAPIS_LAZULI), 30, 7, ShopCategory.ORE));
        source.addShopItem(new ShopItem("Redstone", ItemStack.of(Material.REDSTONE), 20, 5, ShopCategory.ORE));
        source.addShopItem(new ShopItem("Diamant", ItemStack.of(Material.DIAMOND), 500, 125, ShopCategory.ORE));
        source.addShopItem(new ShopItem("Émeraude", ItemStack.of(Material.EMERALD), 400, 100, ShopCategory.ORE));

    }

}
