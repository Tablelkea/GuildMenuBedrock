package fr.kilian.elestya.api.domain.guild;

import java.util.Objects;

public record GuildWeeklyObjective(
        String name,
        String description,
        int currentProgress,
        int targetProgress,
        int rewardPoints
) {

    public GuildWeeklyObjective {
        Objects.requireNonNull(
                name,
                "name cannot be null"
        );

        Objects.requireNonNull(
                description,
                "description cannot be null"
        );
    }

    public boolean completed() {
        return currentProgress >= targetProgress;
    }
}