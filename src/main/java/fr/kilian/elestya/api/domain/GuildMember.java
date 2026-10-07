package fr.kilian.elestya.api.domain;

import java.util.UUID;

public record GuildMember(
        UUID playerId,
        String name,
        GuildRank rank
) {

    public boolean isOwner(){
        return rank.equals(GuildRank.OWNER);
    }

    public boolean isOfficer(){
        return rank.equals(GuildRank.OFFICER);
    }

}
