package fr.kilian.elestya.api.domain.guild;

import java.util.UUID;

public record GuildMember(
        UUID playerId,
        String name,
        GuildRank rank
) {

    public boolean isOwner() {
        return rank.equals(GuildRank.OWNER);
    }

    public boolean isDeputy() {
        return rank.equals(GuildRank.DEPUTY);
    }

}
