package fr.kilian.elestya.api;

import fr.kilian.elestya.api.domain.*;
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

    Optional<GuildChestInfo> getGuildChestInfo(String guildId);

    List<GuildContribution> getGuildContributions(String guildId);

    ActionResult depositAllChestItems(UUID playerId);

    /*
     * Guild membership
     */

    ActionResult createGuild(UUID playerId, String name, String entryMessage);

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

    Set<GuildPermission> getPermissions(String guildId, GuildRank rank);

    boolean hasPermission(UUID playerId, GuildPermission permission);

    ActionResult setPermission(UUID actorId, GuildRank rank, GuildPermission permission, boolean enabled);

    ActionResult transferOwnership(UUID actorId, UUID targetId);

    List<Guild> getPendingJoinRequestGuilds(UUID playerId);

    ActionResult cancelJoinRequest(UUID playerId, String guildId);

    List<Guild> getReceivedInvitations(UUID playerId);

    ActionResult invitePlayer(UUID actorId, String playerName);

    ActionResult acceptInvitation(UUID playerId, String guildId);

    ActionResult rejectInvitation(UUID playerId, String guildId);

    Optional<GuildTreasuryInfo> getTreasuryInfo(String guildId);

    ActionResult upgradeReserve(UUID actorId);

    ActionResult upgradeChestLocks(UUID actorId);

    Optional<GuildShopInfo> getGuildShopInfo(String guildId);

    Optional<GuildWeeklyObjective> getWeeklyObjective(String guildId);

    List<Guild> getGuildRanking();

}