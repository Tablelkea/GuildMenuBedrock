package fr.kilian.elestya.api.domain.guild;

import java.util.List;
import java.util.Objects;

public record GuildShopInfo(
        int novaMembers,
        int requiredNovaMembers,
        int constellationMembers,
        int requiredConstellationMembers,
        List<GuildShopItem> items
) {

    public GuildShopInfo {
        Objects.requireNonNull(items, "items cannot be null");
        items = List.copyOf(items);
    }

    public boolean hasAccess() {
        return novaMembers >= requiredNovaMembers
                || constellationMembers >= requiredConstellationMembers;
    }
}