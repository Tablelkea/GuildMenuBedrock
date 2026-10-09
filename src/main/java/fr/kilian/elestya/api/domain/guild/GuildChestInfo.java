package fr.kilian.elestya.api.domain.guild;

import java.util.Objects;

public record GuildChestInfo(
        String seasonName,
        String timeRemaining,
        int points,
        String targetItemName,
        int targetAmount,
        int depositedAmount
) {

    public GuildChestInfo {

        Objects.requireNonNull(
                seasonName,
                "seasonName cannot be null"
        );

        Objects.requireNonNull(
                timeRemaining,
                "timeRemaining cannot be null"
        );

        Objects.requireNonNull(
                targetItemName,
                "targetItemName cannot be null"
        );
    }
}