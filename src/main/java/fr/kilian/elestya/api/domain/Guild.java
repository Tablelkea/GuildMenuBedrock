package fr.kilian.elestya.api.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Guild {

    private final String id;
    private final String name;
    private final UUID ownerId;
    private final List<GuildMember> members;
    private double balance;
    private final List<UUID> joinRequests;

    public Guild(
            String id,
            String name,
            UUID ownerId,
            List<GuildMember> members,
            double balance,
            List<UUID> joinRequests
    ) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.members = members;
        this.balance = balance;
        this.joinRequests = joinRequests;
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
        return joinRequests.contains(playerId);
    }

    public boolean isMember(UUID playerId) {
        return members.stream()
                .anyMatch(member -> member.playerId().equals(playerId));
    }

    public Optional<GuildMember> findMember(UUID playerId) {
        return members.stream()
                .filter(member -> member.playerId().equals(playerId))
                .findFirst();
    }
}