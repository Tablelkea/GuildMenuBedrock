package fr.kilian.elestya.api.domain.shop;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public record ShopItem(String name, ItemStack itemStack, double buyPrice, double sellPrice, ShopCategory category) {

    public ShopItem(String name, ItemStack itemStack, double buyPrice, double sellPrice, ShopCategory category){
        this.itemStack = Objects.requireNonNull(itemStack, "itemstack cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        this.category = Objects.requireNonNull(category, "category cannot be null");

        if(buyPrice < 0){
            throw new IllegalArgumentException("buyPrice cannot be less than 0");
        }

        if(sellPrice < 0){
            throw new IllegalArgumentException("sellPrice cannot be less than 0");
        }

        if(name.isBlank()){
            throw new IllegalArgumentException("name cannot be blank");
        }

        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.name = name;
    }

}
