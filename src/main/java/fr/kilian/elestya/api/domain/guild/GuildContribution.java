package fr.kilian.elestya.api.domain.guild;

import java.util.Objects;
import java.util.UUID;

public record GuildContribution(
        UUID playerId,
        String playerName,
        int amount
) {

    public GuildContribution {

        Objects.requireNonNull(
                playerId,
                "playerId cannot be null"
        );

        Objects.requireNonNull(
                playerName,
                "playerName cannot be null"
        );
    }
}