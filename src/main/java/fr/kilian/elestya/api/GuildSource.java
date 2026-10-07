package fr.kilian.elestya.api;

import fr.kilian.elestya.api.domain.Guild;
import fr.kilian.elestya.api.domain.GuildPermission;
import fr.kilian.elestya.api.domain.GuildRank;
import fr.kilian.elestya.api.domain.JoinRequest;
import fr.kilian.elestya.api.result.ActionResult;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface GuildSource {

    /*
     * Guild reading
     */

    List<Guild> getGuilds();

    List<JoinRequest> getJoinRequests(String guildId);

    Optional<Guild> findGuildByPlayer(UUID playerId);

    Optional<Guild> findGuildById(String guildId);


    /*
     * Guild membership
     */

    ActionResult createGuild(UUID playerId, String name);

    ActionResult requestToJoin(UUID playerId, String guildId);

    ActionResult acceptJoinRequest(UUID actorId, UUID playerId);

    ActionResult rejectJoinRequest(UUID actorId, UUID playerId);

    ActionResult leaveGuild(UUID playerId);


    /*
     * Guild bank
     */

    ActionResult deposit(UUID playerId, double amount);

    ActionResult withdraw(UUID playerId, double amount);


    /*
     * Member management
     */

    ActionResult promoteMember(UUID actorId, UUID targetId);

    ActionResult demoteMember(UUID actorId, UUID targetId);

    ActionResult kickMember(UUID actorId, UUID targetId);


    /*
     * Permissions
     */

    Set<GuildPermission> getPermissions(
            String guildId,
            GuildRank rank
    );

    boolean hasPermission(
            UUID playerId,
            GuildPermission permission
    );

    ActionResult setPermission(
            UUID actorId,
            GuildRank rank,
            GuildPermission permission,
            boolean enabled
    );
}