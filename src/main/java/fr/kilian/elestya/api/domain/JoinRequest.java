package fr.kilian.elestya.api.domain;

import java.util.UUID;

public record JoinRequest(
        UUID playerId,
        String guildId
) {
}
