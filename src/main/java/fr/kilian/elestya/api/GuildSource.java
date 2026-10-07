package fr.kilian.elestya.api;

import fr.kilian.elestya.api.domain.Guild;
import fr.kilian.elestya.api.result.ActionResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuildSource {

    List<Guild> getGuilds();

    Optional<Guild> findGuildByPlayer(UUID playerId);

    Optional<Guild> findGuildById(String guildId);

    ActionResult createGuild(UUID playerId, String name);

    ActionResult requestToJoin(UUID playerId, String guildId);

    ActionResult acceptJoinRequest(UUID actorId, UUID playerId);

    ActionResult rejectJoinRequest(UUID actorId, UUID playerId);

    ActionResult leaveGuild(UUID playerId);

    ActionResult deposit(UUID playerId, double amount);

    ActionResult withdraw(UUID playerId, double amount);

    ActionResult promoteMember(UUID actorId, UUID targetId);

    ActionResult demoteMember(UUID actorId, UUID targetId);

    ActionResult kickMember(UUID actorId, UUID targetId);
}
