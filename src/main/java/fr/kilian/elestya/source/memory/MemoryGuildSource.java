package fr.kilian.elestya.source.memory;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.Guild;
import fr.kilian.elestya.api.domain.GuildMember;
import fr.kilian.elestya.api.domain.GuildRank;
import fr.kilian.elestya.api.result.ActionResult;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class MemoryGuildSource implements GuildSource {

    private final Map<String, Guild> guilds = new HashMap<>();

    public MemoryGuildSource() {
        new MemoryGuildSeeder(this).seed();
    }

    @Override
    public List<Guild> getGuilds() {
        return new ArrayList<>(guilds.values());
    }

    @Override
    public Optional<Guild> findGuildByPlayer(UUID playerId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        for (Guild guild : guilds.values()) {
            if (guild.isMember(playerId)) {
                return Optional.of(guild);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Guild> findGuildById(String guildId) {

        Objects.requireNonNull(guildId, "guildId cannot be null");

        return Optional.ofNullable(guilds.get(guildId));
    }

    @Override
    public ActionResult createGuild(UUID playerId, String name) {

        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");

        if (findGuildByPlayer(playerId).isPresent()) {
            return ActionResult.failure("You already have a guild.");
        }

        if (name.isBlank()) {
            return ActionResult.failure("Guild name cannot be empty.");
        }

        if (guilds.containsKey(name)) {
            return ActionResult.failure("A guild with this name already exists.");
        }

        String playerName = Bukkit.getOfflinePlayer(playerId).getName();

        List<GuildMember> members = new ArrayList<>();

        members.add(
                new GuildMember(
                        playerId,
                        playerName,
                        GuildRank.OWNER
                )
        );

        Guild guild = new Guild(
                name,
                name,
                playerId,
                members,
                0,
                new ArrayList<>()
        );

        guilds.put(guild.id(), guild);

        return ActionResult.success("Guild created successfully.");
    }

    @Override
    public ActionResult requestToJoin(UUID playerId, String guildId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(guildId, "guildId cannot be null");

        Optional<Guild> optionalGuild = findGuildById(guildId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("This guild doesn't exist.");
        }

        if (findGuildByPlayer(playerId).isPresent()) {
            return ActionResult.failure("You are already in a guild.");
        }

        Guild guild = optionalGuild.get();

        if (guild.hasJoinRequest(playerId)) {
            return ActionResult.failure(
                    "You have already requested to join this guild."
            );
        }

        int requests = 0;

        for (Guild existingGuild : guilds.values()) {
            if (existingGuild.hasJoinRequest(playerId)) {
                requests++;
            }
        }

        if (requests >= 5) {
            return ActionResult.failure(
                    "You already have 5 pending guild requests."
            );
        }

        guild.joinRequests().add(playerId);

        return ActionResult.success(
                "You requested to join " + guild.name() + "."
        );
    }

    @Override
    public ActionResult acceptJoinRequest(UUID actorId, UUID playerId) {

        Objects.requireNonNull(actorId, "actorId cannot be null");
        Objects.requireNonNull(playerId, "playerId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("You don't have a guild.");
        }

        Guild guild = optionalGuild.get();

        if (!guild.ownerId().equals(actorId)) {
            return ActionResult.failure(
                    "You are not the owner of this guild."
            );
        }

        if (!guild.hasJoinRequest(playerId)) {
            return ActionResult.failure(
                    "This player didn't request to join your guild."
            );
        }

        if (findGuildByPlayer(playerId).isPresent()) {

            guild.joinRequests().remove(playerId);

            return ActionResult.failure(
                    "This player is already in another guild."
            );
        }

        String playerName = Bukkit.getOfflinePlayer(playerId).getName();

        guild.members().add(
                new GuildMember(
                        playerId,
                        playerName,
                        GuildRank.MEMBER
                )
        );

        for (Guild existingGuild : guilds.values()) {
            existingGuild.joinRequests().remove(playerId);
        }

        return ActionResult.success(
                playerName + " joined your guild."
        );
    }

    @Override
    public ActionResult rejectJoinRequest(UUID actorId, UUID playerId) {

        Objects.requireNonNull(actorId, "actorId cannot be null");
        Objects.requireNonNull(playerId, "playerId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("You don't have a guild.");
        }

        Guild guild = optionalGuild.get();

        if (!guild.ownerId().equals(actorId)) {
            return ActionResult.failure(
                    "You are not the owner of this guild."
            );
        }

        if (!guild.hasJoinRequest(playerId)) {
            return ActionResult.failure(
                    "This player didn't request to join your guild."
            );
        }

        String playerName = Bukkit.getOfflinePlayer(playerId).getName();

        guild.joinRequests().remove(playerId);

        return ActionResult.success(
                "You rejected " + playerName + "'s join request."
        );
    }

    @Override
    public ActionResult leaveGuild(UUID playerId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(playerId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("You don't have a guild.");
        }

        Guild guild = optionalGuild.get();

        if (guild.ownerId().equals(playerId)) {
            return ActionResult.failure(
                    "The guild owner cannot leave the guild."
            );
        }

        Optional<GuildMember> optionalMember = guild.findMember(playerId);

        if (optionalMember.isEmpty()) {
            return ActionResult.failure(
                    "You are not a member of this guild."
            );
        }

        guild.members().remove(optionalMember.get());

        return ActionResult.success(
                "You left " + guild.name() + "."
        );
    }

    @Override
    public ActionResult deposit(UUID playerId, double amount) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        if (amount <= 0) {
            return ActionResult.failure(
                    "The amount must be greater than 0."
            );
        }

        Optional<Guild> optionalGuild = findGuildByPlayer(playerId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "You don't have a guild."
            );
        }

        Guild guild = optionalGuild.get();

        guild.setBalance(
                guild.balance() + amount
        );

        return ActionResult.success(
                "You deposited " + amount + " into the guild bank."
        );
    }

    @Override
    public ActionResult withdraw(UUID playerId, double amount) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        if (amount <= 0) {
            return ActionResult.failure(
                    "The amount must be greater than 0."
            );
        }

        Optional<Guild> optionalGuild = findGuildByPlayer(playerId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "You don't have a guild."
            );
        }

        Guild guild = optionalGuild.get();

        Optional<GuildMember> optionalMember = guild.findMember(playerId);

        if (optionalMember.isEmpty()) {
            return ActionResult.failure(
                    "You are not a member of this guild."
            );
        }

        GuildMember member = optionalMember.get();

        if (
                member.rank() != GuildRank.OWNER
                        && member.rank() != GuildRank.OFFICER
        ) {
            return ActionResult.failure(
                    "You don't have permission to withdraw money."
            );
        }

        if (guild.balance() < amount) {
            return ActionResult.failure(
                    "The guild doesn't have enough money."
            );
        }

        guild.setBalance(
                guild.balance() - amount
        );

        return ActionResult.success(
                "You withdrew " + amount + " from the guild bank."
        );
    }

    @Override
    public ActionResult promoteMember(UUID actorId, UUID targetId) {

        Objects.requireNonNull(actorId, "actorId cannot be null");
        Objects.requireNonNull(targetId, "targetId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "You don't have a guild."
            );
        }

        Guild guild = optionalGuild.get();

        if (!guild.ownerId().equals(actorId)) {
            return ActionResult.failure(
                    "Only the guild owner can promote members."
            );
        }

        Optional<GuildMember> optionalTarget = guild.findMember(targetId);

        if (optionalTarget.isEmpty()) {
            return ActionResult.failure(
                    "This player is not a member of your guild."
            );
        }

        GuildMember target = optionalTarget.get();

        if (target.rank() == GuildRank.OWNER) {
            return ActionResult.failure(
                    "The guild owner cannot be promoted."
            );
        }

        if (target.rank() == GuildRank.OFFICER) {
            return ActionResult.failure(
                    "This member is already an officer."
            );
        }

        GuildMember promoted = new GuildMember(
                target.playerId(),
                target.name(),
                GuildRank.OFFICER
        );

        int index = guild.members().indexOf(target);

        guild.members().set(index, promoted);

        return ActionResult.success(
                target.name() + " has been promoted to officer."
        );
    }

    @Override
    public ActionResult demoteMember(UUID actorId, UUID targetId) {

        Objects.requireNonNull(actorId, "actorId cannot be null");
        Objects.requireNonNull(targetId, "targetId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "You don't have a guild."
            );
        }

        Guild guild = optionalGuild.get();

        if (!guild.ownerId().equals(actorId)) {
            return ActionResult.failure(
                    "Only the guild owner can demote members."
            );
        }

        Optional<GuildMember> optionalTarget = guild.findMember(targetId);

        if (optionalTarget.isEmpty()) {
            return ActionResult.failure(
                    "This player is not a member of your guild."
            );
        }

        GuildMember target = optionalTarget.get();

        if (target.rank() == GuildRank.OWNER) {
            return ActionResult.failure(
                    "The guild owner cannot be demoted."
            );
        }

        if (target.rank() == GuildRank.MEMBER) {
            return ActionResult.failure(
                    "This player already has the lowest rank."
            );
        }

        GuildMember demoted = new GuildMember(
                target.playerId(),
                target.name(),
                GuildRank.MEMBER
        );

        int index = guild.members().indexOf(target);

        guild.members().set(index, demoted);

        return ActionResult.success(
                target.name() + " has been demoted to member."
        );
    }

    @Override
    public ActionResult kickMember(UUID actorId, UUID targetId) {

        Objects.requireNonNull(actorId, "actorId cannot be null");
        Objects.requireNonNull(targetId, "targetId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "You don't have a guild."
            );
        }

        Guild guild = optionalGuild.get();

        if (!guild.ownerId().equals(actorId)) {
            return ActionResult.failure(
                    "Only the guild owner can kick members."
            );
        }

        if (actorId.equals(targetId)) {
            return ActionResult.failure(
                    "You cannot kick yourself."
            );
        }

        Optional<GuildMember> optionalTarget = guild.findMember(targetId);

        if (optionalTarget.isEmpty()) {
            return ActionResult.failure(
                    "This player is not a member of your guild."
            );
        }

        GuildMember target = optionalTarget.get();

        if (target.rank() == GuildRank.OWNER) {
            return ActionResult.failure(
                    "The guild owner cannot be kicked."
            );
        }

        guild.members().remove(target);

        return ActionResult.success(
                target.name() + " has been kicked from the guild."
        );
    }

    void addGuild(Guild guild) {
        Objects.requireNonNull(guild, "guild cannot be null");
        guilds.put(guild.id(), guild);
    }
}