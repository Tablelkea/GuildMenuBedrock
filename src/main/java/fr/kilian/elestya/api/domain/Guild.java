package fr.kilian.elestya.api.domain;

import java.util.*;

public class Guild {

    private final String id;
    private final String name;
    private final UUID ownerId;
    private final List<GuildMember> members;
    private final List<UUID> joinRequests;

    private final Map<GuildRank, Set<GuildPermission>> permissions;

    private double balance;

    public Guild(
            String id,
            String name,
            UUID ownerId,
            List<GuildMember> members,
            double balance,
            List<UUID> joinRequests
    ) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId cannot be null");
        this.members = Objects.requireNonNull(members, "members cannot be null");
        this.joinRequests = Objects.requireNonNull(joinRequests, "joinRequests cannot be null");

        this.balance = balance;

        this.permissions = new EnumMap<>(GuildRank.class);

        initializePermissions();
    }

    private void initializePermissions() {

        permissions.put(
                GuildRank.DEPUTY,
                EnumSet.noneOf(GuildPermission.class)
        );

        permissions.put(
                GuildRank.MEMBER,
                EnumSet.noneOf(GuildPermission.class)
        );

        permissions.put(
                GuildRank.RECRUIT,
                EnumSet.noneOf(GuildPermission.class)
        );
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public List<GuildMember> members() {
        return members;
    }

    public double balance() {
        return balance;
    }

    public List<UUID> joinRequests() {
        return joinRequests;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean hasJoinRequest(UUID playerId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        return joinRequests.contains(playerId);
    }

    public boolean isMember(UUID playerId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        return members.stream()
                .anyMatch(member -> member.playerId().equals(playerId));
    }

    public Optional<GuildMember> findMember(UUID playerId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        return members.stream()
                .filter(member -> member.playerId().equals(playerId))
                .findFirst();
    }

    public Set<GuildPermission> getPermissions(GuildRank rank) {

        Objects.requireNonNull(rank, "rank cannot be null");

        /*
         * The owner always has every permission.
         */
        if (rank == GuildRank.OWNER) {
            return EnumSet.allOf(GuildPermission.class);
        }

        Set<GuildPermission> rankPermissions = permissions.get(rank);

        if (rankPermissions == null) {
            return Set.of();
        }

        return Set.copyOf(rankPermissions);
    }

    public boolean hasPermission(
            GuildRank rank,
            GuildPermission permission
    ) {

        Objects.requireNonNull(rank, "rank cannot be null");
        Objects.requireNonNull(permission, "permission cannot be null");

        if (rank == GuildRank.OWNER) {
            return true;
        }

        Set<GuildPermission> rankPermissions = permissions.get(rank);

        return rankPermissions != null
                && rankPermissions.contains(permission);
    }

    public boolean hasPermission(
            UUID playerId,
            GuildPermission permission
    ) {

        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(permission, "permission cannot be null");

        Optional<GuildMember> optionalMember = findMember(playerId);

        return optionalMember.filter(guildMember -> hasPermission(
                guildMember.rank(),
                permission
        )).isPresent();

    }

    public void setPermission(
            GuildRank rank,
            GuildPermission permission,
            boolean enabled
    ) {

        Objects.requireNonNull(rank, "rank cannot be null");
        Objects.requireNonNull(permission, "permission cannot be null");

        /*
         * OWNER permissions cannot be configured.
         * The owner always has every permission.
         */
        if (rank == GuildRank.OWNER) {
            throw new IllegalArgumentException(
                    "OWNER permissions cannot be modified"
            );
        }

        Set<GuildPermission> rankPermissions = permissions.get(rank);

        if (rankPermissions == null) {
            throw new IllegalArgumentException(
                    "Unsupported guild rank: " + rank
            );
        }

        if (enabled) {
            rankPermissions.add(permission);
        } else {
            rankPermissions.remove(permission);
        }
    }
}