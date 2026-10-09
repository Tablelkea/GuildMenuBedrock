package fr.kilian.elestya.api.domain.guild;

import java.util.Objects;

public record GuildShopItem(
        String id,
        String name,
        String category,
        double price,
        String description
) {

    public GuildShopItem {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(category, "category cannot be null");
        Objects.requireNonNull(description, "description cannot be null");
    }
}