package fr.kilian.elestya.api.domain.guild;

import java.util.UUID;

public record JoinRequest(
        UUID playerId,
        String playerName
) {
}
